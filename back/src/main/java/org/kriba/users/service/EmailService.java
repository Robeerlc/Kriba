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
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user) {

        String url = baseUrl + "/api/v1/auth/verify/" + user.getVerificationUuid();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verifica tu cuenta - Kriba");

        message.setText(
                "Bienvenido a Kriba!\n\n" +
                "Para activar tu cuenta haz click en el siguiente enlace:\n\n" +
                url + "\n\n" +
                "Si no has creado esta cuenta, ignora este correo."
        );

        mailSender.send(message);
    }
}