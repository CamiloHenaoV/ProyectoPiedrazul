package com.piedrazul.gestioncitasmedicas.model.exceptions;

public class PasswordInvalidaException extends RuntimeException {
    public PasswordInvalidaException(String mensaje) {
        super(mensaje);
    }
}
