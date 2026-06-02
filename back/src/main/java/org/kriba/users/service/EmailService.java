package org.kriba.users.service;

import org.kriba.users.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String backendBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user, String uuid) {
        String link = backendBaseUrl + "/api/v1/auth/verify/" + uuid;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verifica tu cuenta");
        message.setText("Haz click aquí para verificar tu cuenta:\n" + link);
        mailSender.send(message);
    }
}