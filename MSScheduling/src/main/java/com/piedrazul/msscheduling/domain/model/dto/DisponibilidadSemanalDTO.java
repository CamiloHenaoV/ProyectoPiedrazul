package com.piedrazul.msscheduling.domain.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

/**
 * DTO para configurar la disponibilidad semanal de un profesional.
 *
 * HU-1.5: configuración de días habilitados y franjas horarias.
 * HU-1.6: intervalo de atención entre citas (duracionCitaMinutos).
 *
 * Validaciones:
 *   - profesionalId: obligatorio
 *   - diaSemana: 0 (domingo) a 6 (sábado)
 *   - horaInicio / horaFin: obligatorias
 *   - duracionCitaMinutos: mínimo 5, máximo 240 (4h)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadSemanalDTO {

    private Long id;

    @NotNull(message = "El profesional es obligatorio")
    private Long profesionalId;

    /**
     * Día de la semana: 0 = Domingo, 1 = Lunes … 6 = Sábado.
     * HU-1.5 SC-3: el administrador selecciona días de atención.
     */
    @NotNull(message = "El día de la semana es obligatorio")
    @Min(value = 0, message = "El día de la semana debe ser entre 0 (Domingo) y 6 (Sábado)")
    @Max(value = 6, message = "El día de la semana debe ser entre 0 (Domingo) y 6 (Sábado)")
    private Integer diaSemana;

    /** HU-1.5 SC-4: inicio de franja horaria de atención. */
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    /** HU-1.5 SC-4: fin de franja horaria de atención. */
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    /**
     * Duración de cada cita (= intervalo entre citas) en minutos.
     * HU-1.6 SC-1: el administrador define los minutos entre citas.
     * HU-1.6 SC-2: intervalo inválido si < 5 o > 240 o si no cabe ninguna
     *              cita en la franja horaria (validado en el servicio).
     */
    @NotNull(message = "La duración de la cita es obligatoria")
    @Min(value = 5,   message = "El intervalo mínimo es 5 minutos")
    @Max(value = 240, message = "El intervalo máximo es 240 minutos (4 horas)")
    @Builder.Default
    private Integer duracionCitaMinutos = 30;
}
