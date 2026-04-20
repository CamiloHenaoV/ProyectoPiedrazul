package com.piedrazul.msauthservice.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

/**
 * Refresh token persistido en BD.
 * <p>
 * Permite renovar el access token sin que el usuario vuelva
 * a introducir sus credenciales. Se invalida en logout o
 * cuando se emite uno nuevo (rotación).
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_valor", columnList = "token", unique = true),
        @Index(name = "idx_refresh_token_usuario", columnList = "usuario_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Valor UUID del token */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expira_en", nullable = false)
    private ZonedDateTime expiraEn;

    @Column(name = "usado", nullable = false)
    @Builder.Default
    private Boolean usado = false;

    @Column(name = "revocado", nullable = false)
    @Builder.Default
    private Boolean revocado = false;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private ZonedDateTime creadoEn;

    @PrePersist
    void prePersist() {
        creadoEn = ZonedDateTime.now();
    }

    public boolean esValido() {
        return !usado && !revocado && ZonedDateTime.now().isBefore(expiraEn);
    }
}
