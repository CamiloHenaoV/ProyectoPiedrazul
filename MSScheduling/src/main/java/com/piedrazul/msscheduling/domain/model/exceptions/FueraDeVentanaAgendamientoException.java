package com.piedrazul.msscheduling.domain.model.exceptions;

/**
 * Se lanza cuando el paciente intenta reservar una cita fuera de la ventana
 * de tiempo de agendamiento configurada por el administrador.
 *
 * HU-1.7 SC-2: el sistema bloquea la selección de fechas fuera del rango permitido.
 */
public class FueraDeVentanaAgendamientoException extends RuntimeException {
    public FueraDeVentanaAgendamientoException(int semanasHabilitadas) {
        super("Solo se pueden agendar citas dentro de las próximas "
              + semanasHabilitadas + " semana(s) habilitadas.");
    }
}
