package com.piedraazul.gestioncitasmedicas.model.entities;

import com.piedraazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "citas",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_profesional_horario",
                columnNames = {"profesional_id", "fecha_hora"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cita {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profesional_id", nullable = false)
    private Profesional profesional;

    @Column(name = "fecha_hora", nullable = false)
    private ZonedDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "estado_cita")
    private EstadoCita estado = EstadoCita.programada;

    @Column(name = "creado_en")
    private ZonedDateTime creadoEn;

    @OneToOne(mappedBy = "cita", cascade = CascadeType.ALL)
    private HistoriaClinica historiaClinica;
}
