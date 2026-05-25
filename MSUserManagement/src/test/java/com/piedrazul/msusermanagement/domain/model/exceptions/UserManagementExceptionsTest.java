package com.piedrazul.msusermanagement.domain.model.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MSUserManagement — Excepciones de dominio")
class UserManagementExceptionsTest {

    // -----------------------------------------------------------------------
    // LoginDuplicadoException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("LoginDuplicadoException")
    class LoginDuplicadoExceptionTest {

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new LoginDuplicadoException("jperez"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje incluye el login recibido")
        void mensaje_contieneLogin() {
            var ex = new LoginDuplicadoException("maria.garcia");
            assertThat(ex.getMessage()).contains("maria.garcia");
        }

        @Test
        @DisplayName("Mensaje varía según el login proporcionado")
        void mensaje_variaConLogin() {
            String msg1 = new LoginDuplicadoException("loginA").getMessage();
            String msg2 = new LoginDuplicadoException("loginB").getMessage();
            assertThat(msg1).isNotEqualTo(msg2);
        }

        @Test
        @DisplayName("Mensaje no está vacío para cualquier login")
        void mensaje_noEstaVacio() {
            assertThat(new LoginDuplicadoException("").getMessage()).isNotNull();
            assertThat(new LoginDuplicadoException("x").getMessage()).isNotBlank();
        }
    }

    // -----------------------------------------------------------------------
    // UsuarioNoEncontradoException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("UsuarioNoEncontradoException")
    class UsuarioNoEncontradoExceptionTest {

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new UsuarioNoEncontradoException("jperez"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje incluye el login recibido")
        void mensaje_contieneLogin() {
            var ex = new UsuarioNoEncontradoException("agomez");
            assertThat(ex.getMessage()).contains("agomez");
        }

        @Test
        @DisplayName("Mensaje varía según el login proporcionado")
        void mensaje_variaConLogin() {
            String msg1 = new UsuarioNoEncontradoException("userA").getMessage();
            String msg2 = new UsuarioNoEncontradoException("userB").getMessage();
            assertThat(msg1).isNotEqualTo(msg2);
        }

        @Test
        @DisplayName("Mensaje no está vacío para cualquier login")
        void mensaje_noEstaVacio() {
            assertThat(new UsuarioNoEncontradoException("x").getMessage()).isNotBlank();
        }
    }
}
