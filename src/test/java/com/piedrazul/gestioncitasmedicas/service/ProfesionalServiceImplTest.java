package com.piedrazul.gestioncitasmedicas.service;

import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Especialidad;
import com.piedrazul.gestioncitasmedicas.model.entities.Profesional;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import com.piedrazul.gestioncitasmedicas.model.repositories.ProfesionalRepository;
import com.piedrazul.gestioncitasmedicas.model.services.impl.ProfesionalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfesionalServiceImplTest {

    @Mock
    private ProfesionalRepository profesionalRepository;

    @InjectMocks
    private ProfesionalServiceImpl profesionalService;

    @Test
    void shouldListActiveProfessionals() {
        Usuario usuario = Usuario.builder()
                .nombreCompleto("Dr. Juan Pérez")
                .build();

        Especialidad especialidad = Especialidad.builder()
                .nombre("Cardiología")
                .build();

        Profesional profesional = Profesional.builder()
                .id(1)
                .usuario(usuario)
                .tipo(TipoProfesional.medico)
                .especialidad(especialidad)
                .licenciaProfesional("LIC-001")
                .activo(true)
                .build();

        when(profesionalRepository.findByActivoTrue()).thenReturn(List.of(profesional));

        List<ProfesionalDTO> result = profesionalService.listarActivos();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Dr. Juan Pérez", result.get(0).getNombreCompleto());
        assertEquals("Cardiología", result.get(0).getEspecialidadNombre());
        assertEquals("LIC-001", result.get(0).getLicenciaProfesional());
        assertTrue(result.get(0).getActivo());

        verify(profesionalRepository).findByActivoTrue();
    }

    @Test
    void shouldFindProfessionalById() {
        Usuario usuario = Usuario.builder()
                .nombreCompleto("Dr. Juan Pérez")
                .build();

        Profesional profesional = Profesional.builder()
                .id(1)
                .usuario(usuario)
                .tipo(TipoProfesional.medico)
                .licenciaProfesional("LIC-001")
                .activo(true)
                .build();

        when(profesionalRepository.findById(1)).thenReturn(Optional.of(profesional));

        ProfesionalDTO result = profesionalService.buscarPorId(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Dr. Juan Pérez", result.getNombreCompleto());
        assertEquals("LIC-001", result.getLicenciaProfesional());

        verify(profesionalRepository).findById(1);
    }

    @Test
    void shouldListActiveProfessionalsByEspecialidad() {
        Usuario usuario = Usuario.builder()
                .nombreCompleto("Dra. Ana López")
                .build();

        Especialidad especialidad = Especialidad.builder()
                .nombre("Pediatría")
                .build();

        Profesional profesional = Profesional.builder()
                .id(2)
                .usuario(usuario)
                .tipo(TipoProfesional.medico)
                .especialidad(especialidad)
                .licenciaProfesional("LIC-002")
                .activo(true)
                .build();

        when(profesionalRepository.findByEspecialidadNombreAndActivoTrue("Pediatría"))
                .thenReturn(List.of(profesional));

        List<ProfesionalDTO> result = profesionalService.listarActivosPorEspecialidad("Pediatría");

        assertEquals(1, result.size());
        assertEquals("Dra. Ana López", result.get(0).getNombreCompleto());
        assertEquals("Pediatría", result.get(0).getEspecialidadNombre());

        verify(profesionalRepository).findByEspecialidadNombreAndActivoTrue("Pediatría");
    }
}