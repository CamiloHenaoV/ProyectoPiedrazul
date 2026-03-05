package com.piedraazul.gestioncitasmedicas.model.entities;
import com.piedraazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @UuidGenerator                                  // Hibernate 6+ reemplaza uuid_generate_v4()
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "rol_usuario")
    private RolUsuario rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en")
    private ZonedDateTime creadoEn;

    @OneToOne(mappedBy = "usuario")
    private Profesional profesional;

    @OneToOne(mappedBy = "usuario")
    private Paciente paciente;

    @OneToOne(mappedBy = "usuario")
    private Responsable responsable;
}
