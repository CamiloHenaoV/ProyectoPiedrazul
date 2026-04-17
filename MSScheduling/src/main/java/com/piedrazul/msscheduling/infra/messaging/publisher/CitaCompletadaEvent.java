package com.piedrazul.msscheduling.infra.messaging.publisher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitaCompletadaEvent {
    private UUID citaId;
    private Long pacienteId;
    private Long profesionalId;
    private ZonedDateTime fechaHora;
}
