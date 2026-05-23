package com.piedrazul.msscheduling.domain.model.builder;

import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CitaBuilderTest {

    private UsuarioLocal paciente;
    private UsuarioLocal profesional;
    private ZonedDateTime fechaHora;

    @BeforeEach
    void setUp() {
        paciente = UsuarioLocal.builder()
                .id(1L)
                .nombreCompleto("Ana García")
                .login("ana.garcia")
                .rol(RolUsuario.paciente)
                .activo(true)
                .build();

        profesional = UsuarioLocal.builder()
                .id(2L)
                .nombreCompleto("Dr. Carlos López")
                .login("carlos.lopez")
                .rol(RolUsuario.profesional)
                .activo(true)
                .build();

        fechaHora = ZonedDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    @Test
    void citaProgramadaBuilderDebeAsignarTodosLosCampos() {
        CitaProgramadaBuilder builder = new CitaProgramadaBuilder();
        builder.iniciarNuevaCita();
        builder.buildPaciente(paciente);
        builder.buildProfesional(profesional);
        builder.buildFechaHora(fechaHora);
        builder.buildEstado(EstadoCita.programada);
        builder.buildFechaCreacion();

        Cita cita = builder.getCita();

        assertNotNull(cita);
        assertEquals(1L, cita.getPacienteId());
        assertEquals("Ana García", cita.getPacienteNombre());
        assertEquals(2L, cita.getProfesionalId());
        assertEquals("Dr. Carlos López", cita.getProfesionalNombre());
        assertEquals(fechaHora, cita.getFechaHora());
        assertEquals(EstadoCita.programada, cita.getEstado());
        assertNotNull(cita.getCreadoEn());
    }

    @Test
    void citaUrgenteBuilderDebeAsignarEstadoProgramada() {
        CitaUrgenteBuilder builder = new CitaUrgenteBuilder();
        builder.iniciarNuevaCita();
        builder.buildPaciente(paciente);
        builder.buildProfesional(profesional);
        builder.buildFechaHora(fechaHora);
        builder.buildEstado(EstadoCita.programada);
        builder.buildFechaCreacion();

        Cita cita = builder.getCita();

        assertNotNull(cita);
        assertEquals(EstadoCita.programada, cita.getEstado());
        assertNotNull(cita.getCreadoEn());
    }

    @Test
    void directorConCitaProgramadaBuilderDebeProducirCitaCompleta() {
        DirectorCita director = new DirectorCita();
        director.setCitaBuilder(new CitaProgramadaBuilder());
        director.construirCita(paciente, profesional, fechaHora,5);

        Cita cita = director.getCita();

        assertNotNull(cita);
        assertEquals(1L, cita.getPacienteId());
        assertEquals("Ana García", cita.getPacienteNombre());
        assertEquals(2L, cita.getProfesionalId());
        assertEquals("Dr. Carlos López", cita.getProfesionalNombre());
        assertEquals(fechaHora, cita.getFechaHora());
        assertEquals(EstadoCita.programada, cita.getEstado());
        assertNotNull(cita.getCreadoEn());
    }

    @Test
    void directorConCitaUrgenteBuilderDebeProducirCitaCompleta() {
        DirectorCita director = new DirectorCita();
        director.setCitaBuilder(new CitaUrgenteBuilder());
        director.construirCita(paciente, profesional, fechaHora,5);

        Cita cita = director.getCita();

        assertNotNull(cita);
        assertEquals(1L, cita.getPacienteId());
        assertEquals(2L, cita.getProfesionalId());
        assertEquals(EstadoCita.programada, cita.getEstado());
        assertNotNull(cita.getCreadoEn());
    }

    @Test
    void directorDebePermitirCambiarBuilderEnTiempoDeEjecucion() {
        DirectorCita director = new DirectorCita();

        director.setCitaBuilder(new CitaProgramadaBuilder());
        director.construirCita(paciente, profesional, fechaHora,5);
        Cita citaProgramada = director.getCita();

        director.setCitaBuilder(new CitaUrgenteBuilder());
        director.construirCita(paciente, profesional, fechaHora,5);
        Cita citaUrgente = director.getCita();

        assertNotSame(citaProgramada, citaUrgente);
        assertEquals(citaProgramada.getEstado(), citaUrgente.getEstado());
    }

    @Test
    void iniciarNuevaCitaDebeReemplazarInstanciaAnterior() {
        CitaProgramadaBuilder builder = new CitaProgramadaBuilder();

        builder.iniciarNuevaCita();
        Cita primera = builder.getCita();

        builder.iniciarNuevaCita();
        Cita segunda = builder.getCita();

        assertNotSame(primera, segunda);
    }
}
