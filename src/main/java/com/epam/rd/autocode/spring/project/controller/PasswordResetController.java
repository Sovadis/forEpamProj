package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordResetController {

    private final EmailService emailService;

    public PasswordResetController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        String resetLink = "https://example.com/reset?token=ABC123";

        try {
            emailService.sendPasswordResetEmail(email, resetLink);
            return ResponseEntity.ok("Reset password email sent successfully.");
        } catch (MailException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send reset password email. Please try again later.");
        }
    }
}

