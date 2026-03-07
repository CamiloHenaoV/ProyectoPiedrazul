package com.piedrazul.gestioncitasmedicas.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "responsables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Responsable {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "cedula_identidad", unique = true, nullable = false, length = 20)
    private String cedulaIdentidad;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 50)
    private String parentesco;

    @OneToMany(mappedBy = "responsable")
    private List<Paciente> pacientes;
}
