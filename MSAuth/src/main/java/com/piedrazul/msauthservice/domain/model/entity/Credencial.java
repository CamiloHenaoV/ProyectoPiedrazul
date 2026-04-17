package com.piedrazul.msauthservice.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

/**
 * Almacena las credenciales de acceso de un usuario.
 * <p>
 * - {@code usuarioId} es una FK lógica hacia ms-usuario-service.
 *   No existe como FK de BD real porque cada microservicio
 *   tiene su propia base de datos.
 * - {@code login} se duplica aquí para que auth-service pueda
 *   resolver el login sin llamar a usuario-service en cada intento.
 * - {@code passwordHash} contiene el hash BCrypt. La contraseña
 *   en texto plano nunca se persiste ni se propaga entre servicios.
 */
@Entity
@Table(name = "credenciales", indexes = {
        @Index(name = "idx_credenciales_login", columnList = "login", unique = true),
        @Index(name = "idx_credenciales_usuario_id", columnList = "usuario_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK lógica hacia usuario-service */
    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    /** Identificador de login — duplicado de usuario-service */
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    /** Hash BCrypt de la contraseña */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private ZonedDateTime creadoEn;

    @Column(name = "actualizado_en")
    private ZonedDateTime actualizadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = ZonedDateTime.now();
        actualizadoEn = ZonedDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        actualizadoEn = ZonedDateTime.now();
    }
}
