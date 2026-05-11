package com.piedrazul.gestioncitasmedicas.model.builder;

import com.piedrazul.gestioncitasmedicas.model.entities.Cita;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CitaBuilderTest {

    private Paciente paciente;
    private Profesional profesional;
    private ZonedDateTime fechaHora;

    @BeforeEach
    void setUp() {
        paciente = Paciente.builder()
                .id(UUID.randomUUID())
                .nombreCompleto("Ana García")
                .build();

        profesional = Profesional.builder()
                .id(1)
                .usuario(Usuario.builder().nombreCompleto("Dr. Carlos López").build())
                .tipo(TipoProfesional.medico)
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
        assertEquals(paciente, cita.getPaciente());
        assertEquals(profesional, cita.getProfesional());
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
        director.construirCita(paciente, profesional, fechaHora);

        Cita cita = director.getCita();

        assertNotNull(cita);
        assertEquals(paciente, cita.getPaciente());
        assertEquals(profesional, cita.getProfesional());
        assertEquals(fechaHora, cita.getFechaHora());
        assertEquals(EstadoCita.programada, cita.getEstado());
        assertNotNull(cita.getCreadoEn());
    }

    @Test
    void directorConCitaUrgenteBuilderDebeProducirCitaCompleta() {
        DirectorCita director = new DirectorCita();
        director.setCitaBuilder(new CitaUrgenteBuilder());
        director.construirCita(paciente, profesional, fechaHora);

        Cita cita = director.getCita();

        assertNotNull(cita);
        assertEquals(paciente, cita.getPaciente());
        assertEquals(profesional, cita.getProfesional());
        assertEquals(fechaHora, cita.getFechaHora());
        assertEquals(EstadoCita.programada, cita.getEstado());
        assertNotNull(cita.getCreadoEn());
    }

    @Test
    void directorDebePermitirCambiarBuilderEnTiempoDeEjecucion() {
        DirectorCita director = new DirectorCita();

        director.setCitaBuilder(new CitaProgramadaBuilder());
        director.construirCita(paciente, profesional, fechaHora);
        Cita citaProgramada = director.getCita();

        director.setCitaBuilder(new CitaUrgenteBuilder());
        director.construirCita(paciente, profesional, fechaHora);
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
