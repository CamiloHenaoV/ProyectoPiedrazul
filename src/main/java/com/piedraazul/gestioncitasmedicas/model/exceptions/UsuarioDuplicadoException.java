package com.piedraazul.gestioncitasmedicas.model.exceptions;

public class UsuarioDuplicadoException extends RuntimeException {
    public UsuarioDuplicadoException(String usuario) {
        super("El Usuario '" + usuario + "' ya está en uso");
    }
}
