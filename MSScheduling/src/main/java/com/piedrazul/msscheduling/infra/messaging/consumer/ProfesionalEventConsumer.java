package com.piedrazul.msscheduling.infra.messaging.consumer;

import com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal;
import com.piedrazul.msscheduling.domain.model.repository.DisponibilidadSemanalRepository;
import com.piedrazul.msscheduling.infra.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Escucha el evento "profesional.creado" publicado por MSUserManagement
 * y genera la disponibilidad semanal por defecto (Lunes–Viernes 08:00–17:00)
 * usando el intervalo de cita configurado para ese profesional.
 */
@Component
@Slf4j
public class ProfesionalEventConsumer {

    // Lunes a Viernes en convención getDayOfWeek().getValue() % 7:
    //   Lunes=1, Martes=2, Miércoles=3, Jueves=4, Viernes=5
    private static final int[] DIAS_LABORALES = {1, 2, 3, 4, 5};
    private static final LocalTime HORA_INICIO_DEFAULT = LocalTime.of(7, 0);
    private static final LocalTime HORA_FIN_DEFAULT    = LocalTime.of(14, 0);

    private final DisponibilidadSemanalRepository disponibilidadRepository;

    public ProfesionalEventConsumer(DisponibilidadSemanalRepository disponibilidadRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_PROFESIONAL_CREADO)
    public void onProfesionalCreado(ProfesionalCreadoEvent event) {
        log.info("Evento recibido: profesional.creado profesionalId={} duracion={}min",
                event.getProfesionalId(), event.getDuracionCitaMinutos());

        int duracion = (event.getDuracionCitaMinutos() != null && event.getDuracionCitaMinutos() > 0)
                ? event.getDuracionCitaMinutos()
                : 30;

        List<DisponibilidadSemanal> slots = new ArrayList<>();
        for (int dia : DIAS_LABORALES) {
            slots.add(DisponibilidadSemanal.builder()
                    .profesionalId(event.getProfesionalId())
                    .diaSemana(dia)
                    .horaInicio(HORA_INICIO_DEFAULT)
                    .horaFin(HORA_FIN_DEFAULT)
                    .duracionCitaMinutos(duracion)
                    .build());
        }

        disponibilidadRepository.saveAll(slots);
        log.info("Disponibilidad semanal creada para profesionalId={} ({} días)",
                event.getProfesionalId(), slots.size());
    }
}