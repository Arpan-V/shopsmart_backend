package com.arpan.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 1. THIS IS THE FIX. This tells Spring to grab the real email from your envs.
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendAlert(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // 2. Use the variable here! No quotes around it.
            message.setFrom(senderEmail);

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            System.out.println("Attempting to send email to: " + to); // Added a 'start' log
            mailSender.send(message);
            System.out.println("Email successfully sent to Google for: " + to);

        } catch (Exception e) {
            System.err.println("SMTP Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}