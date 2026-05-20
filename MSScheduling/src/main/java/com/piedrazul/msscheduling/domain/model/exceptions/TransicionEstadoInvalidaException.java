package com.piedrazul.msscheduling.domain.model.exceptions;

import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;

/**
 * Excepción de dominio lanzada cuando se intenta realizar una transición
 * de estado inválida sobre una {@code Cita}.
 *
 * <p>Ejemplos de transiciones inválidas:
 * <ul>
 *   <li>Cancelar una cita que ya está {@code cancelada}.</li>
 *   <li>Completar una cita que ya está {@code completada}.</li>
 *   <li>Cancelar o completar una cita en estado {@code completada} o {@code cancelada}.</li>
 * </ul>
 *
 * El {@link com.piedrazul.msscheduling.infra.exception.GlobalExceptionHandler}
 * la mapea a HTTP 422 (Unprocessable Entity).
 */
public class TransicionEstadoInvalidaException extends RuntimeException {

    private final EstadoCita estadoActual;
    private final EstadoCita transicionIntentada;

    public TransicionEstadoInvalidaException(EstadoCita estadoActual,
                                             EstadoCita transicionIntentada) {
        super(String.format(
                "Transición inválida: una cita en estado '%s' no puede pasar a '%s'.",
                estadoActual.name(), transicionIntentada.name()
        ));
        this.estadoActual         = estadoActual;
        this.transicionIntentada  = transicionIntentada;
    }

    public EstadoCita getEstadoActual()        { return estadoActual; }
    public EstadoCita getTransicionIntentada() { return transicionIntentada; }
}
