package com.piedrazul.msscheduling.domain.model.exceptions;

public class HorarioOcupadoException extends RuntimeException {
    public HorarioOcupadoException() {
        super("El profesional no está disponible en ese horario");
    }
}
