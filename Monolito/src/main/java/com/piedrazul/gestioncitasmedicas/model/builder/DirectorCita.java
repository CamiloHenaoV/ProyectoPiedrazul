package com.piedrazul.gestioncitasmedicas.model.builder;

import com.piedrazul.gestioncitasmedicas.model.entities.Cita;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;

import java.time.ZonedDateTime;

public class DirectorCita {

    private CitaBuilder citaBuilder;

    public void setCitaBuilder(CitaBuilder citaBuilder) {
        this.citaBuilder = citaBuilder;
    }

    public Cita getCita() {
        return citaBuilder.getCita();
    }

    public void construirCita(Paciente paciente, Profesional profesional, ZonedDateTime fechaHora) {
        citaBuilder.iniciarNuevaCita();
        citaBuilder.buildPaciente(paciente);
        citaBuilder.buildProfesional(profesional);
        citaBuilder.buildFechaHora(fechaHora);
        citaBuilder.buildEstado(EstadoCita.programada);
        citaBuilder.buildFechaCreacion();
    }
}
