package com.piedraazul.gestioncitasmedicas.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "historias_clinicas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoriaClinica {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Column(name = "fecha_registro")
    private ZonedDateTime fechaRegistro;

    @Column(name = "motivo_consulta", columnDefinition = "TEXT")
    private String motivoConsulta;

    // JSONB de PostgreSQL → Hibernate 6 lo maneja nativamente
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signos_vitales", columnDefinition = "jsonb")
    private String signosVitales;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String observaciones;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;
}
