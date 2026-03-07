package com.piedrazul.gestioncitasmedicas.model.entities;

import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "profesionales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private TipoProfesional tipo;

    @ManyToOne
    @JoinColumn(name = "especialidad_id")
    private Especialidad especialidad;

    @Column(name = "licencia_profesional", unique = true, nullable = false, length = 50)
    private String licenciaProfesional;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL)
    private List<DisponibilidadSemanal> disponibilidades;

    @OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL)
    private List<BloqueoDisponibilidad> bloqueos;
}
