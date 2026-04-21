package com.piedrazul.msusermanagement.domain.model.entity;

import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String login;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private RolUsuario rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en")
    private ZonedDateTime creadoEn;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Profesional profesional;

    @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
    private Paciente paciente;

    @PrePersist void prePersist() { creadoEn = ZonedDateTime.now(); }
}
