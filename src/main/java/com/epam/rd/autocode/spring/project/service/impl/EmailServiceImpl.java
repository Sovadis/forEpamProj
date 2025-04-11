package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.EmailAuthenticationException;
import com.epam.rd.autocode.spring.project.exception.EmailSendingException;
import com.epam.rd.autocode.spring.project.exception.EmailTimeoutException;
import com.epam.rd.autocode.spring.project.exception.InvalidEmailAddressException;
import com.epam.rd.autocode.spring.project.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");

            String htmlContent = "<p>Ви подали заявку на зброс паролю для вашого акаунту.</p>"
                    + "<p>Натисніть на кнопку вище, щоб встановити новий пароль:</p>"
                    + "<p style='text-align:center;'>"
                    + "<a href=\"" + resetLink + "\" style='display:inline-block;padding:10px 20px;font-size:16px;"
                    + "color:#FFFFFF;background-color:#4CAF50;text-decoration:none;border-radius:5px;'>Зкинути пароль</a>"
                    + "</p>"
                    + "<p>Якщо це були не ви, то ігноруйте цей лист.</p>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MailAuthenticationException e) {
            log.error("Failed to send password reset email to {}: SMTP authentication failed", toEmail);
            throw new EmailAuthenticationException("SMTP authentication failed", e);
        } catch (MailException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SendFailedException || cause instanceof AddressException || cause instanceof MessagingException) {
                log.error("Failed to send password reset email to {}: invalid email address", toEmail);
                throw new InvalidEmailAddressException("Invalid email address", e);
            }
            if (cause != null && cause.getMessage() != null && cause.getMessage().toLowerCase().contains("timed out")) {
                log.error("Failed to send password reset email to {}: mail server connection timed out", toEmail);
                throw new EmailTimeoutException("Mail server connection timed out", e);
            }
            log.error("Failed to send password reset email to {}: unexpected mail sending error: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Unexpected error during email sending", e);
        } catch (MessagingException e) {
            if (e instanceof AddressException) {
                log.error("Failed to send password reset email: invalid email address format '{}' - {}", toEmail, e.getMessage());
                throw new InvalidEmailAddressException("Invalid email address format", e);
            }
            log.error("Failed to send password reset email to {}: error preparing email message: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Error preparing email message", e);
        }
    }
}
