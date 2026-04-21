package com.piedrazul.msscheduling.domain.model.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadSemanalDTO {
    private Long id;
    private Long profesionalId;
    private Integer diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionCitaMinutos;
}
