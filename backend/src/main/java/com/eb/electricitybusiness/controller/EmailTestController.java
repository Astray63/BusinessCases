package com.eb.electricitybusiness.controller;

import com.eb.electricitybusiness.dto.ApiResponse;
import com.eb.electricitybusiness.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-test-email")
    public ResponseEntity<ApiResponse<?>> sendTestEmail(@RequestParam String email) {
        try {
            emailService.sendVerificationEmail(email, "Test User", "123456");
            return ResponseEntity.ok(ApiResponse.success("Email de test envoyé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Erreur: " + e.getMessage()));
        }
    }
}
