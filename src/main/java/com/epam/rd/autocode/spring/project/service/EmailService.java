package com.epam.rd.autocode.spring.project.service;

import org.springframework.mail.MailException;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink) throws MailException;
}
