package com.piedrazul.msusermanagement.infra.messaging;

import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import com.piedrazul.msusermanagement.infra.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserRegistered(Usuario usuario) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getNombreCompleto(),
                usuario.getRol().name()
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                event
        );
    }
}
