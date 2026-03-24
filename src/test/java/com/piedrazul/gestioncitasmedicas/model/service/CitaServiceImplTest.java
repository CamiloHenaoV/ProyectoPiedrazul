package com.piedrazul.gestioncitasmedicas.model.service;

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

import java.time.*;
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
    void shouldThrowWhenTryingToScheduleInThePast() {
        UUID pacienteId = UUID.randomUUID();
        Integer profesionalId = 1;
        ZonedDateTime fechaHoraPasada = ZonedDateTime.now().minusDays(1);

        CitaDTO dto = CitaDTO.builder()
                .pacienteId(pacienteId)
                .profesionalId(profesionalId)
                .fechaHora(fechaHoraPasada)
                .build();

        assertThrows(RuntimeException.class, () -> citaService.agendarCita(dto));
    }

    @Test
    void shouldScheduleCitaInFuture() {
        UUID pacienteId = UUID.randomUUID();
        Integer profesionalId = 1;
        ZonedDateTime fechaHora = ZonedDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

        Paciente paciente = Paciente.builder()
                .id(pacienteId)
                .nombreCompleto("Paciente Uno")
                .build();

        Profesional profesional = Profesional.builder()
                .id(profesionalId)
                .usuario(Usuario.builder().nombreCompleto("Dr. Juan").build())
                .tipo(TipoProfesional.medico)
                .build();

        int diaSemana = fechaHora.getDayOfWeek().getValue() % 7;

        DisponibilidadSemanal disponibilidad = DisponibilidadSemanal.builder()
                .diaSemana(diaSemana)
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
        when(disponibilidadRepository.findByProfesionalIdAndDiaSemana(profesionalId, diaSemana))
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
        assertEquals(EstadoCita.programada, result.getEstado());
        assertEquals(pacienteId, result.getPacienteId());
        assertEquals(profesionalId, result.getProfesionalId());
        verify(eventBus).publish(eq(AppEvent.CITA_AGENDADA), any());
    }

    @Test
    void shouldThrowWhenHorarioIsOccupied() {
        UUID pacienteId = UUID.randomUUID();
        Integer profesionalId = 1;
        ZonedDateTime fechaHora = ZonedDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

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
    void shouldThrowWhenCitaNotFoundOnCancel() {
        UUID citaId = UUID.randomUUID();
        when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

        assertThrows(CitaNoEncontradaException.class, () -> citaService.cancelarCita(citaId));
    }
}