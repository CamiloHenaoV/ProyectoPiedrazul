package com.piedrazul.gestioncitasmedicas.model.service;

import com.piedrazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.*;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import com.piedrazul.gestioncitasmedicas.model.exceptions.*;
import com.piedrazul.gestioncitasmedicas.model.repositories.*;
import com.piedrazul.gestioncitasmedicas.model.services.impl.CitaServiceImpl;
import com.piedrazul.gestioncitasmedicas.model.services.impl.UsuarioServiceImpl;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioCitaServiceTests {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfesionalRepository profesionalRepository;
    @Mock private EspecialidadRepository especialidadRepository;
    @Mock private DisponibilidadSemanalRepository disponibilidadRepository;
    @Mock private BloqueoDisponibilidadRepository bloqueoRepository;
    @Mock private CitaRepository citaRepository;
    @Mock private IPasswordService passwordService;
    @Mock private EventBus eventBus;

    private UsuarioServiceImpl usuarioService;
    private CitaServiceImpl citaService;

    @Captor private ArgumentCaptor<AppEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(
                usuarioRepository,
                passwordService,
                eventBus,
                pacienteRepository,
                profesionalRepository,
                especialidadRepository,
                disponibilidadRepository
        );

        citaService = new CitaServiceImpl(
                citaRepository,
                pacienteRepository,
                profesionalRepository,
                disponibilidadRepository,
                bloqueoRepository,
                eventBus
        );
    }

    @Test
    void HU_1_1_creaPacienteValido_yPasswordCifrada_yPublicaEvento() {
        UsuarioDTO input = UsuarioDTO.builder()
                .nombreCompleto("Pedro Paciente")
                .login("pedro")
                .password("12345678")
                .rol(RolUsuario.paciente)
                .build();

        PacienteDTO pacienteDTO = PacienteDTO.builder()
                .nombreCompleto("Pedro Paciente")
                .cedulaIdentidad("1234567890")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .email("pedro@example.com")
                .telefono("3000000000")
                .direccion("Calle 123")
                .build();

        when(usuarioRepository.existsByLogin(input.getLogin())).thenReturn(false);
        when(passwordService.encriptar(anyString())).thenReturn("hash-1234");

        Usuario persisted = Usuario.builder()
                .id(UUID.randomUUID())
                .login(input.getLogin())
                .nombreCompleto(input.getNombreCompleto())
                .rol(input.getRol())
                .activo(true)
                .passwordHash("hash-1234")
                .build();
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(persisted);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioDTO result = usuarioService.crearUsuarioConPaciente(input, pacienteDTO);

        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo("pedro");
        assertThat(result.getRol()).isEqualTo(RolUsuario.paciente);
        assertThat(result.getPassword()).isNull();
        verify(usuarioRepository). save(any(Usuario.class));
        verify(eventBus).publish(eq(AppEvent.USUARIO_CREADO), any());
    }

    @Test
    void HU_1_1_registroConLoginDuplicado_lanzaLoginDuplicadoException() {
        UsuarioDTO input = UsuarioDTO.builder().login("maria").password("12345678").rol(RolUsuario.paciente).build();
        when(usuarioRepository.existsByLogin("maria")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario(input))
                .isInstanceOf(LoginDuplicadoException.class)
                .hasMessageContaining("maria");
    }

    @Test
    void HU_1_1_passwordInvalida_menorDe8_lanzaPasswordInvalidaException() {
        UsuarioDTO input = UsuarioDTO.builder().login("paco").password("1234567").rol(RolUsuario.paciente).build();
        when(usuarioRepository.existsByLogin("paco")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.crearUsuario(input))
                .isInstanceOf(PasswordInvalidaException.class)
                .hasMessageContaining("al menos 8 caracteres");
    }

    @Test
    void HU_1_3_edicionUsuario_cambiaNombreRol_yRegistraAuditoria() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(id)
                .login("alicia")
                .nombreCompleto("Alicia Vieja")
                .rol(RolUsuario.paciente)
                .activo(true)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioDTO updated = UsuarioDTO.builder()
                .nombreCompleto("Alicia Nueva")
                .rol(RolUsuario.administrador)
                .build();

        UsuarioDTO result = usuarioService.actualizarUsuario(id, updated);

        assertThat(result.getNombreCompleto()).isEqualTo("Alicia Nueva");
        assertThat(result.getRol()).isEqualTo(RolUsuario.administrador);
        verify(eventBus).publish(eq(AppEvent.USUARIO_ACTUALIZADO), any());
    }

    @Test
    void HU_1_5_desactivar_usuarioNoPuedeAutenticar() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(id)
                .login("oscar")
                .passwordHash("hash-antiguo")
                .activo(true)
                .rol(RolUsuario.paciente)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        usuarioService.desactivarUsuario(id);
        assertThat(usuario.getActivo()).isFalse();
        verify(eventBus).publish(eq(AppEvent.USUARIO_DESACTIVADO), any());

        when(usuarioRepository.findByLogin("oscar")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.autenticar("oscar", "12345678"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void HU_1_6_cambioContrasena_correctoActualActualizaHash_yAuditoria() {
        Usuario usuario = Usuario.builder()
                .login("luis")
                .passwordHash("oldHash")
                .activo(true)
                .build();

        when(usuarioRepository.findByLogin("luis")).thenReturn(Optional.of(usuario));
        when(passwordService.verificar("oldpass", "oldHash")).thenReturn(true);
        when(passwordService.encriptar("newpass123")).thenReturn("newHash");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean resultado = usuarioService.cambiarContrasena("luis", "oldpass", "newpass123");

        assertThat(resultado).isTrue();
        assertThat(usuario.getPasswordHash()).isEqualTo("newHash");
        verify(eventBus).publish(eq(AppEvent.USUARIO_ACTUALIZADO), any());
    }

    @Test
    void HU_1_6_cambioContrasena_conActualIncorrecta_lanzaCredencialesInvalidas() {
        Usuario usuario = Usuario.builder().login("luis").passwordHash("oldHash").activo(true).build();
        when(usuarioRepository.findByLogin("luis")).thenReturn(Optional.of(usuario));
        when(passwordService.verificar("wrong", "oldHash")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.cambiarContrasena("luis", "wrong", "newpass123"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void HU_1_7_recuperacionContrasena_loginNoExistente_lanzaUsuarioNoEncontrado() {
        when(usuarioRepository.findByLogin("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.recuperarContrasena("inexistente", "newpass123"))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void HU_1_7_recuperacionContrasena_passwordInvalida_lanzaPasswordInvalida() {
        Usuario usuario = Usuario.builder().login("mari").passwordHash("h").activo(true).build();
        when(usuarioRepository.findByLogin("mari")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.recuperarContrasena("mari", "1234"))
                .isInstanceOf(PasswordInvalidaException.class);
    }

    @Test
    void HU_1_7_recuperacionContrasena_exito_almacenaHash_yAuditoria() {
        Usuario usuario = Usuario.builder().login("edu").passwordHash("old").build();
        when(usuarioRepository.findByLogin("edu")).thenReturn(Optional.of(usuario));
        when(passwordService.encriptar("newPassword123")).thenReturn("newHash");
        when(usuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        boolean resultado = usuarioService.recuperarContrasena("edu", "newPassword123");

        assertThat(resultado).isTrue();
        assertThat(usuario.getPasswordHash()).isEqualTo("newHash");
        verify(eventBus).publish(eq(AppEvent.USUARIO_ACTUALIZADO), any());
    }

    @Test
    void HU_1_4_listarUsuarios_yFiltrarPorRol() {
        Usuario admin = Usuario.builder().id(UUID.randomUUID()).login("admin").rol(RolUsuario.administrador).activo(true).build();
        Usuario paciente = Usuario.builder().id(UUID.randomUUID()).login("pac").rol(RolUsuario.paciente).activo(true).build();

        when(usuarioRepository.findAll()).thenReturn(List.of(admin, paciente));
        when(usuarioRepository.findByRol(RolUsuario.paciente)).thenReturn(List.of(paciente));

        List<UsuarioDTO> todos = usuarioService.listarTodos();
        assertThat(todos).hasSize(2);

        List<UsuarioDTO> pacientes = usuarioService.listarPorRol(RolUsuario.paciente);
        assertThat(pacientes).hasSize(1);
        assertThat(pacientes.get(0).getLogin()).isEqualTo("pac");
    }

    @Test
    void HU_5_3_sugerirHorariosDisponibles_excluyeOcupadosBloqueados() {
        Integer profesionalId = 10;
        LocalDate fecha = LocalDate.of(2026, 3, 24); // lunes
        var disponibilidad = DisponibilidadSemanal.builder()
                .id(1)
                .diaSemana(1)
                .horaInicio(LocalTime.of(8, 0))
                .horaFin(LocalTime.of(9, 0))
                .duracionCitaMinutos(30)
                .build();

        when(disponibilidadRepository.findByProfesionalIdAndDiaSemana(profesionalId, 2)).thenReturn(List.of(disponibilidad));
        when(citaRepository.existsByProfesionalIdAndFechaHora(eq(profesionalId), any(ZonedDateTime.class))).thenAnswer(invocation -> {
            ZonedDateTime slot = invocation.getArgument(1);
            return slot.getHour() == 8 && slot.getMinute() == 30;
        });
        when(bloqueoRepository.existeBloqueoEnFecha(anyInt(), any(ZonedDateTime.class))).thenReturn(false);

        List<ZonedDateTime> horarios = citaService.obtenerHorariosDisponibles(profesionalId, fecha);

        assertThat(horarios).hasSize(1);
        assertThat(horarios.get(0).getHour()).isEqualTo(8);
        assertThat(horarios.get(0).getMinute()).isEqualTo(0);
    }

    @Test
    void HU_5_4_confirmarCita_creaCitaYPublicaEvento() {
        UUID citaId = UUID.randomUUID();
        UUID pacienteId = UUID.randomUUID();
        Integer profesionalId = 22;

        Paciente paciente = Paciente.builder().id(pacienteId).nombreCompleto("Paciente").build();
        Usuario usuarioProf = Usuario.builder().id(UUID.randomUUID()).nombreCompleto("Profe").build();
        Profesional profesional = Profesional.builder().id(profesionalId).usuario(usuarioProf).build();

        ZonedDateTime fechaHora = ZonedDateTime.of(2026, 3, 25, 8, 0, 0, 0, ZoneId.systemDefault());
        CitaDTO input = CitaDTO.builder().pacienteId(pacienteId).profesionalId(profesionalId).fechaHora(fechaHora).build();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(profesionalRepository.findById(profesionalId)).thenReturn(Optional.of(profesional));
        when(citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, fechaHora)).thenReturn(false);
        when(bloqueoRepository.existeBloqueoEnFecha(profesionalId, fechaHora)).thenReturn(false);
        when(disponibilidadRepository.findByProfesionalIdAndDiaSemana(profesionalId, fechaHora.getDayOfWeek().getValue() % 7)).thenReturn(List.of(
                DisponibilidadSemanal.builder()
                        .profesional(profesional)
                        .diaSemana(fechaHora.getDayOfWeek().getValue() % 7)
                        .horaInicio(LocalTime.of(7, 0))
                        .horaFin(LocalTime.of(9, 0))
                        .duracionCitaMinutos(30)
                        .build()
        ));

        Cita citaGuardada = Cita.builder()
                .id(citaId)
                .paciente(paciente)
                .profesional(profesional)
                .fechaHora(fechaHora)
                .estado(EstadoCita.programada)
                .build();

        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        CitaDTO output = citaService.agendarCita(input);

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(citaId);
        assertThat(output.getEstado()).isEqualTo(EstadoCita.programada);
        verify(eventBus).publish(eq(AppEvent.CITA_AGENDADA), any());
    }

    @Test
    void HU_5_3_agendarCitaEnHorarioOcupado_lanzaHorarioOcupadoException() {
        UUID pacienteId = UUID.randomUUID();
        Integer profesionalId = 10;
        ZonedDateTime fecha = ZonedDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);

        when(citaRepository.existsByProfesionalIdAndFechaHora(profesionalId, fecha)).thenReturn(true);

        CitaDTO citaDTO = CitaDTO.builder().pacienteId(pacienteId).profesionalId(profesionalId).fechaHora(fecha).build();

        assertThatThrownBy(() -> citaService.agendarCita(citaDTO))
                .isInstanceOf(HorarioOcupadoException.class);
    }
}
