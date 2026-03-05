package com.piedraazul.gestioncitasmedicas.model.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String usuario) {
        super("No se encontró el usuario: " + usuario);
    }
}
