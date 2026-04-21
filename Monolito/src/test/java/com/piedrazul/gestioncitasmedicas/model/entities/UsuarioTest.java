package com.piedrazul.gestioncitasmedicas.model.entities;

import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void shouldBuildUsuarioCorrectly() {
        UUID id = UUID.randomUUID();
        ZonedDateTime creadoEn = ZonedDateTime.now();

        Usuario usuario = Usuario.builder()
                .id(id)
                .login("juanp")
                .passwordHash("hash123")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.paciente)
                .activo(true)
                .creadoEn(creadoEn)
                .build();

        assertNotNull(usuario);
        assertEquals(id, usuario.getId());
        assertEquals("juanp", usuario.getLogin());
        assertEquals("hash123", usuario.getPasswordHash());
        assertEquals("Juan Pérez", usuario.getNombreCompleto());
        assertEquals(RolUsuario.paciente, usuario.getRol());
        assertTrue(usuario.getActivo());
        assertEquals(creadoEn, usuario.getCreadoEn());
    }

    @Test
    void shouldAllowSettingAndGettingRelations() {
        Usuario usuario = new Usuario();

        Profesional profesional = new Profesional();
        Paciente paciente = new Paciente();
        Responsable responsable = new Responsable();

        usuario.setProfesional(profesional);
        usuario.setPaciente(paciente);
        usuario.setResponsable(responsable);

        assertEquals(profesional, usuario.getProfesional());
        assertEquals(paciente, usuario.getPaciente());
        assertEquals(responsable, usuario.getResponsable());
    }
}