package com.piedrazul.msscheduling.domain.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para leer y actualizar la configuración global de agendamiento.
 *
 * HU-1.7: el administrador configura cuántas semanas adelante
 *          se puede reservar una cita.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionAgendamientoDTO {

    private Long id;

    /**
     * Número de semanas habilitadas hacia adelante para agendamiento.
     * Rango permitido: 1–52.
     */
    @NotNull(message = "Las semanas habilitadas son obligatorias")
    @Min(value = 1, message = "Debe habilitar al menos 1 semana")
    @Max(value = 52, message = "No se pueden habilitar más de 52 semanas")
    private Integer semanasHabilitadas;
}
