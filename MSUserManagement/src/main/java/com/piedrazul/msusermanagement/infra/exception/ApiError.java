package com.piedrazul.msusermanagement.infra.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class ApiError {

    private final int status;
    private final String error;
    private final String mensaje;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final ZonedDateTime timestamp;

    public ApiError(int status, String error, String mensaje) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = ZonedDateTime.now();
    }
}