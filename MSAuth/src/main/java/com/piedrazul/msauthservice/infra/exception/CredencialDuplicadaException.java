package com.piedrazul.msauthservice.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CredencialDuplicadaException extends RuntimeException {
    public CredencialDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
