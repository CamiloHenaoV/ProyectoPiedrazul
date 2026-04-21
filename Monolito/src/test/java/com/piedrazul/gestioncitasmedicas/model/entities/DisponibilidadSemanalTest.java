package com.piedrazul.gestioncitasmedicas.model.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadSemanalTest {

    @Test
    void shouldBuildDisponibilidadSemanalWithoutDuration() {
        DisponibilidadSemanal disponibilidad = DisponibilidadSemanal.builder()
                .diaSemana(1)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0))
                .build();

        assertNotNull(disponibilidad);
        assertEquals(1, disponibilidad.getDiaSemana());
        assertEquals(LocalTime.of(8, 0), disponibilidad.getHoraInicio());
        assertEquals(LocalTime.of(12, 0), disponibilidad.getHoraFin());
        assertNull(disponibilidad.getDuracionCitaMinutos());
    }
}