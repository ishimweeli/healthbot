package com.hiv.predictionbot.service;

import com.hiv.predictionbot.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;
    @Autowired
    private final JwtUtil jwtUtil;

    @Value("${app.registration.url}")
    private String registrationUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender emailSender, JwtUtil jwtUtil) {
        this.emailSender = emailSender;
        this.jwtUtil = jwtUtil;
    }

    public void sendInvitationEmail(String to) {
        String token = jwtUtil.generateInvitationToken(to);
        String invitationLink = registrationUrl + "?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Invitation to HIV Survival App");
        message.setText("You've been invited to join the HIV Survival App. "
                + "Please use the following link to register: "
                + invitationLink);

        emailSender.send(message);
    }
}