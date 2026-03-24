package com.piedrazul.gestioncitasmedicas.model.entities;

import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CitaTest {

    @Test
    void shouldBuildCitaCorrectly() {
        UUID id = UUID.randomUUID();
        ZonedDateTime fechaHora = ZonedDateTime.now();

        Cita cita = Cita.builder()
                .id(id)
                .fechaHora(fechaHora)
                .estado(EstadoCita.programada)
                .build();

        assertNotNull(cita);
        assertEquals(id, cita.getId());
        assertEquals(fechaHora, cita.getFechaHora());
        assertEquals(EstadoCita.programada, cita.getEstado());
    }

    @Test
    void shouldAllowSettingRelations() {
        Cita cita = new Cita();
        Paciente paciente = new Paciente();
        Profesional profesional = new Profesional();
        HistoriaClinica historiaClinica = new HistoriaClinica();

        cita.setPaciente(paciente);
        cita.setProfesional(profesional);
        cita.setHistoriaClinica(historiaClinica);

        assertEquals(paciente, cita.getPaciente());
        assertEquals(profesional, cita.getProfesional());
        assertEquals(historiaClinica, cita.getHistoriaClinica());
    }

    @Test
    void shouldUseDefaultEstadoWhenEntityIsCreated() {
        Cita cita = new Cita();

        assertEquals(EstadoCita.programada, cita.getEstado());
    }
}