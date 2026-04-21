package com.piedrazul.gestioncitasmedicas.model.exceptions;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos");
    }
}
