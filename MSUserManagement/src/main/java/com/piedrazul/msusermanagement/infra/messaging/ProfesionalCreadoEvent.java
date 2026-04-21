package com.piedrazul.msusermanagement.infra.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfesionalCreadoEvent {
    private Long   profesionalId;
    private String nombreCompleto;
    private Integer duracionCitaMinutos;
}