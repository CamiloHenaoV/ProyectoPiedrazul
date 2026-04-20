package com.piedrazul.frontend.model.dto;

import com.piedrazul.frontend.model.enums.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CitaDTO {
    private Long         id;
    private Long pacienteId;
    private Long profesionalId;
    private String pacienteNombre;
    private String        profesionalNombre;
    private ZonedDateTime fechaHora;
    private EstadoCita    estado;
}
