package com.piedrazul.msscheduling.domain.model.entity;

import com.piedrazul.msscheduling.domain.model.entity.enums.TipoDiaNoDisponible;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Día no disponible a nivel global: festivos y días bloqueados por el administrador.
 *
 * HU-1.8: el administrador puede registrar fechas que bloquean el agendamiento
 * para TODOS los profesionales en ese día. Diferente a {@link BloqueoDisponibilidad}
 * que bloquea solo a un profesional específico.
 *
 * Escenarios cubiertos:
 *   1. Registro de día no disponible (SC-1): fecha restringida → agendamiento bloqueado.
 *   2. Validación de festivos (SC-2): la fecha está marcada como FESTIVO.
 *   3. Eliminación de restricción (SC-3): el admin habilita de nuevo la fecha.
 */
@Entity
@Table(name = "dias_no_disponibles",
       uniqueConstraints = @UniqueConstraint(name = "uc_fecha_no_disponible", columnNames = "fecha"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaNoDisponible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha exacta bloqueada (sin hora). */
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Descripción opcional del motivo (ej.: "Día de la Independencia"). */
    @Column(name = "motivo", length = 255)
    private String motivo;

    /**
     * Tipo de bloqueo: FESTIVO o BLOQUEO_MANUAL.
     * Los festivos tienen icono diferente en la UI.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private TipoDiaNoDisponible tipo = TipoDiaNoDisponible.BLOQUEO_MANUAL;
}
