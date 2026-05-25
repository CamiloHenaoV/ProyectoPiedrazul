package com.piedrazul.msscheduling.domain.model.state;

import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoCanceladaHandler;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoCompletadaHandler;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoProgramadaHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CitaEstadoResolver")
class CitaEstadoResolverTest {

    private CitaEstadoResolver resolver;

    @BeforeEach
    void setUp() {
        // Construimos el resolver con los tres handlers concretos, igual que lo
        // haría Spring al levantar el contexto.
        resolver = new CitaEstadoResolver(List.of(
                new EstadoProgramadaHandler(),
                new EstadoCanceladaHandler(),
                new EstadoCompletadaHandler()
        ));
    }

    // -----------------------------------------------------------------------
    // Resolución correcta de cada estado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("resolve(programada) retorna un EstadoProgramadaHandler")
    void resolve_programada_retornaHandlerCorrecto() {
        EstadoCitaHandler handler = resolver.resolve(EstadoCita.programada);

        assertThat(handler).isInstanceOf(EstadoProgramadaHandler.class);
    }

    @Test
    @DisplayName("resolve(cancelada) retorna un EstadoCanceladaHandler")
    void resolve_cancelada_retornaHandlerCorrecto() {
        EstadoCitaHandler handler = resolver.resolve(EstadoCita.cancelada);

        assertThat(handler).isInstanceOf(EstadoCanceladaHandler.class);
    }

    @Test
    @DisplayName("resolve(completada) retorna un EstadoCompletadaHandler")
    void resolve_completada_retornaHandlerCorrecto() {
        EstadoCitaHandler handler = resolver.resolve(EstadoCita.completada);

        assertThat(handler).isInstanceOf(EstadoCompletadaHandler.class);
    }

    // -----------------------------------------------------------------------
    // Cada handler retorna el estado que lo identifica
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("resolve(programada).getEstado() == programada")
    void resolve_programada_getEstadoCoincide() {
        assertThat(resolver.resolve(EstadoCita.programada).getEstado())
                .isEqualTo(EstadoCita.programada);
    }

    @Test
    @DisplayName("resolve(cancelada).getEstado() == cancelada")
    void resolve_cancelada_getEstadoCoincide() {
        assertThat(resolver.resolve(EstadoCita.cancelada).getEstado())
                .isEqualTo(EstadoCita.cancelada);
    }

    @Test
    @DisplayName("resolve(completada).getEstado() == completada")
    void resolve_completada_getEstadoCoincide() {
        assertThat(resolver.resolve(EstadoCita.completada).getEstado())
                .isEqualTo(EstadoCita.completada);
    }

    // -----------------------------------------------------------------------
    // Estado sin handler registrado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("resolve() lanza IllegalStateException si no hay handler para el estado")
    void resolve_sinHandler_lanzaIllegalStateException() {
        // Resolver vacío — simula que se agregó un enum sin su handler
        CitaEstadoResolver resolverVacio = new CitaEstadoResolver(List.of());

        assertThatThrownBy(() -> resolverVacio.resolve(EstadoCita.programada))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("programada");
    }

    @Test
    @DisplayName("resolve() con lista parcial lanza excepción solo para el estado faltante")
    void resolve_listaParcial_lanzaSoloParaFaltante() {
        // Solo tiene el handler de programada
        CitaEstadoResolver parcial = new CitaEstadoResolver(
                List.of(new EstadoProgramadaHandler())
        );

        // El estado registrado resuelve bien
        assertThat(parcial.resolve(EstadoCita.programada)).isNotNull();

        // Los no registrados lanzan excepción
        assertThatThrownBy(() -> parcial.resolve(EstadoCita.cancelada))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> parcial.resolve(EstadoCita.completada))
                .isInstanceOf(IllegalStateException.class);
    }

    // -----------------------------------------------------------------------
    // Consistencia: el mismo handler siempre resuelve al mismo objeto
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("resolve() retorna la misma instancia en llamadas sucesivas")
    void resolve_mismaClave_mismInstancia() {
        EstadoCitaHandler h1 = resolver.resolve(EstadoCita.programada);
        EstadoCitaHandler h2 = resolver.resolve(EstadoCita.programada);

        assertThat(h1).isSameAs(h2);
    }
}
