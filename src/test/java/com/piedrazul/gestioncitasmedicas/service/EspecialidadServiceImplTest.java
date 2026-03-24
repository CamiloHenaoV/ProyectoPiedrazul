package com.piedrazul.gestioncitasmedicas.service;

import com.piedrazul.gestioncitasmedicas.model.entities.Especialidad;
import com.piedrazul.gestioncitasmedicas.model.repositories.EspecialidadRepository;
import com.piedrazul.gestioncitasmedicas.model.services.impl.EspecialidadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EspecialidadServiceImplTest {

    @Mock
    private EspecialidadRepository especialidadRepository;

    @InjectMocks
    private EspecialidadServiceImpl especialidadService;

    @Test
    void shouldListSpecialtyNamesSorted() {
        when(especialidadRepository.findAll()).thenReturn(List.of(
                Especialidad.builder().nombre("Pediatría").build(),
                Especialidad.builder().nombre("Cardiología").build(),
                Especialidad.builder().nombre("Dermatología").build()
        ));

        List<String> result = especialidadService.listarNombres();

        assertEquals(3, result.size());
        assertEquals(List.of("Cardiología", "Dermatología", "Pediatría"), result);
    }

    @Test
    void shouldReturnEmptyListWhenNoSpecialtiesExist() {
        when(especialidadRepository.findAll()).thenReturn(List.of());

        List<String> result = especialidadService.listarNombres();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}