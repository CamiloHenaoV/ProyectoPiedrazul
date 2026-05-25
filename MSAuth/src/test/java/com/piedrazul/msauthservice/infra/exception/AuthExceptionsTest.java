package com.piedrazul.msauthservice.infra.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MSAuth — Excepciones de infraestructura")
class AuthExceptionsTest {

    // -----------------------------------------------------------------------
    // CredencialDuplicadaException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("CredencialDuplicadaException")
    class CredencialDuplicadaExceptionTest {

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new CredencialDuplicadaException("msg"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje refleja el texto del constructor")
        void mensaje_reflejaTexto() {
            String msg = "El login 'jperez' ya existe";
            assertThat(new CredencialDuplicadaException(msg).getMessage())
                    .isEqualTo(msg);
        }

        @Test
        @DisplayName("Anotada con @ResponseStatus(CONFLICT = 409)")
        void anotacion_responseStatus_esConflict() {
            ResponseStatus rs = CredencialDuplicadaException.class
                    .getAnnotation(ResponseStatus.class);

            assertThat(rs).isNotNull();
            assertThat(rs.value()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    // -----------------------------------------------------------------------
    // CredencialesInvalidasException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("CredencialesInvalidasException")
    class CredencialesInvalidasExceptionTest {

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new CredencialesInvalidasException())
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje es genérico (no revela si el login existe)")
        void mensaje_esGenerico() {
            String msg = new CredencialesInvalidasException().getMessage();
            assertThat(msg).isNotBlank();
            // No debe decir "login no existe" ni "contraseña incorrecta"
            assertThat(msg.toLowerCase()).doesNotContain("no existe", "incorrecta");
        }

        @Test
        @DisplayName("Anotada con @ResponseStatus(UNAUTHORIZED = 401)")
        void anotacion_responseStatus_esUnauthorized() {
            ResponseStatus rs = CredencialesInvalidasException.class
                    .getAnnotation(ResponseStatus.class);

            assertThat(rs).isNotNull();
            assertThat(rs.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Dos instancias tienen el mismo mensaje (constructor sin parámetros)")
        void dosInstancias_mismoMensaje() {
            String msg1 = new CredencialesInvalidasException().getMessage();
            String msg2 = new CredencialesInvalidasException().getMessage();
            assertThat(msg1).isEqualTo(msg2);
        }
    }

    // -----------------------------------------------------------------------
    // PasswordInvalidaException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("PasswordInvalidaException")
    class PasswordInvalidaExceptionTest {

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new PasswordInvalidaException("error"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje refleja el texto del constructor")
        void mensaje_reflejaTexto() {
            String msg = "La contraseña debe tener al menos 8 caracteres";
            assertThat(new PasswordInvalidaException(msg).getMessage()).isEqualTo(msg);
        }

        @Test
        @DisplayName("Anotada con @ResponseStatus(BAD_REQUEST = 400)")
        void anotacion_responseStatus_esBadRequest() {
            ResponseStatus rs = PasswordInvalidaException.class
                    .getAnnotation(ResponseStatus.class);

            assertThat(rs).isNotNull();
            assertThat(rs.value()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // -----------------------------------------------------------------------
    // RefreshTokenInvalidoException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("RefreshTokenInvalidoException")
    class RefreshTokenInvalidoExceptionTest {

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new RefreshTokenInvalidoException())
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje no está vacío")
        void mensaje_noEstaVacio() {
            assertThat(new RefreshTokenInvalidoException().getMessage()).isNotBlank();
        }

        @Test
        @DisplayName("Mensaje menciona 'inválido' o 'expirado'")
        void mensaje_mencionaInvalidoOExpirado() {
            String msg = new RefreshTokenInvalidoException().getMessage().toLowerCase();
            assertThat(msg).containsAnyOf("inválido", "invalido", "expirado");
        }

        @Test
        @DisplayName("Anotada con @ResponseStatus(UNAUTHORIZED = 401)")
        void anotacion_responseStatus_esUnauthorized() {
            ResponseStatus rs = RefreshTokenInvalidoException.class
                    .getAnnotation(ResponseStatus.class);

            assertThat(rs).isNotNull();
            assertThat(rs.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
