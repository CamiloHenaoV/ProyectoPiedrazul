package com.piedrazul.apigateway.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenValidationResponse")
class TokenValidationResponseTest {

    // -----------------------------------------------------------------------
    // isValido()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isValido()")
    class ValidoTest {

        @Test
        @DisplayName("isValido() retorna false por defecto (campo booleano primitivo)")
        void valido_porDefecto_esFalse() {
            assertThat(new TokenValidationResponse().isValido()).isFalse();
        }

        @Test
        @DisplayName("setValido(true) → isValido() retorna true")
        void setValido_true_retornaTrue() {
            TokenValidationResponse r = new TokenValidationResponse();
            r.setValido(true);
            assertThat(r.isValido()).isTrue();
        }

        @Test
        @DisplayName("setValido(false) → isValido() retorna false")
        void setValido_false_retornaFalse() {
            TokenValidationResponse r = new TokenValidationResponse();
            r.setValido(true);
            r.setValido(false);
            assertThat(r.isValido()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // Campos de identificación del usuario
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Campos de identificación")
    class CamposIdentificacionTest {

        @Test
        @DisplayName("getUsuarioId() retorna null por defecto")
        void usuarioId_porDefecto_esNull() {
            assertThat(new TokenValidationResponse().getUsuarioId()).isNull();
        }

        @Test
        @DisplayName("setUsuarioId() → getUsuarioId() retorna el valor asignado")
        void setUsuarioId_retornaValorAsignado() {
            TokenValidationResponse r = new TokenValidationResponse();
            r.setUsuarioId(42L);
            assertThat(r.getUsuarioId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("getLogin() retorna null por defecto")
        void login_porDefecto_esNull() {
            assertThat(new TokenValidationResponse().getLogin()).isNull();
        }

        @Test
        @DisplayName("setLogin() → getLogin() retorna el valor asignado")
        void setLogin_retornaValorAsignado() {
            TokenValidationResponse r = new TokenValidationResponse();
            r.setLogin("jperez");
            assertThat(r.getLogin()).isEqualTo("jperez");
        }

        @Test
        @DisplayName("getRol() retorna null por defecto")
        void rol_porDefecto_esNull() {
            assertThat(new TokenValidationResponse().getRol()).isNull();
        }

        @Test
        @DisplayName("setRol() → getRol() retorna el valor asignado")
        void setRol_retornaValorAsignado() {
            TokenValidationResponse r = new TokenValidationResponse();
            r.setRol("profesional");
            assertThat(r.getRol()).isEqualTo("profesional");
        }
    }

    // -----------------------------------------------------------------------
    // Respuesta completa (token válido)
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Respuesta completa de token válido")
    class RespuestaCompletaTest {

        @Test
        @DisplayName("Todos los campos se asignan y recuperan correctamente")
        void todosLosCampos_asignadosYRecuperados() {
            TokenValidationResponse r = new TokenValidationResponse();
            r.setValido(true);
            r.setUsuarioId(7L);
            r.setLogin("agomez");
            r.setRol("administrador");

            assertThat(r.isValido()).isTrue();
            assertThat(r.getUsuarioId()).isEqualTo(7L);
            assertThat(r.getLogin()).isEqualTo("agomez");
            assertThat(r.getRol()).isEqualTo("administrador");
        }

        @Test
        @DisplayName("Respuesta de token inválido tiene valido=false y los demás campos null")
        void respuestaInvalida_camposNulos() {
            TokenValidationResponse r = new TokenValidationResponse();
            // No se llama a ningún setter → simula deserialización de respuesta de token inválido

            assertThat(r.isValido()).isFalse();
            assertThat(r.getUsuarioId()).isNull();
            assertThat(r.getLogin()).isNull();
            assertThat(r.getRol()).isNull();
        }
    }
}
