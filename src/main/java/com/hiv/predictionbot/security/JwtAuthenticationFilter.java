package com.hiv.predictionbot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtils;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    private final List<String> excludedUrls = Arrays.asList("/api/auth/login", "/api/auth/register", "/", "/swagger-ui/**", "/v3/api-docs/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isExcludedUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractJwtFromRequest(request);
        logger.debug("Extracted token: {}", token);

        if (StringUtils.hasText(token)) {
            try {
                logger.debug("Attempting to validate token");
                if (jwtUtils.validateToken(token)) {
                    String email = jwtUtils.getUsernameFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token);
                    logger.debug("current role Role: {}", role);
                    logger.debug("Email extracted from token: {}", email);
                    logger.debug("Role extracted from token: {}", role);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication set in SecurityContext");

                    if (isAuthorized(request, role)) {
                        filterChain.doFilter(request, response);
                    } else {
                        handleAuthorizationFailure(response, "Insufficient permissions");
                    }
                } else {
                    logger.warn("Invalid JWT token");
                    handleAuthenticationFailure(response, "Invalid JWT token");
                }
            } catch (Exception e) {
                logger.error("Error processing JWT token", e);
                handleAuthenticationFailure(response, "Error processing JWT token");
            }
        } else {
            logger.warn("Missing JWT token");
            handleAuthenticationFailure(response, "Missing JWT token");
        }
    }

    private boolean isAuthorized(HttpServletRequest request, String role) {
        String requestURI = request.getRequestURI();
        System.out.println("thiiiiiiiis is "+role);
        // Add your authorization logic here
        // For example:
        if (requestURI.startsWith("/admin") && !role.equals("ADMIN")) {
            logger.info("role",role);

            return false;
        }
        // Add more rules as needed
        return true;
    }

    private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private void handleAuthorizationFailure(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private boolean isExcludedUrl(HttpServletRequest request) {
        return excludedUrls.stream()
                .anyMatch(url -> new AntPathRequestMatcher(url).matches(request));
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}