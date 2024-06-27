package com.hiv.predictionbot.controller;

import com.hiv.predictionbot.repository.UserRepository;
import com.hiv.predictionbot.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/admin")
public class InvitationController {
    @Autowired
private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

//    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @PostMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestBody String email) {
        if(userRepository.existsByEmail(email)){
            return  ResponseEntity.badRequest().body("Failed to send invitation: email already exist ");
        }
//        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
//            return ResponseEntity.badRequest().body("Invalid email format");
//        }
        try {
            emailService.sendInvitationEmail(email);
            return ResponseEntity.ok().body("Invitation sent successfully to " + email);
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send invitation: " + e.getMessage());
        }
    }
}