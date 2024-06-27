package com.hiv.predictionbot.controller;

import com.hiv.predictionbot.model.User;
import com.hiv.predictionbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUserDetails() {
        User user = userService.getCurrentUserDetails();
        return ResponseEntity.ok(user);
    }
}