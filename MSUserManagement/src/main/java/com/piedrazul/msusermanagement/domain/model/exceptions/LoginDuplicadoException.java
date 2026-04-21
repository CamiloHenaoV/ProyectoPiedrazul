package com.piedrazul.msusermanagement.domain.model.exceptions;

public class LoginDuplicadoException extends RuntimeException {
    public LoginDuplicadoException(String login) {
        super("El login '" + login + "' ya está en uso");
    }
}