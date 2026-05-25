package com.piedrazul.msscheduling.domain.model.state;

import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoProgramadaHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EstadoProgramadaHandler")
class EstadoProgramadaHandlerTest {

    private EstadoProgramadaHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EstadoProgramadaHandler();
    }

    @Test
    @DisplayName("getEstado() debe retornar EstadoCita.programada")
    void getEstado_retornaProgramada() {
        assertThat(handler.getEstado()).isEqualTo(EstadoCita.programada);
    }

    @Test
    @DisplayName("cancelar() transiciona la cita de programada a cancelada")
    void cancelar_cambiasEstadoACancelada() {
        Cita cita = citaProgramada();

        handler.cancelar(cita);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.cancelada);
    }

    @Test
    @DisplayName("completar() transiciona la cita de programada a completada")
    void completar_cambiasEstadoACompletada() {
        Cita cita = citaProgramada();

        handler.completar(cita);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.completada);
    }

    @Test
    @DisplayName("cancelar() no lanza excepción para cita programada")
    void cancelar_noLanzaExcepcion() {
        Cita cita = citaProgramada();
        // No debe lanzar nada
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> handler.cancelar(cita));
    }

    @Test
    @DisplayName("completar() no lanza excepción para cita programada")
    void completar_noLanzaExcepcion() {
        Cita cita = citaProgramada();
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> handler.completar(cita));
    }

    // -----------------------------------------------------------------------
    private Cita citaProgramada() {
        Cita cita = new Cita();
        cita.setEstado(EstadoCita.programada);
        return cita;
    }
}
