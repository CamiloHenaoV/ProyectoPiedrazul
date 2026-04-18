package com.piedrazul.frontend.model.dto;

import com.piedrazul.frontend.model.enums.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CitaDTO {
    private UUID          id;
    private UUID          pacienteId;
    private UUID          profesionalId;
    private String        profesionalNombre;
    private ZonedDateTime fechaHora;
    private EstadoCita    estado;
}
