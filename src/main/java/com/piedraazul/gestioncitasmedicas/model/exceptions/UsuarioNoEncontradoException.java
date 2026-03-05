package com.piedraazul.gestioncitasmedicas.model.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String login) {
        super("No se encontró el usuario: " + login);
    }
}