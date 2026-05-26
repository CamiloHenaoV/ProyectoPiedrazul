package com.piedrazul.frontend.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HttpException — Errores HTTP del API Gateway")
class HttpExceptionTest {

    // -----------------------------------------------------------------------
    // Constructor y campos básicos
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Constructor y campos básicos")
    class ConstructorTest {

        @Test
        @DisplayName("Es un RuntimeException (no marcada)")
        void esRuntimeException() {
            assertThat(new HttpException(400, "error")).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("getStatusCode() retorna el código HTTP pasado al constructor")
        void getStatusCode_retornaCodigoCorrecto() {
            assertThat(new HttpException(404, "no encontrado").getStatusCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("getMessage() retorna el mensaje pasado al constructor")
        void getMessage_retornaMensajeCorrecto() {
            assertThat(new HttpException(500, "error interno").getMessage())
                    .isEqualTo("error interno");
        }

        @ParameterizedTest(name = "statusCode={0}, mensaje=''{1}''")
        @CsvSource({
            "200, OK",
            "400, Bad Request",
            "401, Unauthorized",
            "500, Internal Server Error"
        })
        @DisplayName("Constructor asigna correctamente statusCode y mensaje en distintos escenarios")
        void constructor_asignaCorrectamente(int status, String mensaje) {
            HttpException ex = new HttpException(status, mensaje);
            assertThat(ex.getStatusCode()).isEqualTo(status);
            assertThat(ex.getMessage()).isEqualTo(mensaje);
        }
    }

    // -----------------------------------------------------------------------
    // isUnauthorized() — 401
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isUnauthorized() → 401")
    class UnauthorizedTest {

        @Test
        @DisplayName("Retorna true exactamente para código 401")
        void codigo401_retornaTrue() {
            assertThat(new HttpException(401, "msg").isUnauthorized()).isTrue();
        }

        @ParameterizedTest(name = "statusCode={0}")
        @ValueSource(ints = {400, 403, 404, 409, 500})
        @DisplayName("Retorna false para cualquier código distinto de 401")
        void codigoDistinto_retornaFalse(int status) {
            assertThat(new HttpException(status, "msg").isUnauthorized()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // isForbidden() — 403
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isForbidden() → 403")
    class ForbiddenTest {

        @Test
        @DisplayName("Retorna true exactamente para código 403")
        void codigo403_retornaTrue() {
            assertThat(new HttpException(403, "msg").isForbidden()).isTrue();
        }

        @ParameterizedTest(name = "statusCode={0}")
        @ValueSource(ints = {400, 401, 404, 409, 500})
        @DisplayName("Retorna false para cualquier código distinto de 403")
        void codigoDistinto_retornaFalse(int status) {
            assertThat(new HttpException(status, "msg").isForbidden()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // isNotFound() — 404
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isNotFound() → 404")
    class NotFoundTest {

        @Test
        @DisplayName("Retorna true exactamente para código 404")
        void codigo404_retornaTrue() {
            assertThat(new HttpException(404, "msg").isNotFound()).isTrue();
        }

        @ParameterizedTest(name = "statusCode={0}")
        @ValueSource(ints = {400, 401, 403, 409, 500})
        @DisplayName("Retorna false para cualquier código distinto de 404")
        void codigoDistinto_retornaFalse(int status) {
            assertThat(new HttpException(status, "msg").isNotFound()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // isConflict() — 409
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isConflict() → 409")
    class ConflictTest {

        @Test
        @DisplayName("Retorna true exactamente para código 409")
        void codigo409_retornaTrue() {
            assertThat(new HttpException(409, "msg").isConflict()).isTrue();
        }

        @ParameterizedTest(name = "statusCode={0}")
        @ValueSource(ints = {400, 401, 403, 404, 500})
        @DisplayName("Retorna false para cualquier código distinto de 409")
        void codigoDistinto_retornaFalse(int status) {
            assertThat(new HttpException(status, "msg").isConflict()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // isServerError() — 5xx
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isServerError() → 5xx")
    class ServerErrorTest {

        @ParameterizedTest(name = "statusCode={0}")
        @ValueSource(ints = {500, 502, 503, 504, 599})
        @DisplayName("Retorna true para cualquier código >= 500")
        void codigo5xx_retornaTrue(int status) {
            assertThat(new HttpException(status, "msg").isServerError()).isTrue();
        }

        @ParameterizedTest(name = "statusCode={0}")
        @ValueSource(ints = {200, 201, 400, 401, 404, 499})
        @DisplayName("Retorna false para códigos menores de 500")
        void codigoMenor500_retornaFalse(int status) {
            assertThat(new HttpException(status, "msg").isServerError()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // isUnavailable() — 503
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isUnavailable() → 503")
    class UnavailableTest {

        @Test
        @DisplayName("Retorna true exactamente para código 503")
        void codigo503_retornaTrue() {
            assertThat(new HttpException(503, "msg").isUnavailable()).isTrue();
        }

        @ParameterizedTest(name = "statusCode={0}")
        @ValueSource(ints = {500, 502, 504, 400, 404})
        @DisplayName("Retorna false para cualquier código distinto de 503")
        void codigoDistinto_retornaFalse(int status) {
            assertThat(new HttpException(status, "msg").isUnavailable()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // Exclusividad entre clasificadores
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Exclusividad entre clasificadores")
    class ExclusividadTest {

        @Test
        @DisplayName("Solo isUnauthorized() es true para 401")
        void solo401_esUnauthorized() {
            HttpException ex = new HttpException(401, "msg");
            assertThat(ex.isUnauthorized()).isTrue();
            assertThat(ex.isForbidden()).isFalse();
            assertThat(ex.isNotFound()).isFalse();
            assertThat(ex.isConflict()).isFalse();
            assertThat(ex.isServerError()).isFalse();
            assertThat(ex.isUnavailable()).isFalse();
        }

        @Test
        @DisplayName("Solo isConflict() es true para 409")
        void solo409_esConflict() {
            HttpException ex = new HttpException(409, "msg");
            assertThat(ex.isConflict()).isTrue();
            assertThat(ex.isUnauthorized()).isFalse();
            assertThat(ex.isForbidden()).isFalse();
            assertThat(ex.isNotFound()).isFalse();
            assertThat(ex.isServerError()).isFalse();
        }

        @Test
        @DisplayName("isServerError() y isUnavailable() son ambos true para 503")
        void codigo503_esServerErrorYUnavailable() {
            HttpException ex = new HttpException(503, "msg");
            assertThat(ex.isServerError()).isTrue();
            assertThat(ex.isUnavailable()).isTrue();
        }
    }
}
