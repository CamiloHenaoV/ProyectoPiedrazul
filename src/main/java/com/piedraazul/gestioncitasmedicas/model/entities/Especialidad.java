package com.piedraazul.gestioncitasmedicas.model.entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "especialidades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @OneToMany(mappedBy = "especialidad")
    private List<Profesional> profesionales;
}
