package com.piedrazul.msnotifications.infra.messaging;

import com.piedrazul.msnotifications.domain.model.entity.Notificacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailSenderAdapter {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public EmailSenderAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviar(Notificacion notificacion) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(notificacion.getDestinatario());
        message.setSubject(notificacion.getAsunto());
        message.setText(notificacion.getCuerpo());
        mailSender.send(message);
        log.info("Email enviado a {}", notificacion.getDestinatario());
    }
}
