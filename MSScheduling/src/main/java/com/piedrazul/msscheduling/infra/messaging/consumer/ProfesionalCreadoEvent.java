package com.piedrazul.msscheduling.infra.messaging.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfesionalCreadoEvent {
    private Long    profesionalId;
    private String  nombreCompleto;
    private Integer duracionCitaMinutos;
}