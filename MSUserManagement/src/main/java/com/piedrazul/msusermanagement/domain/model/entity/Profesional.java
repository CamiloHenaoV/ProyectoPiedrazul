package com.piedrazul.msusermanagement.domain.model.entity;

import com.piedrazul.msusermanagement.domain.model.entity.enums.TipoProfesional;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "profesionales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private TipoProfesional tipo;

    @ManyToOne
    @JoinColumn(name = "especialidad_id",nullable=false)
    private Especialidad especialidad;

    @Column(name = "licencia_profesional", unique = true, nullable = false, length = 50)
    private String licenciaProfesional;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "duracion_cita_minutos", nullable = false)
    private Integer duracionCitaMinutos;
}
