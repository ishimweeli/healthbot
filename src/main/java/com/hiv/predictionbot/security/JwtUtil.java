package com.hiv.predictionbot.security;

import com.hiv.predictionbot.model.Role;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.invitation.expiration}")
    private Long invitationExpiration;

    public String generateToken(String email, Role role, String firstName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        claims.put("firstName", firstName);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return (String) claims.get("role");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            logger.debug("Token validated successfully");
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String generateInvitationToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "invitation");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + invitationExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public boolean validateInvitationToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            return "invitation".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid invitation token: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromInvitationToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            if ("invitation".equals(claims.get("type"))) {
                String email = claims.getSubject();
                // Remove any surrounding quotation marks
                return email.replaceAll("^\"|\"$", "");
            }
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid invitation token: {}", e.getMessage());
        }
        return null;
    }
}