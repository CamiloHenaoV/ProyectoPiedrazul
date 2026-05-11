package com.piedrazul.gestioncitasmedicas.model.builder;

import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;

import java.time.ZonedDateTime;

public class CitaUrgenteBuilder extends CitaBuilder {

    @Override
    public void buildPaciente(Paciente paciente) {
        cita.setPaciente(paciente);
    }

    @Override
    public void buildProfesional(Profesional profesional) {
        cita.setProfesional(profesional);
    }

    @Override
    public void buildFechaHora(ZonedDateTime fechaHora) {
        cita.setFechaHora(fechaHora);
    }

    @Override
    public void buildEstado(EstadoCita estado) {
        cita.setEstado(EstadoCita.programada);
    }

    @Override
    public void buildFechaCreacion() {
        cita.setCreadoEn(ZonedDateTime.now());
    }
}
