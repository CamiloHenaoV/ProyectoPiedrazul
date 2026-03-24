package com.piedrazul.gestioncitasmedicas.service;

import com.piedrazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.*;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import com.piedrazul.gestioncitasmedicas.model.exceptions.CitaNoEncontradaException;
import com.piedrazul.gestioncitasmedicas.model.exceptions.HorarioOcupadoException;
import com.piedrazul.gestioncitasmedicas.model.repositories.*;
import com.piedrazul.gestioncitasmedicas.model.services.impl.CitaServiceImpl;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceImplTest {

    @Mock private CitaRepository citaRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfesionalRepository profesionalRepository;
    @Mock private DisponibilidadSemanalRepository disponibilidadRepository;
    @Mock private BloqueoDisponibilidadRepository bloqueoRepository;
    @Mock private EventBus eventBus;

    @InjectMocks
    private CitaServiceImpl citaService;

    @Test
    void shouldScheduleCita() {
        UUID pacienteId = UUID.randomUUID();
        Integer profesionalId = 1;
        ZonedDateTime fechaHora = ZonedDateTime.of(2026, 3, 24, 9, 0, 0, 0, ZoneId.systemDefault());

        Paciente paciente = Paciente.builder()
                .id(pacienteId)
                .nombreCompleto("Paciente Uno")
                .build();

        Profesional profesional = Profesional.builder()
                .id(profesionalId)
                .usuario(Usuario.builder().nombreCompleto("Dr. Juan").build())
                .tipo(TipoProfesional.medico)
                .build();

        DisponibilidadSemanal disponibilidad = DisponibilidadSemanal.builder()
                .diaSemana(fechaHora.getDayOfWeek().getValue() % 7)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(14, 0))
                .duracionCitaMinutos(30)
                .build();

        CitaDTO dto = CitaDTO.builder()
                .pacienteId(pacienteId)
                .profesionalId(profesionalId)
                .fechaHora(fechaHora)
                .build();

        when(citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, fechaHora)).thenReturn(false);
        when(bloqueoRepository.existeBloqueoEnFecha(profesionalId, fechaHora)).thenReturn(false);
        when(disponibilidadRepository.findByProfesionalIdAndDiaSemana(profesionalId, fechaHora.getDayOfWeek().getValue() % 7))
                .thenReturn(List.of(disponibilidad));
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.of(profesional));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> {
            Cita cita = invocation.getArgument(0);
            cita.setId(UUID.randomUUID());
            return cita;
        });

        CitaDTO result = citaService.agendarCita(dto);

        assertNotNull(result);
        assertEquals(pacienteId, result.getPacienteId());
        assertEquals(profesionalId, result.getProfesionalId());
        assertEquals(EstadoCita.programada, result.getEstado());
        verify(eventBus).publish(eq(AppEvent.CITA_AGENDADA), any());
    }

    @Test
    void shouldThrowWhenHorarioIsOccupied() {
        UUID pacienteId = UUID.randomUUID();
        Integer profesionalId = 1;
        ZonedDateTime fechaHora = ZonedDateTime.now();

        CitaDTO dto = CitaDTO.builder()
                .pacienteId(pacienteId)
                .profesionalId(profesionalId)
                .fechaHora(fechaHora)
                .build();

        when(citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, fechaHora)).thenReturn(true);

        assertThrows(HorarioOcupadoException.class, () -> citaService.agendarCita(dto));
    }

    @Test
    void shouldCancelCita() {
        UUID citaId = UUID.randomUUID();

        Cita cita = Cita.builder()
                .id(citaId)
                .estado(EstadoCita.programada)
                .paciente(Paciente.builder().id(UUID.randomUUID()).nombreCompleto("Paciente Uno").build())
                .profesional(Profesional.builder().id(1).usuario(Usuario.builder().nombreCompleto("Dr. Juan").build()).build())
                .build();

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaDTO result = citaService.cancelarCita(citaId);

        assertEquals(EstadoCita.cancelada, result.getEstado());
        verify(eventBus).publish(eq(AppEvent.CITA_CANCELADA), any());
    }

    @Test
    void shouldCompleteCita() {
        UUID citaId = UUID.randomUUID();

        Cita cita = Cita.builder()
                .id(citaId)
                .estado(EstadoCita.programada)
                .paciente(Paciente.builder().id(UUID.randomUUID()).nombreCompleto("Paciente Uno").build())
                .profesional(Profesional.builder().id(1).usuario(Usuario.builder().nombreCompleto("Dr. Juan").build()).build())
                .build();

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaDTO result = citaService.completarCita(citaId);

        assertEquals(EstadoCita.completada, result.getEstado());
        verify(eventBus).publish(eq(AppEvent.CITA_COMPLETADA), any());
    }

    @Test
    void shouldListHorariosDisponibles() {
        Integer profesionalId = 1;
        LocalDate fecha = LocalDate.of(2026, 3, 24);

        DisponibilidadSemanal disponibilidad = DisponibilidadSemanal.builder()
                .diaSemana(fecha.getDayOfWeek().getValue() % 7)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(9, 0))
                .duracionCitaMinutos(30)
                .build();

        when(disponibilidadRepository.findByProfesionalIdAndDiaSemana(profesionalId, fecha.getDayOfWeek().getValue() % 7))
                .thenReturn(List.of(disponibilidad));
        when(citaRepository.existsByProfesionalIdAndFechaHora(eq(profesionalId), any())).thenReturn(false);
        when(bloqueoRepository.existeBloqueoEnFecha(eq(profesionalId), any())).thenReturn(false);

        List<ZonedDateTime> result = citaService.obtenerHorariosDisponibles(profesionalId, fecha);

        assertEquals(2, result.size());
    }

    @Test
    void shouldThrowWhenCitaNotFoundOnCancel() {
        UUID citaId = UUID.randomUUID();
        when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

        assertThrows(CitaNoEncontradaException.class, () -> citaService.cancelarCita(citaId));
    }
}