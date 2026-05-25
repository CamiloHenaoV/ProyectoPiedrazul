package com.piedrazul.msscheduling.application.service;

import com.piedrazul.msscheduling.application.service.impl.CitaServiceImpl;
import com.piedrazul.msscheduling.application.service.interfaces.IConfiguracionAgendamientoService;
import com.piedrazul.msscheduling.application.service.interfaces.IDiaNoDisponibleService;
import com.piedrazul.msscheduling.domain.model.dto.CitaDTO;
import com.piedrazul.msscheduling.domain.model.entity.Cita;
import com.piedrazul.msscheduling.domain.model.entity.DisponibilidadSemanal;
import com.piedrazul.msscheduling.domain.model.entity.UsuarioLocal;
import com.piedrazul.msscheduling.domain.model.entity.enums.EstadoCita;
import com.piedrazul.msscheduling.domain.model.entity.enums.RolUsuario;
import com.piedrazul.msscheduling.domain.model.exceptions.*;
import com.piedrazul.msscheduling.domain.model.repository.*;
import com.piedrazul.msscheduling.domain.model.state.CitaEstadoResolver;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoCanceladaHandler;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoCompletadaHandler;
import com.piedrazul.msscheduling.domain.model.state.impl.EstadoProgramadaHandler;
import com.piedrazul.msscheduling.infra.messaging.publisher.CitaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitaServiceImpl — reglas de negocio")
class CitaServiceImplTest {

    @Mock private CitaRepository                    citaRepository;
    @Mock private DisponibilidadSemanalRepository   disponibilidadRepository;
    @Mock private BloqueoDisponibilidadRepository   bloqueoRepository;
    @Mock private UsuarioLocalRepository            usuarioLocalRepository;
    @Mock private CitaEventPublisher                citaEventPublisher;
    @Mock private IConfiguracionAgendamientoService configuracionService;
    @Mock private IDiaNoDisponibleService           diaNoDisponibleService;

    private CitaServiceImpl service;

    // Mañana a las 09:00, siempre dentro de la ventana de agendamiento
    private ZonedDateTime MANANA_9AM;
    private static final Long PACIENTE_ID     = 1L;
    private static final Long PROFESIONAL_ID  = 2L;
    private static final int  DURACION        = 30;

    @BeforeEach
    void setUp() {
        CitaEstadoResolver resolver = new CitaEstadoResolver(List.of(
                new EstadoProgramadaHandler(),
                new EstadoCanceladaHandler(),
                new EstadoCompletadaHandler()
        ));

        service = new CitaServiceImpl(
                citaRepository, disponibilidadRepository, bloqueoRepository,
                usuarioLocalRepository, citaEventPublisher, resolver,
                configuracionService, diaNoDisponibleService
        );

        MANANA_9AM = LocalDate.now().plusDays(1)
                .atTime(LocalTime.of(9, 0))
                .atZone(ZoneId.systemDefault());
    }

    // -----------------------------------------------------------------------
    // agendarCita()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("agendarCita()")
    class AgendarCitaTest {

        @Test
        @DisplayName("Agenda exitosamente y publica el evento cita.agendada")
        void agendarExitoso_publicaEvento() {
            stubDisponibilidadOk();

            CitaDTO resultado = service.agendarCita(dtoAgendar());

            assertThat(resultado.getEstado()).isEqualTo(EstadoCita.programada);
            verify(citaEventPublisher).publicarCitaAgendada(any());
        }

        @Test
        @DisplayName("Lanza FueraDeVentanaAgendamientoException cuando la fecha supera la ventana")
        void fechaFueraDeVentana_lanzaExcepcion() {
            ZonedDateTime futuroLejano = ZonedDateTime.now().plusWeeks(10);
            when(configuracionService.obtenerFechaMaximaAgendamiento())
                    .thenReturn(LocalDate.now().plusWeeks(4));

            CitaDTO dto = CitaDTO.builder()
                    .pacienteId(PACIENTE_ID).profesionalId(PROFESIONAL_ID)
                    .fechaHora(futuroLejano).build();

            when(configuracionService.obtener()).thenReturn(
                    com.piedrazul.msscheduling.domain.model.dto.ConfiguracionAgendamientoDTO.builder()
                            .semanasHabilitadas(4).build());

            assertThatThrownBy(() -> service.agendarCita(dto))
                    .isInstanceOf(FueraDeVentanaAgendamientoException.class);
        }

        @Test
        @DisplayName("Lanza FechaNoDisponibleException cuando la fecha es festivo o bloqueada")
        void fechaNoDisponible_lanzaExcepcion() {
            when(configuracionService.obtenerFechaMaximaAgendamiento())
                    .thenReturn(LocalDate.now().plusWeeks(8));
            when(diaNoDisponibleService.esFechaNoDisponible(any())).thenReturn(true);

            assertThatThrownBy(() -> service.agendarCita(dtoAgendar()))
                    .isInstanceOf(FechaNoDisponibleException.class);
        }

        @Test
        @DisplayName("Lanza HorarioOcupadoException cuando hay traslape con otra cita programada")
        void horarioOcupado_lanzaExcepcion() {
            when(configuracionService.obtenerFechaMaximaAgendamiento())
                    .thenReturn(LocalDate.now().plusWeeks(8));
            when(diaNoDisponibleService.esFechaNoDisponible(any())).thenReturn(false);
            stubDisponibilidadSemanal(MANANA_9AM);

            // Existe una cita programada que traslapa
            Cita citaExistente = citaProgramada(PROFESIONAL_ID, MANANA_9AM, DURACION);
            when(citaRepository.findByProfesionalIdAndEstadoAndFechaHoraBetween(
                    eq(PROFESIONAL_ID), eq(EstadoCita.programada), any(), any()))
                    .thenReturn(List.of(citaExistente));
            when(bloqueoRepository.existeBloqueoEnFecha(eq(PROFESIONAL_ID), any()))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.agendarCita(dtoAgendar()))
                    .isInstanceOf(HorarioOcupadoException.class);
        }

        @Test
        @DisplayName("Lanza UsuarioNoEncontradoException cuando el paciente no existe en caché local")
        void pacienteNoEncontrado_lanzaExcepcion() {
            stubDisponibilidadOk();
            // Override la carga del paciente para que falle
            when(usuarioLocalRepository.findById(PACIENTE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.agendarCita(dtoAgendar()))
                    .isInstanceOf(UsuarioNoEncontradoException.class);
        }

        @Test
        @DisplayName("Lanza IllegalArgumentException para fecha en el pasado")
        void fechaPasada_lanzaExcepcion() {
            ZonedDateTime ayer = ZonedDateTime.now().minusDays(1);
            when(configuracionService.obtenerFechaMaximaAgendamiento())
                    .thenReturn(LocalDate.now().plusWeeks(4));

            CitaDTO dto = CitaDTO.builder()
                    .pacienteId(PACIENTE_ID).profesionalId(PROFESIONAL_ID)
                    .fechaHora(ayer).build();

            assertThatThrownBy(() -> service.agendarCita(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pasadas");
        }
    }

    // -----------------------------------------------------------------------
    // cancelarCita()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("cancelarCita()")
    class CancelarCitaTest {

        @Test
        @DisplayName("Cancela correctamente una cita programada")
        void cancelarProgramada_cambiaEstadoYPublicaEvento() {
            Cita cita = citaProgramada(PROFESIONAL_ID, MANANA_9AM, DURACION);
            cita.setId(10L);
            when(citaRepository.findById(10L)).thenReturn(Optional.of(cita));
            when(citaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CitaDTO resultado = service.cancelarCita(10L);

            assertThat(resultado.getEstado()).isEqualTo(EstadoCita.cancelada);
            verify(citaEventPublisher).publicarCitaCancelada(any());
        }

        @Test
        @DisplayName("Lanza CitaNoEncontradaException cuando la cita no existe")
        void citaNoExiste_lanzaExcepcion() {
            when(citaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelarCita(99L))
                    .isInstanceOf(CitaNoEncontradaException.class);
        }

        @Test
        @DisplayName("Lanza TransicionEstadoInvalidaException al cancelar una cita ya cancelada")
        void cancelarCancelada_lanzaExcepcion() {
            Cita cita = citaEnEstado(PROFESIONAL_ID, MANANA_9AM, DURACION, EstadoCita.cancelada);
            cita.setId(5L);
            when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));

            assertThatThrownBy(() -> service.cancelarCita(5L))
                    .isInstanceOf(TransicionEstadoInvalidaException.class);
        }

        @Test
        @DisplayName("Lanza TransicionEstadoInvalidaException al cancelar una cita ya completada")
        void cancelarCompletada_lanzaExcepcion() {
            Cita cita = citaEnEstado(PROFESIONAL_ID, MANANA_9AM, DURACION, EstadoCita.completada);
            cita.setId(6L);
            when(citaRepository.findById(6L)).thenReturn(Optional.of(cita));

            assertThatThrownBy(() -> service.cancelarCita(6L))
                    .isInstanceOf(TransicionEstadoInvalidaException.class);
        }
    }

    // -----------------------------------------------------------------------
    // completarCita()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("completarCita()")
    class CompletarCitaTest {

        @Test
        @DisplayName("Completa correctamente una cita programada")
        void completarProgramada_cambiaEstadoYPublicaEvento() {
            Cita cita = citaProgramada(PROFESIONAL_ID, MANANA_9AM, DURACION);
            cita.setId(20L);
            when(citaRepository.findById(20L)).thenReturn(Optional.of(cita));
            when(citaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CitaDTO resultado = service.completarCita(20L);

            assertThat(resultado.getEstado()).isEqualTo(EstadoCita.completada);
            verify(citaEventPublisher).publicarCitaCompletada(any());
        }

        @Test
        @DisplayName("Lanza CitaNoEncontradaException cuando la cita no existe")
        void citaNoExiste_lanzaExcepcion() {
            when(citaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.completarCita(99L))
                    .isInstanceOf(CitaNoEncontradaException.class);
        }

        @Test
        @DisplayName("Lanza TransicionEstadoInvalidaException al completar una cita completada")
        void completarCompletada_lanzaExcepcion() {
            Cita cita = citaEnEstado(PROFESIONAL_ID, MANANA_9AM, DURACION, EstadoCita.completada);
            cita.setId(7L);
            when(citaRepository.findById(7L)).thenReturn(Optional.of(cita));

            assertThatThrownBy(() -> service.completarCita(7L))
                    .isInstanceOf(TransicionEstadoInvalidaException.class);
        }
    }

    // -----------------------------------------------------------------------
    // buscarPorId()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorIdTest {

        @Test
        @DisplayName("Retorna el DTO cuando la cita existe")
        void citaExiste_retornaDTO() {
            Cita cita = citaProgramada(PROFESIONAL_ID, MANANA_9AM, DURACION);
            cita.setId(1L);
            cita.setPacienteId(PACIENTE_ID);
            when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

            CitaDTO resultado = service.buscarPorId(1L);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getPacienteId()).isEqualTo(PACIENTE_ID);
        }

        @Test
        @DisplayName("Lanza CitaNoEncontradaException cuando la cita no existe")
        void citaNoExiste_lanzaExcepcion() {
            when(citaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(CitaNoEncontradaException.class);
        }
    }

    // -----------------------------------------------------------------------
    // listarPorPaciente() / listarPorProfesional()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("listar*()")
    class ListarTest {

        @Test
        @DisplayName("listarPorPaciente() delega en el repositorio y mapea a DTOs")
        void listarPorPaciente_delegaYMapea() {
            Cita c = citaProgramada(PROFESIONAL_ID, MANANA_9AM, DURACION);
            c.setPacienteId(PACIENTE_ID);
            when(citaRepository.findByPacienteId(PACIENTE_ID)).thenReturn(List.of(c));

            List<CitaDTO> resultado = service.listarPorPaciente(PACIENTE_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getPacienteId()).isEqualTo(PACIENTE_ID);
        }

        @Test
        @DisplayName("listarPorProfesional() delega en el repositorio y mapea a DTOs")
        void listarPorProfesional_delegaYMapea() {
            Cita c = citaProgramada(PROFESIONAL_ID, MANANA_9AM, DURACION);
            when(citaRepository.findByProfesionalId(PROFESIONAL_ID)).thenReturn(List.of(c));

            List<CitaDTO> resultado = service.listarPorProfesional(PROFESIONAL_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getProfesionalId()).isEqualTo(PROFESIONAL_ID);
        }

        @Test
        @DisplayName("listarPorPaciente() retorna lista vacía si no hay citas")
        void listarPorPaciente_sinCitas_retornaVacio() {
            when(citaRepository.findByPacienteId(PACIENTE_ID)).thenReturn(List.of());
            assertThat(service.listarPorPaciente(PACIENTE_ID)).isEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // contarCitasPorEstado()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("contarCitasPorEstado()")
    class ContarCitasTest {

        @Test
        @DisplayName("Cuenta correctamente las citas en estado programada")
        void contarProgramadas_retornaConteoCorrect() {
            Cita c1 = citaProgramada(PROFESIONAL_ID, MANANA_9AM, DURACION);
            Cita c2 = citaEnEstado(PROFESIONAL_ID, MANANA_9AM.plusHours(1), DURACION, EstadoCita.cancelada);
            when(citaRepository.findAll()).thenReturn(List.of(c1, c2));

            assertThat(service.contarCitasPorEstado(EstadoCita.programada)).isEqualTo(1L);
        }

        @Test
        @DisplayName("Retorna 0 cuando no hay citas en el estado consultado")
        void sinCitasEnEstado_retornaCero() {
            when(citaRepository.findAll()).thenReturn(List.of());
            assertThat(service.contarCitasPorEstado(EstadoCita.completada)).isEqualTo(0L);
        }
    }

    // -----------------------------------------------------------------------
    // Stubs y fixtures
    // -----------------------------------------------------------------------

    /** Configura todos los mocks necesarios para un agendamiento exitoso. */
    private void stubDisponibilidadOk() {
        when(configuracionService.obtenerFechaMaximaAgendamiento())
                .thenReturn(LocalDate.now().plusWeeks(8));
        when(diaNoDisponibleService.esFechaNoDisponible(any())).thenReturn(false);
        stubDisponibilidadSemanal(MANANA_9AM);
        when(bloqueoRepository.existeBloqueoEnFecha(eq(PROFESIONAL_ID), any()))
                .thenReturn(false);
        when(citaRepository.findByProfesionalIdAndEstadoAndFechaHoraBetween(
                eq(PROFESIONAL_ID), eq(EstadoCita.programada), any(), any()))
                .thenReturn(List.of());
        when(usuarioLocalRepository.findById(PACIENTE_ID))
                .thenReturn(Optional.of(usuario(PACIENTE_ID, RolUsuario.paciente)));
        when(usuarioLocalRepository.findById(PROFESIONAL_ID))
                .thenReturn(Optional.of(usuario(PROFESIONAL_ID, RolUsuario.profesional)));
        when(citaRepository.save(any())).thenAnswer(inv -> {
            Cita c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });
        doNothing().when(citaEventPublisher).publicarCitaAgendada(any());
    }

    private void stubDisponibilidadSemanal(ZonedDateTime fechaHora) {
        int diaSemana = fechaHora.getDayOfWeek().getValue() % 7;
        DisponibilidadSemanal disp = DisponibilidadSemanal.builder()
                .profesionalId(PROFESIONAL_ID)
                .diaSemana(diaSemana)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(18, 0))
                .duracionCitaMinutos(DURACION)
                .build();
        when(disponibilidadRepository.findByProfesionalIdAndDiaSemana(
                eq(PROFESIONAL_ID), eq(diaSemana)))
                .thenReturn(List.of(disp));
    }

    private CitaDTO dtoAgendar() {
        return CitaDTO.builder()
                .pacienteId(PACIENTE_ID)
                .profesionalId(PROFESIONAL_ID)
                .fechaHora(MANANA_9AM)
                .build();
    }

    private Cita citaProgramada(Long profesionalId, ZonedDateTime fecha, int duracion) {
        return citaEnEstado(profesionalId, fecha, duracion, EstadoCita.programada);
    }

    private Cita citaEnEstado(Long profesionalId, ZonedDateTime fecha, int duracion, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setProfesionalId(profesionalId);
        cita.setFechaHora(fecha);
        cita.setDuracionMinutos(duracion);
        cita.setEstado(estado);
        cita.setCreadoEn(ZonedDateTime.now());
        return cita;
    }

    private UsuarioLocal usuario(Long id, RolUsuario rol) {
        return UsuarioLocal.builder()
                .id(id).nombreCompleto("Usuario " + id)
                .login("user" + id).rol(rol).activo(true).build();
    }
}
