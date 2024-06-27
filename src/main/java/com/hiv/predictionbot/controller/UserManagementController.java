package com.hiv.predictionbot.controller;

import com.hiv.predictionbot.model.User;
import com.hiv.predictionbot.service.UserManagementService;
import com.hiv.predictionbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class UserManagementController {

    private final UserService userService;
    private final UserManagementService userManagementService;

    @Autowired
    public UserManagementController(UserService userService, UserManagementService userManagementService) {
        this.userService = userService;
        this.userManagementService = userManagementService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/user/{email}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable String email, @RequestParam String newRole) {
        try {
            User updatedUser = userManagementService.changeUserRole(email, newRole);
            return ResponseEntity.ok("User role updated successfully for: " + updatedUser.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update user role: " + e.getMessage());
        }
    }
}