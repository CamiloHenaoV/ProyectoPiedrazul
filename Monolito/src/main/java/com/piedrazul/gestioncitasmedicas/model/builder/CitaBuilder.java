package com.piedrazul.gestioncitasmedicas.model.builder;

import com.piedrazul.gestioncitasmedicas.model.entities.Cita;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;

import java.time.ZonedDateTime;

public abstract class CitaBuilder {

    protected Cita cita;

    public void iniciarNuevaCita() {
        cita = new Cita();
    }

    public Cita getCita() {
        return cita;
    }

    public abstract void buildPaciente(Paciente paciente);

    public abstract void buildProfesional(Profesional profesional);

    public abstract void buildFechaHora(ZonedDateTime fechaHora);

    public abstract void buildEstado(EstadoCita estado);

    public abstract void buildFechaCreacion();
}
