package com.piedrazul.msnotifications.domain.model.exceptions;

public class NotificacionNoEncontradaException extends RuntimeException {

    public NotificacionNoEncontradaException(String id) {
        super("Notificación no encontrada con id: " + id);
    }
}
