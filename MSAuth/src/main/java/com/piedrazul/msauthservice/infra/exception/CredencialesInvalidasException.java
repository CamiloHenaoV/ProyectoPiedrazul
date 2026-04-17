package com.piedrazul.msauthservice.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando el login o la contraseña no coinciden,
 * o el usuario está inactivo.
 * Siempre devuelve 401 con el mismo mensaje genérico
 * para no revelar si el login existe o no.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
