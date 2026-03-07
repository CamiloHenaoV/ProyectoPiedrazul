package com.piedrazul.gestioncitasmedicas.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "bloqueos_disponibilidad")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BloqueoDisponibilidad {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Column(name = "fecha_inicio", nullable = false)
    private ZonedDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private ZonedDateTime fechaFin;

    @Column(length = 255)
    private String motivo;
}
