package com.piedrazul.gestioncitasmedicas.model.entities;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EspecialidadTest {

    @Test
    void shouldBuildEspecialidadCorrectly() {
        Especialidad especialidad = Especialidad.builder()
                .id(1)
                .nombre("Cardiología")
                .build();

        assertNotNull(especialidad);
        assertEquals(1, especialidad.getId());
        assertEquals("Cardiología", especialidad.getNombre());
    }

    @Test
    void shouldAllowSettingProfesionales() {
        Especialidad especialidad = new Especialidad();
        Profesional profesional = new Profesional();

        especialidad.setProfesionales(new ArrayList<>());
        especialidad.getProfesionales().add(profesional);

        assertNotNull(especialidad.getProfesionales());
        assertEquals(1, especialidad.getProfesionales().size());
        assertEquals(profesional, especialidad.getProfesionales().get(0));
    }
}