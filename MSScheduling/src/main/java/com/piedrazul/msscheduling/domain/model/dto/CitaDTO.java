package com.piedrazul.msscheduling.domain.model.dto;

import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
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
    private Long pacienteId;
    private String pacienteNombre;
    private Long profesionalId;
    private String profesionalNombre;
    private ZonedDateTime fechaHora;
    private EstadoCita estado;
}
