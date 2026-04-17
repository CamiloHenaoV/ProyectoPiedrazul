package com.piedrazul.msscheduling.infra.messaging.consumer;

import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import com.piedrazul.msscheduling.domain.model.repository.UsuarioLocalRepository;
import com.piedrazul.msscheduling.infra.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventConsumer {

    private final UsuarioLocalRepository usuarioLocalRepository;

    public UserEventConsumer(UsuarioLocalRepository usuarioLocalRepository) {
        this.usuarioLocalRepository = usuarioLocalRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_USER_REGISTERED)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Evento recibido: user.registered userId={} rol={}", event.getUserId(), event.getRol());

        // Upsert: si ya existe lo actualiza, si no lo crea
        UsuarioLocal usuario = usuarioLocalRepository.findById(event.getUserId())
                .orElse(new UsuarioLocal());

        usuario.setId(event.getUserId());
        usuario.setLogin(event.getLogin());
        usuario.setNombreCompleto(event.getLogin()); // nombre real llega por UserUpdated cuando esté disponible
        usuario.setRol(RolUsuario.valueOf(event.getRol()));
        usuario.setActivo(true);

        usuarioLocalRepository.save(usuario);
        log.info("Usuario sincronizado en caché local: id={}", event.getUserId());
    }
}
