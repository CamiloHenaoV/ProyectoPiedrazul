package com.piedrazul.gestioncitasmedicas.model.dto;

import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaDTO {
    private UUID id;
    private UUID pacienteId;
    private String pacienteNombre;
    private Integer profesionalId;
    private String profesionalNombre;
    private ZonedDateTime fechaHora;
    private EstadoCita estado;
}
