package com.lms.lms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String to, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        send(to, "Verify Your Email", "Welcome! Please verify your email by clicking the link below:\n\n" + link + "\n\nThis link expires in 24 hours.");
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        send(to, "Reset Your Password", "A password reset was requested for your account. Click the link below to reset it:\n\n" + link + "\n\nThis link expires in 1 hour. If you did not request this, you can ignore this email.");
    }

    private void send(String to, String subject, String body) {
        if (mailSender == null || mailHost.isBlank()) {
            log.warn("Mail is not configured (MAIL_HOST is empty). Email to {} with subject '{}' was not sent. Body:\n{}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // a mail failure should not fail the request that triggered it; the user can use resend-verification
            log.error("Failed to send email to {} with subject '{}': {}", to, subject, e.getMessage());
        }
    }
}
