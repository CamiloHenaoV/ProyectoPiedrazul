package com.piedrazul.msscheduling.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Configuración global del sistema de agendamiento.
 * Singleton de base de datos (id = 1). Guarda:
 *   - semanasHabilitadas : ventana de tiempo hacia adelante (HU-1.7)
 * Uso: el administrador define cuántas semanas a futuro pueden reservarse citas.
 * El sistema bloquea automáticamente cualquier fecha fuera de ese rango.
 */
@Entity
@Table(name = "configuracion_agendamiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionAgendamiento {

    /** ID fijo = 1, siempre existe un único registro de configuración. */
    @Id
    @Column(nullable = false)
    private Long id;

    /**
     * Número de semanas hacia adelante que los pacientes pueden reservar citas.
     * Mínimo 1, máximo 52.
     * HU-1.7: define la «ventana de tiempo disponible para agendamiento».
     */
    @Column(name = "semanas_habilitadas", nullable = false)
    @Builder.Default
    private Integer semanasHabilitadas = 4;
}
