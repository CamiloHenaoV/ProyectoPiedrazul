package com.piedrazul.msscheduling.domain.model.exceptions;

import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Excepciones de dominio")
class DomainExceptionsTest {

    // -----------------------------------------------------------------------
    // TransicionEstadoInvalidaException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("TransicionEstadoInvalidaException")
    class TransicionEstadoInvalidaExceptionTest {

        @Test
        @DisplayName("Construye mensaje con los dos estados involucrados")
        void mensaje_contieneAmbosEstados() {
            var ex = new TransicionEstadoInvalidaException(
                    EstadoCita.cancelada, EstadoCita.completada);

            assertThat(ex.getMessage())
                    .contains("cancelada")
                    .contains("completada");
        }

        @Test
        @DisplayName("getEstadoActual() retorna el estado pasado como primer argumento")
        void getEstadoActual_retornaEstadoOrigen() {
            var ex = new TransicionEstadoInvalidaException(
                    EstadoCita.completada, EstadoCita.cancelada);

            assertThat(ex.getEstadoActual()).isEqualTo(EstadoCita.completada);
        }

        @Test
        @DisplayName("getTransicionIntentada() retorna el estado pasado como segundo argumento")
        void getTransicionIntentada_retornaEstadoDestino() {
            var ex = new TransicionEstadoInvalidaException(
                    EstadoCita.cancelada, EstadoCita.cancelada);

            assertThat(ex.getTransicionIntentada()).isEqualTo(EstadoCita.cancelada);
        }

        @Test
        @DisplayName("Es un RuntimeException (no marcada)")
        void esRuntimeException() {
            var ex = new TransicionEstadoInvalidaException(
                    EstadoCita.programada, EstadoCita.cancelada);

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("cancelada → cancelada: mensaje refleja correctamente el caso homogéneo")
        void mensajeCanceladaCancelada() {
            var ex = new TransicionEstadoInvalidaException(
                    EstadoCita.cancelada, EstadoCita.cancelada);

            // El mensaje debe mencionar 'cancelada' en alguna forma
            assertThat(ex.getMessage()).containsIgnoringCase("cancelada");
        }

        @Test
        @DisplayName("completada → completada: mensaje refleja correctamente el caso homogéneo")
        void mensajeCompletadaCompletada() {
            var ex = new TransicionEstadoInvalidaException(
                    EstadoCita.completada, EstadoCita.completada);

            assertThat(ex.getMessage()).containsIgnoringCase("completada");
        }
    }

    // -----------------------------------------------------------------------
    // FechaNoDisponibleException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("FechaNoDisponibleException")
    class FechaNoDisponibleExceptionTest {

        @Test
        @DisplayName("Mensaje incluye la fecha recibida")
        void mensaje_contienelaFecha() {
            var ex = new FechaNoDisponibleException("2025-12-25");

            assertThat(ex.getMessage()).contains("2025-12-25");
        }

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new FechaNoDisponibleException("2025-01-01"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje menciona 'no disponible' o 'festivo'")
        void mensaje_mencionaNoDisponibleOFestivo() {
            var ex = new FechaNoDisponibleException("2025-06-20");

            assertThat(ex.getMessage().toLowerCase())
                    .containsAnyOf("no disponible", "festivo");
        }
    }

    // -----------------------------------------------------------------------
    // FueraDeVentanaAgendamientoException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("FueraDeVentanaAgendamientoException")
    class FueraDeVentanaAgendamientoExceptionTest {

        @Test
        @DisplayName("Mensaje incluye el número de semanas habilitadas")
        void mensaje_contieneSemanasHabilitadas() {
            var ex = new FueraDeVentanaAgendamientoException(4);

            assertThat(ex.getMessage()).contains("4");
        }

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new FueraDeVentanaAgendamientoException(2))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje varía según el número de semanas")
        void mensaje_variaConSemanas() {
            String msg8 = new FueraDeVentanaAgendamientoException(8).getMessage();
            String msg1 = new FueraDeVentanaAgendamientoException(1).getMessage();

            assertThat(msg8).contains("8");
            assertThat(msg1).contains("1");
            assertThat(msg8).isNotEqualTo(msg1);
        }
    }

    // -----------------------------------------------------------------------
    // HorarioOcupadoException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("HorarioOcupadoException")
    class HorarioOcupadoExceptionTest {

        @Test
        @DisplayName("Mensaje informa que el profesional no está disponible")
        void mensaje_informaNoDisponible() {
            var ex = new HorarioOcupadoException();

            assertThat(ex.getMessage()).isNotBlank();
            assertThat(ex.getMessage().toLowerCase()).contains("disponible");
        }

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new HorarioOcupadoException())
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // -----------------------------------------------------------------------
    // ConfiguracionInvalidaException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("ConfiguracionInvalidaException")
    class ConfiguracionInvalidaExceptionTest {

        @Test
        @DisplayName("Mensaje refleja el texto recibido en el constructor")
        void mensaje_refleja_textoConstructor() {
            String motivo = "La hora de inicio es posterior a la hora de fin";
            var ex = new ConfiguracionInvalidaException(motivo);

            assertThat(ex.getMessage()).isEqualTo(motivo);
        }

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new ConfiguracionInvalidaException("error"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje con texto vacío no lanza NullPointerException")
        void mensajeVacio_noLanzaNPE() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> new ConfiguracionInvalidaException("")
            );
        }
    }

    // -----------------------------------------------------------------------
    // CitaNoEncontradaException
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("CitaNoEncontradaException")
    class CitaNoEncontradaExceptionTest {

        @Test
        @DisplayName("Es un RuntimeException")
        void esRuntimeException() {
            assertThat(new CitaNoEncontradaException("99"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje incluye el ID de la cita buscada")
        void mensaje_contieneId() {
            var ex = new CitaNoEncontradaException("42");
            assertThat(ex.getMessage()).contains("42");
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
            assertThat(new UsuarioNoEncontradoException("5"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Mensaje incluye el ID del usuario buscado")
        void mensaje_contieneId() {
            var ex = new UsuarioNoEncontradoException("7");
            assertThat(ex.getMessage()).contains("7");
        }
    }
}
