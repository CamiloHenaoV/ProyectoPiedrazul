package com.piedrazul.frontend.http;

/**
 * Excepción para errores HTTP recibidos del API Gateway.
 *
 * Reemplaza las excepciones de dominio del monolito
 * (CredencialesInvalidasException, LoginDuplicadoException, etc.)
 * que ahora llegan como códigos HTTP.
 */
public class HttpException extends RuntimeException {

    private final int statusCode;

    public HttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }

    public boolean isUnauthorized()  { return statusCode == 401; }
    public boolean isForbidden()     { return statusCode == 403; }
    public boolean isNotFound()      { return statusCode == 404; }
    public boolean isConflict()      { return statusCode == 409; }
    public boolean isServerError()   { return statusCode >= 500; }
    public boolean isUnavailable()   { return statusCode == 503; }
}
