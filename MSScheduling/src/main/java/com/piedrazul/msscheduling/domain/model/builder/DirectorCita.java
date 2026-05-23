package com.piedrazul.msscheduling.domain.model.builder;

import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;

import java.time.ZonedDateTime;

public class DirectorCita {

    private CitaBuilder citaBuilder;

    public void setCitaBuilder(CitaBuilder citaBuilder) {
        this.citaBuilder = citaBuilder;
    }

    public Cita getCita() {
        return citaBuilder.getCita();
    }

    public void construirCita(UsuarioLocal paciente, UsuarioLocal profesional,
                              ZonedDateTime fechaHora, int duracionMinutos) {
        citaBuilder.iniciarNuevaCita();
        citaBuilder.buildPaciente(paciente);
        citaBuilder.buildProfesional(profesional);
        citaBuilder.buildFechaHora(fechaHora);
        citaBuilder.buildDuracion(duracionMinutos);
        citaBuilder.buildEstado(EstadoCita.programada);
        citaBuilder.buildFechaCreacion();
    }
}
