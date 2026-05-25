package com.piedrazul.msauthservice.domain.model.entity;

import com.piedrazul.msauthservice.domain.model.entity.enums.RolUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MSAuth — Entidades de dominio")
class AuthDomainEntityTest {

    // -----------------------------------------------------------------------
    // Credencial
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Credencial")
    class CredencialTest {

        @Test
        @DisplayName("Builder crea Credencial con todos los campos asignados")
        void builder_creaConCamposCompletos() {
            ZonedDateTime ahora = ZonedDateTime.now();
            Credencial cred = Credencial.builder()
                    .id(1L)
                    .usuarioId(10L)
                    .login("jperez")
                    .passwordHash("$2a$10$hash")
                    .activo(true)
                    .creadoEn(ahora)
                    .actualizadoEn(ahora)
                    .build();

            assertThat(cred.getId()).isEqualTo(1L);
            assertThat(cred.getUsuarioId()).isEqualTo(10L);
            assertThat(cred.getLogin()).isEqualTo("jperez");
            assertThat(cred.getPasswordHash()).isEqualTo("$2a$10$hash");
            assertThat(cred.getActivo()).isTrue();
            assertThat(cred.getCreadoEn()).isEqualTo(ahora);
            assertThat(cred.getActualizadoEn()).isEqualTo(ahora);
        }

        @Test
        @DisplayName("activo tiene valor por defecto true al usar @Builder.Default")
        void activoPorDefecto_esTrue() {
            Credencial cred = Credencial.builder()
                    .usuarioId(5L)
                    .login("test")
                    .passwordHash("hash")
                    .build();

            assertThat(cred.getActivo()).isTrue();
        }

        @Test
        @DisplayName("Setter de activo puede marcarla inactiva")
        void setter_activoPuedeDesactivar() {
            Credencial cred = Credencial.builder()
                    .usuarioId(1L).login("u").passwordHash("h").build();
            cred.setActivo(false);

            assertThat(cred.getActivo()).isFalse();
        }

        @Test
        @DisplayName("Setter de passwordHash actualiza el hash")
        void setter_passwordHash_actualiza() {
            Credencial cred = Credencial.builder()
                    .usuarioId(1L).login("u").passwordHash("old").build();
            cred.setPasswordHash("new_hash");

            assertThat(cred.getPasswordHash()).isEqualTo("new_hash");
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new Credencial()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // RefreshToken
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RefreshToken")
    class RefreshTokenTest {

        @Test
        @DisplayName("esValido() retorna true cuando no está usado, no revocado y no ha expirado")
        void esValido_tokenVigente_retornaTrue() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L)
                    .token("uuid-token")
                    .expiraEn(ZonedDateTime.now().plusHours(1))
                    .build();

            assertThat(token.esValido()).isTrue();
        }

        @Test
        @DisplayName("esValido() retorna false cuando está marcado como usado")
        void esValido_tokenUsado_retornaFalse() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L)
                    .token("uuid-token")
                    .expiraEn(ZonedDateTime.now().plusHours(1))
                    .usado(true)
                    .build();

            assertThat(token.esValido()).isFalse();
        }

        @Test
        @DisplayName("esValido() retorna false cuando está revocado")
        void esValido_tokenRevocado_retornaFalse() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L)
                    .token("uuid-token")
                    .expiraEn(ZonedDateTime.now().plusHours(1))
                    .revocado(true)
                    .build();

            assertThat(token.esValido()).isFalse();
        }

        @Test
        @DisplayName("esValido() retorna false cuando ya expiró")
        void esValido_tokenExpirado_retornaFalse() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L)
                    .token("uuid-token")
                    .expiraEn(ZonedDateTime.now().minusHours(1))
                    .build();

            assertThat(token.esValido()).isFalse();
        }

        @Test
        @DisplayName("esValido() retorna false cuando está usado Y revocado Y expirado")
        void esValido_tokenTotalmenteInvalido_retornaFalse() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L)
                    .token("uuid-token")
                    .expiraEn(ZonedDateTime.now().minusSeconds(1))
                    .usado(true)
                    .revocado(true)
                    .build();

            assertThat(token.esValido()).isFalse();
        }

        @Test
        @DisplayName("usado y revocado tienen valor por defecto false")
        void valoresPorDefecto_usadoYRevocado_sonFalse() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L)
                    .token("t")
                    .expiraEn(ZonedDateTime.now().plusHours(1))
                    .build();

            assertThat(token.getUsado()).isFalse();
            assertThat(token.getRevocado()).isFalse();
        }

        @Test
        @DisplayName("Builder asigna usuarioId y token correctamente")
        void builder_asignaCamposPrincipales() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(42L)
                    .token("mi-uuid-unico")
                    .expiraEn(ZonedDateTime.now().plusDays(7))
                    .build();

            assertThat(token.getUsuarioId()).isEqualTo(42L);
            assertThat(token.getToken()).isEqualTo("mi-uuid-unico");
        }

        @Test
        @DisplayName("Setter revocado cambia el estado del token")
        void setter_revocado_cambiaEstado() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L).token("t")
                    .expiraEn(ZonedDateTime.now().plusHours(1))
                    .build();

            token.setRevocado(true);

            assertThat(token.getRevocado()).isTrue();
            assertThat(token.esValido()).isFalse();
        }

        @Test
        @DisplayName("Setter usado cambia el estado del token")
        void setter_usado_cambiaEstado() {
            RefreshToken token = RefreshToken.builder()
                    .usuarioId(1L).token("t")
                    .expiraEn(ZonedDateTime.now().plusHours(1))
                    .build();

            token.setUsado(true);

            assertThat(token.getUsado()).isTrue();
            assertThat(token.esValido()).isFalse();
        }

        @Test
        @DisplayName("NoArgsConstructor crea instancia sin lanzar excepción")
        void noArgs_creaInstancia() {
            assertThat(new RefreshToken()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // RolUsuario enum (MSAuth)
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RolUsuario enum (MSAuth)")
    class RolUsuarioEnumTest {

        @Test
        @DisplayName("Contiene exactamente los cuatro roles del sistema")
        void tieneLosCuatroRoles() {
            assertThat(RolUsuario.values())
                    .containsExactlyInAnyOrder(
                            RolUsuario.profesional,
                            RolUsuario.agendador,
                            RolUsuario.administrador,
                            RolUsuario.paciente
                    );
        }

        @Test
        @DisplayName("name() retorna valores en minúsculas tal como están declarados")
        void nombres_enMinusculas() {
            assertThat(RolUsuario.paciente.name()).isEqualTo("paciente");
            assertThat(RolUsuario.profesional.name()).isEqualTo("profesional");
            assertThat(RolUsuario.administrador.name()).isEqualTo("administrador");
            assertThat(RolUsuario.agendador.name()).isEqualTo("agendador");
        }
    }
}
