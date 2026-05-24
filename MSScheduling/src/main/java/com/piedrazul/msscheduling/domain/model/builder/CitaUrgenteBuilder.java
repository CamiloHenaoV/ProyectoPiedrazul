package com.piedrazul.msscheduling.domain.model.builder;

import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;

import java.time.ZonedDateTime;

public class CitaUrgenteBuilder extends CitaBuilder {

    @Override
    public void buildPaciente(UsuarioLocal paciente) {
        cita.setPacienteId(paciente.getId());
        cita.setPacienteNombre(paciente.getNombreCompleto());
    }

    @Override
    public void buildProfesional(UsuarioLocal profesional) {
        cita.setProfesionalId(profesional.getId());
        cita.setProfesionalNombre(profesional.getNombreCompleto());
    }

    @Override
    public void buildFechaHora(ZonedDateTime fechaHora) {
        cita.setFechaHora(fechaHora);
    }

    @Override
    public void buildDuracion(int duracionMinutos) {
        cita.setDuracionMinutos(duracionMinutos);
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
