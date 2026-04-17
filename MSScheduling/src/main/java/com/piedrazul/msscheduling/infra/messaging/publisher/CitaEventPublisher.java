package com.piedrazul.msscheduling.infra.messaging.publisher;

import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.infra.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CitaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public CitaEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarCitaAgendada(CitaDTO dto) {
        CitaAgendadaEvent event = new CitaAgendadaEvent(
                dto.getId(),
                dto.getPacienteId(),
                dto.getPacienteNombre(),
                dto.getProfesionalId(),
                dto.getProfesionalNombre(),
                dto.getFechaHora()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.SCHEDULING_EXCHANGE,
                                      RabbitConfig.ROUTING_CITA_AGENDADA, event);
        log.info("Evento publicado: cita.agendada citaId={}", dto.getId());
    }

    public void publicarCitaCancelada(CitaDTO dto) {
        CitaCanceladaEvent event = new CitaCanceladaEvent(
                dto.getId(),
                dto.getPacienteId(),
                dto.getProfesionalId(),
                dto.getFechaHora()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.SCHEDULING_EXCHANGE,
                                      RabbitConfig.ROUTING_CITA_CANCELADA, event);
        log.info("Evento publicado: cita.cancelada citaId={}", dto.getId());
    }

    public void publicarCitaCompletada(CitaDTO dto) {
        CitaCompletadaEvent event = new CitaCompletadaEvent(
                dto.getId(),
                dto.getPacienteId(),
                dto.getProfesionalId(),
                dto.getFechaHora()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.SCHEDULING_EXCHANGE,
                                      RabbitConfig.ROUTING_CITA_COMPLETADA, event);
        log.info("Evento publicado: cita.completada citaId={}", dto.getId());
    }
}
