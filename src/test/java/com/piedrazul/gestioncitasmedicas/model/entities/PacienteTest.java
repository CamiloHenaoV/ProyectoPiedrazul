package com.piedrazul.gestioncitasmedicas.model.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PacienteTest {

    @Test
    void shouldBuildPacienteCorrectly() {
        UUID id = UUID.randomUUID();
        LocalDate fechaNacimiento = LocalDate.of(1995, 5, 20);
        ZonedDateTime creadoEn = ZonedDateTime.now();

        Paciente paciente = Paciente.builder()
                .id(id)
                .nombreCompleto("Juan Pérez")
                .cedulaIdentidad("001-1234567-8")
                .fechaNacimiento(fechaNacimiento)
                .telefono("8091234567")
                .email("juan@example.com")
                .direccion("Santo Domingo")
                .creadoEn(creadoEn)
                .build();

        assertNotNull(paciente);
        assertEquals(id, paciente.getId());
        assertEquals("Juan Pérez", paciente.getNombreCompleto());
        assertEquals("001-1234567-8", paciente.getCedulaIdentidad());
        assertEquals(fechaNacimiento, paciente.getFechaNacimiento());
        assertEquals("8091234567", paciente.getTelefono());
        assertEquals("juan@example.com", paciente.getEmail());
        assertEquals("Santo Domingo", paciente.getDireccion());
        assertEquals(creadoEn, paciente.getCreadoEn());
    }

    @Test
    void shouldAllowSettingRelationsAndCitas() {
        Paciente paciente = new Paciente();
        Usuario usuario = new Usuario();
        Responsable responsable = new Responsable();
        List<Cita> citas = new ArrayList<>();

        paciente.setUsuario(usuario);
        paciente.setResponsable(responsable);
        paciente.setCitas(citas);

        assertEquals(usuario, paciente.getUsuario());
        assertEquals(responsable, paciente.getResponsable());
        assertEquals(citas, paciente.getCitas());
    }
}