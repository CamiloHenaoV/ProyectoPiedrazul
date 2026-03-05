package com.piedraazul.gestioncitasmedicas.model.dto;

import com.piedraazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CitaDTO {
    private UUID          id;
    private UUID          pacienteId;
    private String        pacienteNombre;
    private Integer       profesionalId;
    private String        profesionalNombre;
    private ZonedDateTime fechaHora;
    private EstadoCita estado;
}
