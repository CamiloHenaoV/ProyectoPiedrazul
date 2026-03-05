package com.piedraazul.gestioncitasmedicas.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "disponibilidad_semanal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisponibilidadSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;               // 0=Domingo ... 6=Sábado

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "duracion_cita_minutos")
    private Integer duracionCitaMinutos = 30;
}
