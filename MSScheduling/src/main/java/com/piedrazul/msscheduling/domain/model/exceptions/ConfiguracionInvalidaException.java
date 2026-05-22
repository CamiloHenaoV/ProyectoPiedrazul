package com.piedrazul.msscheduling.domain.model.exceptions;

/**
 * Se lanza cuando la configuración de disponibilidad es semánticamente inválida.
 *
 * Ejemplos:
 *   - La hora de inicio es posterior a la hora de fin.
 *   - El intervalo configurado no permite ninguna cita dentro de la franja horaria.
 *   - (HU-1.6 SC-2) El intervalo ingresado es inconsistente.
 */
public class ConfiguracionInvalidaException extends RuntimeException {
    public ConfiguracionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
