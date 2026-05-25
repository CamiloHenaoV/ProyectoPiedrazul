package com.piedrazul.msauthservice.application.service;

import com.piedrazul.msauthservice.application.service.impl.BCryptPasswordServiceImpl;
import com.piedrazul.msauthservice.infra.exception.PasswordInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BCryptPasswordServiceImpl")
class BCryptPasswordServiceImplTest {

    private BCryptPasswordServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BCryptPasswordServiceImpl();
    }

    // -----------------------------------------------------------------------
    // encriptar
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("encriptar()")
    class EncriptarTest {

        @Test
        @DisplayName("Retorna un hash no vacío")
        void retornaHashNoVacio() {
            assertThat(service.encriptar("Password1")).isNotBlank();
        }

        @Test
        @DisplayName("El hash empieza con el prefijo BCrypt '$2a$'")
        void hashTienePrefijoBcrypt() {
            String hash = service.encriptar("Password1");
            assertThat(hash).startsWith("$2a$");
        }

        @Test
        @DisplayName("Dos llamadas con la misma contraseña producen hashes distintos (sal aleatoria)")
        void dosLlamadasProducenHashesDistintos() {
            String h1 = service.encriptar("Password1");
            String h2 = service.encriptar("Password1");
            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("El hash no contiene la contraseña en texto plano")
        void hashNoContienePasswordEnPlano() {
            String password = "Password1";
            String hash = service.encriptar(password);
            assertThat(hash).doesNotContain(password);
        }
    }

    // -----------------------------------------------------------------------
    // verificar
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("verificar()")
    class VerificarTest {

        @Test
        @DisplayName("Retorna true cuando la contraseña coincide con su hash")
        void coincidencia_retornaTrue() {
            String hash = service.encriptar("Password1");
            assertThat(service.verificar("Password1", hash)).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando la contraseña no coincide con el hash")
        void noCoincidencia_retornaFalse() {
            String hash = service.encriptar("Password1");
            assertThat(service.verificar("OtraPassword2", hash)).isFalse();
        }

        @Test
        @DisplayName("Retorna false para contraseña vacía contra un hash real")
        void contrasenaVacia_retornaFalse() {
            String hash = service.encriptar("Password1");
            assertThat(service.verificar("", hash)).isFalse();
        }

        @Test
        @DisplayName("Es consistente: misma contraseña siempre verifica correctamente su propio hash")
        void consistencia_mismaPasswordVerifica() {
            String password = "Segura99";
            String hash = service.encriptar(password);
            // verificar tres veces para asegurar idempotencia
            assertThat(service.verificar(password, hash)).isTrue();
            assertThat(service.verificar(password, hash)).isTrue();
            assertThat(service.verificar(password, hash)).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // validarFormato
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("validarFormato()")
    class ValidarFormatoTest {

        @Test
        @DisplayName("No lanza excepción para contraseña válida (≥8 chars, 1 mayúscula, 1 número)")
        void passwordValida_noLanzaExcepcion() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> service.validarFormato("Password1"));
        }

        @Test
        @DisplayName("No lanza excepción para contraseña larga con mayúsculas y números")
        void passwordLargaValida_noLanzaExcepcion() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> service.validarFormato("MiSuperPassword99!"));
        }

        @ParameterizedTest(name = "password=''{0}'' es demasiado corta")
        @ValueSource(strings = {"Pass1", "Ab1", "A1", ""})
        @DisplayName("Lanza PasswordInvalidaException para contraseñas de menos de 8 caracteres")
        void passwordCorta_lanzaExcepcion(String password) {
            assertThatThrownBy(() -> service.validarFormato(password))
                    .isInstanceOf(PasswordInvalidaException.class);
        }

        @Test
        @DisplayName("Lanza PasswordInvalidaException cuando es null")
        void passwordNull_lanzaExcepcion() {
            assertThatThrownBy(() -> service.validarFormato(null))
                    .isInstanceOf(PasswordInvalidaException.class);
        }

        @Test
        @DisplayName("Lanza PasswordInvalidaException cuando no tiene mayúscula")
        void sinMayuscula_lanzaExcepcion() {
            assertThatThrownBy(() -> service.validarFormato("password1"))
                    .isInstanceOf(PasswordInvalidaException.class);
        }

        @Test
        @DisplayName("Lanza PasswordInvalidaException cuando no tiene número")
        void sinNumero_lanzaExcepcion() {
            assertThatThrownBy(() -> service.validarFormato("PasswordSinNum"))
                    .isInstanceOf(PasswordInvalidaException.class);
        }

        @Test
        @DisplayName("El mensaje de excepción por longitud menciona '8 caracteres'")
        void mensajeLongitud_menciona8Caracteres() {
            assertThatThrownBy(() -> service.validarFormato("Ab1"))
                    .isInstanceOf(PasswordInvalidaException.class)
                    .hasMessageContaining("8");
        }

        @Test
        @DisplayName("El mensaje de excepción por formato menciona 'mayúscula' y 'número'")
        void mensajeFormato_mencionaMayusculaYNumero() {
            assertThatThrownBy(() -> service.validarFormato("sinmayuscula1"))
                    .isInstanceOf(PasswordInvalidaException.class)
                    .hasMessageContainingAll("mayúscula", "número");
        }
    }
}
