package com.piedrazul.gestioncitasmedicas.model.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadSemanalTest {

    @Test
    void shouldBuildDisponibilidadSemanalCorrectly() {
        DisponibilidadSemanal disponibilidad = DisponibilidadSemanal.builder()
                .id(1)
                .diaSemana(1)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(12, 0))
                .duracionCitaMinutos(30)
                .build();

        assertNotNull(disponibilidad);
        assertEquals(1, disponibilidad.getId());
        assertEquals(1, disponibilidad.getDiaSemana());
        assertEquals(LocalTime.of(8, 0), disponibilidad.getHoraInicio());
        assertEquals(LocalTime.of(12, 0), disponibilidad.getHoraFin());
        assertEquals(30, disponibilidad.getDuracionCitaMinutos());
    }

    @Test
    void shouldUseDefaultDurationWhenNotProvided() {
        DisponibilidadSemanal disponibilidad = DisponibilidadSemanal.builder()
                .diaSemana(2)
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(13, 0))
                .build();

        assertEquals(30, disponibilidad.getDuracionCitaMinutos());
    }
}