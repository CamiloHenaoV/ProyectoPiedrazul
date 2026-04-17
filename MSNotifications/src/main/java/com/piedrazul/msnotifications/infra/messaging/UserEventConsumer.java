package com.piedrazul.msnotifications.infra.messaging;

import com.piedrazul.msnotifications.application.service.interfaces.INotificacionService;
import com.piedrazul.msnotifications.domain.model.entity.enums.CanalNotificacion;
import com.piedrazul.msnotifications.infra.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventConsumer {

    private final INotificacionService notificacionService;

    public UserEventConsumer(INotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Evento recibido: user.registered para userId={} login={}", event.getUserId(), event.getLogin());

        String asunto = "Bienvenido a PiedraZul";
        String cuerpo = String.format(
                "Hola %s, tu cuenta ha sido creada exitosamente con el rol: %s.",
                event.getLogin(),
                event.getRol()
        );

        notificacionService.enviar(
                event.getLogin(),
                asunto,
                cuerpo,
                CanalNotificacion.EMAIL,
                "user.registered"
        );
    }

    // Descomenta cuando Scheduling esté listo:
    /*
    @RabbitListener(queues = RabbitConfig.QUEUE_CITA_AGENDADA)
    public void onCitaAgendada(CitaAgendadaEvent event) {
        log.info("Evento recibido: cita.agendada citaId={}", event.getCitaId());
        // ...
    }
    */
}
