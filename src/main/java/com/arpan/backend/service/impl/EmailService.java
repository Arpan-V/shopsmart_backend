package com.arpan.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendAlert(String to, String subject, String body) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Important: Gmail often rejects emails where "From" doesn't match the Auth user
            message.setFrom("${spring.mail.username}");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        } catch (Exception e) {
            // This will show up in your Render "Logs" tab
            System.err.println("SMTP Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}