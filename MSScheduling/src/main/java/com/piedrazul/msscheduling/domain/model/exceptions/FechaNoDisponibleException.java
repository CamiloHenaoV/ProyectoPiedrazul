package com.piedrazul.msscheduling.domain.model.exceptions;

/**
 * Se lanza cuando se intenta agendar una cita en un día marcado como
 * no disponible o festivo.
 *
 * HU-1.8 SC-1/SC-2: el sistema bloquea el agendamiento en esa fecha.
 */
public class FechaNoDisponibleException extends RuntimeException {
    public FechaNoDisponibleException(String fecha) {
        super("La fecha " + fecha + " está marcada como no disponible o festivo.");
    }
}
