package com.usuarios.api.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoRecuperacion(String emailDestino, String tokenTemporal) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestino);
        message.setSubject("Vital Vet - Recuperación de Contraseña");

        // El enlace apuntará a una pantalla de tu Frontend para que ponga la nueva clave
        String urlRecuperacion = "http://localhost:4200/reset-password?token=" + tokenTemporal;

        message.setText("Hola, para restablecer tu contraseña en Vital Vet, haz clic en el siguiente enlace:\n" + urlRecuperacion);

        mailSender.send(message);
    }
}

