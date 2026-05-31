package com.piedrazul.frontend.http;

import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionManager — Gestión de sesión activa")
class SessionManagerTest {

    /**
     * Resetea el singleton antes de cada test para garantizar aislamiento.
     * Como SessionManager guarda estado estático, sin este reset los tests
     * podrían interferir entre sí.
     */
    @BeforeEach
    void resetSingleton() throws Exception {
        Field field = SessionManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Patrón Singleton")
    class SingletonTest {

        @Test
        @DisplayName("getInstance() siempre retorna la misma instancia")
        void getInstance_retornaMismaInstancia() {
            SessionManager s1 = SessionManager.getInstance();
            SessionManager s2 = SessionManager.getInstance();

            assertThat(s1).isSameAs(s2);
        }

        @Test
        @DisplayName("getInstance() no retorna null")
        void getInstance_noRetornaNull() {
            assertThat(SessionManager.getInstance()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Token JWT
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Gestión del token JWT")
    class TokenTest {

        @Test
        @DisplayName("getToken() retorna null cuando no se ha asignado ningún token")
        void getToken_sinAsignar_retornaNull() {
            assertThat(SessionManager.getInstance().getToken()).isNull();
        }

        @Test
        @DisplayName("getToken() retorna el token asignado con setToken()")
        void getToken_retornaTokenAsignado() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("eyJhbGciOiJIUzI1NiJ9.payload.sig");

            assertThat(sm.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiJ9.payload.sig");
        }

        @Test
        @DisplayName("setToken() sobreescribe un token previo")
        void setToken_sobreescribeTokenPrevio() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("token-viejo");
            sm.setToken("token-nuevo");

            assertThat(sm.getToken()).isEqualTo("token-nuevo");
        }
    }

    // -----------------------------------------------------------------------
    // hasSesion()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("hasSesion()")
    class HasSesionTest {

        @Test
        @DisplayName("Retorna false cuando no hay token")
        void sinToken_retornaFalse() {
            assertThat(SessionManager.getInstance().hasSesion()).isFalse();
        }

        @Test
        @DisplayName("Retorna false cuando el token es cadena vacía")
        void tokenVacio_retornaFalse() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("");

            assertThat(sm.hasSesion()).isFalse();
        }

        @Test
        @DisplayName("Retorna false cuando el token es solo espacios")
        void tokenSoloEspacios_retornaFalse() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("   ");

            assertThat(sm.hasSesion()).isFalse();
        }

        @Test
        @DisplayName("Retorna true cuando hay un token válido asignado")
        void tokenValido_retornaTrue() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("Bearer token-valido");

            assertThat(sm.hasSesion()).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Usuario actual
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Gestión del usuario actual")
    class UsuarioActualTest {

        @Test
        @DisplayName("getUsuarioActual() retorna null cuando no se ha asignado ningún usuario")
        void getUsuarioActual_sinAsignar_retornaNull() {
            assertThat(SessionManager.getInstance().getUsuarioActual()).isNull();
        }

        @Test
        @DisplayName("getUsuarioActual() retorna el usuario asignado con setUsuarioActual()")
        void getUsuarioActual_retornaUsuarioAsignado() {
            SessionManager sm = SessionManager.getInstance();
            UsuarioDTO usuario = usuario(1L, "jperez", RolUsuario.paciente);
            sm.setUsuarioActual(usuario);

            assertThat(sm.getUsuarioActual()).isSameAs(usuario);
        }

        @Test
        @DisplayName("setUsuarioActual() sobreescribe el usuario previo")
        void setUsuarioActual_sobreescribeUsuarioPrevio() {
            SessionManager sm = SessionManager.getInstance();
            sm.setUsuarioActual(usuario(1L, "user1", RolUsuario.paciente));
            UsuarioDTO nuevo = usuario(2L, "user2", RolUsuario.administrador);
            sm.setUsuarioActual(nuevo);

            assertThat(sm.getUsuarioActual().getLogin()).isEqualTo("user2");
        }

        @Test
        @DisplayName("El rol del usuario actual se preserva correctamente")
        void rolUsuario_sePreserva() {
            SessionManager sm = SessionManager.getInstance();
            sm.setUsuarioActual(usuario(3L, "admin", RolUsuario.administrador));

            assertThat(sm.getUsuarioActual().getRol()).isEqualTo(RolUsuario.administrador);
        }
    }

    // -----------------------------------------------------------------------
    // cerrarSesion()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("cerrarSesion()")
    class CerrarSesionTest {

        @Test
        @DisplayName("Limpia el token tras cerrar sesión")
        void cerrarSesion_limpiaToken() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("token-activo");
            sm.cerrarSesion();

            assertThat(sm.getToken()).isNull();
        }

        @Test
        @DisplayName("Limpia el usuario actual tras cerrar sesión")
        void cerrarSesion_limpiaUsuarioActual() {
            SessionManager sm = SessionManager.getInstance();
            sm.setUsuarioActual(usuario(1L, "u", RolUsuario.paciente));
            sm.cerrarSesion();

            assertThat(sm.getUsuarioActual()).isNull();
        }

        @Test
        @DisplayName("hasSesion() retorna false inmediatamente tras cerrar sesión")
        void cerrarSesion_hasSesionEsFalse() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("token");
            sm.cerrarSesion();

            assertThat(sm.hasSesion()).isFalse();
        }

        @Test
        @DisplayName("cerrarSesion() es idempotente: llamarlo dos veces no lanza excepción")
        void cerrarSesion_esIdempotente() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("t");
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
                sm.cerrarSesion();
                sm.cerrarSesion();
            });
        }

        @Test
        @DisplayName("La sesión se puede iniciar de nuevo tras cerrarla")
        void reabrir_sesion_trasCerrar() {
            SessionManager sm = SessionManager.getInstance();
            sm.setToken("primer-token");
            sm.cerrarSesion();
            sm.setToken("segundo-token");

            assertThat(sm.hasSesion()).isTrue();
            assertThat(sm.getToken()).isEqualTo("segundo-token");
        }
    }

    // -----------------------------------------------------------------------
    // Fixture
    // -----------------------------------------------------------------------
    private UsuarioDTO usuario(Long id, String login, RolUsuario rol) {
        return UsuarioDTO.builder()
                .id(id).login(login).nombreCompleto("Usuario " + login)
                .rol(rol).activo(true).build();
    }
}
