package com.piedrazul.msscheduling.domain.model.builder;

import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;

import java.time.ZonedDateTime;

public abstract class CitaBuilder {

    protected Cita cita;

    public void iniciarNuevaCita() {
        cita = new Cita();
    }

    public Cita getCita() {
        return cita;
    }

    public abstract void buildPaciente(UsuarioLocal paciente);

    public abstract void buildProfesional(UsuarioLocal profesional);

    public abstract void buildFechaHora(ZonedDateTime fechaHora);

    public abstract void buildEstado(EstadoCita estado);

    public abstract void buildFechaCreacion();
}
