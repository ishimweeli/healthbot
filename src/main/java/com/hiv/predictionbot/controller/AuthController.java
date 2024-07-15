package com.hiv.predictionbot.controller;

import com.hiv.predictionbot.model.User;
import com.hiv.predictionbot.repository.UserRepository;
import com.hiv.predictionbot.security.JwtUtil;
import com.hiv.predictionbot.security.LoginRequest;
import com.hiv.predictionbot.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final HttpServletResponse httpServletResponse;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          UserService userService,
                          HttpServletResponse httpServletResponse) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.httpServletResponse = httpServletResponse;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, @RequestParam String token) {
        try {
            if (!jwtUtil.validateInvitationToken(token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired invitation token");
            }

            String invitedEmail = jwtUtil.getEmailFromInvitationToken(token);
            if (!invitedEmail.equals(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email does not match the invited email");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
            }

            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok("User registered successfully: " + savedUser.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userRepository.findByEmail(loginRequest.getEmail());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
            }
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getFirstName());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getEmail());
            response.put("Role", user.getRole());
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            cookie.setPath("/");
            httpServletResponse.addCookie(cookie);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during authentication");
        }
    }

    @GetMapping("/validate-token/{token}")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        System.out.println(token);
        try {
            String email = jwtUtil.getEmailFromInvitationToken(token);
            if (email != null) {
                return ResponseEntity.ok().body(Map.of("email", email));
            } else {
                return ResponseEntity.badRequest().body("Invalid token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error validating token");
        }
    }
}