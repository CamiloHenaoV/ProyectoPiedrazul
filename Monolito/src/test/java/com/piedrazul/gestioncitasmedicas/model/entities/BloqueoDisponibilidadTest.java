package com.piedrazul.gestioncitasmedicas.model.entities;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BloqueoDisponibilidadTest {

    @Test
    void shouldBuildBloqueoDisponibilidadCorrectly() {
        UUID id = UUID.randomUUID();
        ZonedDateTime inicio = ZonedDateTime.now();
        ZonedDateTime fin = inicio.plusHours(2);

        BloqueoDisponibilidad bloqueo = BloqueoDisponibilidad.builder()
                .id(id)
                .fechaInicio(inicio)
                .fechaFin(fin)
                .motivo("Vacaciones")
                .build();

        assertNotNull(bloqueo);
        assertEquals(id, bloqueo.getId());
        assertEquals(inicio, bloqueo.getFechaInicio());
        assertEquals(fin, bloqueo.getFechaFin());
        assertEquals("Vacaciones", bloqueo.getMotivo());
    }

    @Test
    void shouldAllowSettingProfesional() {
        BloqueoDisponibilidad bloqueo = new BloqueoDisponibilidad();
        Profesional profesional = new Profesional();

        bloqueo.setProfesional(profesional);

        assertEquals(profesional, bloqueo.getProfesional());
    }
}