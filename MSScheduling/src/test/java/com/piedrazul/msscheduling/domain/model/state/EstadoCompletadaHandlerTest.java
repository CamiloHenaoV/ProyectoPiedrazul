package com.piedrazul.msscheduling.domain.model.state;

import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.exceptions.TransicionEstadoInvalidaException;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoCompletadaHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EstadoCompletadaHandler")
class EstadoCompletadaHandlerTest {

    private EstadoCompletadaHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EstadoCompletadaHandler();
    }

    @Test
    @DisplayName("getEstado() debe retornar EstadoCita.completada")
    void getEstado_retornaCompletada() {
        assertThat(handler.getEstado()).isEqualTo(EstadoCita.completada);
    }

    // -----------------------------------------------------------------------
    // cancelar()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("cancelar() sobre cita completada lanza TransicionEstadoInvalidaException")
    void cancelar_lanzaExcepcion() {
        Cita cita = citaCompletada();

        assertThatThrownBy(() -> handler.cancelar(cita))
                .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    @DisplayName("cancelar() informa estadoActual=completada en la excepción")
    void cancelar_excepcionContieneEstadoActual() {
        Cita cita = citaCompletada();

        assertThatThrownBy(() -> handler.cancelar(cita))
                .isInstanceOf(TransicionEstadoInvalidaException.class)
                .satisfies(ex -> {
                    TransicionEstadoInvalidaException t = (TransicionEstadoInvalidaException) ex;
                    assertThat(t.getEstadoActual()).isEqualTo(EstadoCita.completada);
                });
    }

    @Test
    @DisplayName("cancelar() informa transicionIntentada=cancelada en la excepción")
    void cancelar_excepcionContieneTransicionIntentada() {
        Cita cita = citaCompletada();

        assertThatThrownBy(() -> handler.cancelar(cita))
                .isInstanceOf(TransicionEstadoInvalidaException.class)
                .satisfies(ex -> {
                    TransicionEstadoInvalidaException t = (TransicionEstadoInvalidaException) ex;
                    assertThat(t.getTransicionIntentada()).isEqualTo(EstadoCita.cancelada);
                });
    }

    @Test
    @DisplayName("cancelar() no muta el estado de la cita al lanzar excepción")
    void cancelar_noMutaEstado() {
        Cita cita = citaCompletada();

        try {
            handler.cancelar(cita);
        } catch (TransicionEstadoInvalidaException ignored) { }

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.completada);
    }

    // -----------------------------------------------------------------------
    // completar()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("completar() sobre cita completada lanza TransicionEstadoInvalidaException")
    void completar_lanzaExcepcion() {
        Cita cita = citaCompletada();

        assertThatThrownBy(() -> handler.completar(cita))
                .isInstanceOf(TransicionEstadoInvalidaException.class);
    }

    @Test
    @DisplayName("completar() informa estadoActual=completada en la excepción")
    void completar_excepcionContieneEstadoActual() {
        Cita cita = citaCompletada();

        assertThatThrownBy(() -> handler.completar(cita))
                .isInstanceOf(TransicionEstadoInvalidaException.class)
                .satisfies(ex -> {
                    TransicionEstadoInvalidaException t = (TransicionEstadoInvalidaException) ex;
                    assertThat(t.getEstadoActual()).isEqualTo(EstadoCita.completada);
                });
    }

    @Test
    @DisplayName("completar() informa transicionIntentada=completada en la excepción")
    void completar_excepcionContieneTransicionIntentada() {
        Cita cita = citaCompletada();

        assertThatThrownBy(() -> handler.completar(cita))
                .isInstanceOf(TransicionEstadoInvalidaException.class)
                .satisfies(ex -> {
                    TransicionEstadoInvalidaException t = (TransicionEstadoInvalidaException) ex;
                    assertThat(t.getTransicionIntentada()).isEqualTo(EstadoCita.completada);
                });
    }

    @Test
    @DisplayName("completar() no muta el estado de la cita al lanzar excepción")
    void completar_noMutaEstado() {
        Cita cita = citaCompletada();

        try {
            handler.completar(cita);
        } catch (TransicionEstadoInvalidaException ignored) { }

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.completada);
    }

    // -----------------------------------------------------------------------
    private Cita citaCompletada() {
        Cita cita = new Cita();
        cita.setEstado(EstadoCita.completada);
        return cita;
    }
}
