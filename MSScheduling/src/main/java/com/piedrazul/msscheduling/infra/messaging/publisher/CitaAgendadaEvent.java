package com.piedrazul.msscheduling.infra.messaging.publisher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitaAgendadaEvent {
    private Long citaId;
    private Long pacienteId;
    private String pacienteNombre;
    private Long profesionalId;
    private String profesionalNombre;
    private ZonedDateTime fechaHora;
}
