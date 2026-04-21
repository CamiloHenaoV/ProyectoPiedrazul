package com.piedrazul.gestioncitasmedicas.model.entities;

import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfesionalTest {

    @Test
    void shouldBuildProfesionalCorrectly() {
        Profesional profesional = Profesional.builder()
                .id(1)
                .tipo(TipoProfesional.medico)
                .licenciaProfesional("LIC-001")
                .activo(true)
                .build();

        assertNotNull(profesional);
        assertEquals(1, profesional.getId());
        assertEquals(TipoProfesional.medico, profesional.getTipo());
        assertEquals("LIC-001", profesional.getLicenciaProfesional());
        assertTrue(profesional.getActivo());
    }

    @Test
    void shouldAllowSettingRelationsAndCollections() {
        Profesional profesional = new Profesional();
        Usuario usuario = new Usuario();
        Especialidad especialidad = new Especialidad();
        List<DisponibilidadSemanal> disponibilidades = new ArrayList<>();
        List<BloqueoDisponibilidad> bloqueos = new ArrayList<>();

        profesional.setUsuario(usuario);
        profesional.setEspecialidad(especialidad);
        profesional.setDisponibilidades(disponibilidades);
        profesional.setBloqueos(bloqueos);

        assertEquals(usuario, profesional.getUsuario());
        assertEquals(especialidad, profesional.getEspecialidad());
        assertEquals(disponibilidades, profesional.getDisponibilidades());
        assertEquals(bloqueos, profesional.getBloqueos());
    }
}