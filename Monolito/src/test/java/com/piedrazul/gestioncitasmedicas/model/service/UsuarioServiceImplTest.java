package com.piedrazul.gestioncitasmedicas.model.service;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.*;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import com.piedrazul.gestioncitasmedicas.model.exceptions.*;
import com.piedrazul.gestioncitasmedicas.model.repositories.*;
import com.piedrazul.gestioncitasmedicas.model.services.impl.UsuarioServiceImpl;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IPasswordService passwordService;
    @Mock private EventBus eventBus;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfesionalRepository profesionalRepository;
    @Mock private EspecialidadRepository especialidadRepository;
    @Mock private DisponibilidadSemanalRepository disponibilidadRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void shouldAuthenticateUser() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .login("juanp")
                .passwordHash("hashed")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.paciente)
                .activo(true)
                .build();

        when(usuarioRepository.findByLogin("juanp")).thenReturn(Optional.of(usuario));
        when(passwordService.verificar("12345678", "hashed")).thenReturn(true);

        UsuarioDTO result = usuarioService.autenticar("juanp", "12345678");

        assertNotNull(result);
        assertEquals("juanp", result.getLogin());
        assertEquals("Juan Pérez", result.getNombreCompleto());
    }

    @Test
    void shouldCreateUsuario() {
        UsuarioDTO dto = UsuarioDTO.builder()
                .login("juanp")
                .password("12345678")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.paciente)
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .id(UUID.randomUUID())
                .login("juanp")
                .nombreCompleto("Juan Pérez")
                .passwordHash("encrypted")
                .rol(RolUsuario.paciente)
                .activo(true)
                .build();

        when(usuarioRepository.existsByLogin("juanp")).thenReturn(false);
        when(passwordService.encriptar("12345678")).thenReturn("encrypted");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        UsuarioDTO result = usuarioService.crearUsuario(dto);

        assertNotNull(result);
        assertEquals("juanp", result.getLogin());
        assertEquals("Juan Pérez", result.getNombreCompleto());
        verify(eventBus).publish(eq(AppEvent.USUARIO_CREADO), any());
    }

    @Test
    void shouldThrowWhenLoginAlreadyExists() {
        UsuarioDTO dto = UsuarioDTO.builder()
                .login("juanp")
                .password("12345678")
                .nombreCompleto("Juan Pérez")
                .rol(RolUsuario.paciente)
                .build();

        when(usuarioRepository.existsByLogin("juanp")).thenReturn(true);

        assertThrows(LoginDuplicadoException.class, () -> usuarioService.crearUsuario(dto));
    }

    @Test
    void shouldCreateUsuarioWithPaciente() {
        UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                .login("paciente1")
                .password("12345678")
                .nombreCompleto("Paciente Uno")
                .rol(RolUsuario.paciente)
                .build();

        PacienteDTO pacienteDTO = PacienteDTO.builder()
                .nombreCompleto("Paciente Uno")
                .cedulaIdentidad("001-1234567-8")
                .fechaNacimiento(LocalDate.of(1995, 1, 1))
                .telefono("8091234567")
                .email("p@example.com")
                .direccion("Santo Domingo")
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .id(UUID.randomUUID())
                .login("paciente1")
                .nombreCompleto("Paciente Uno")
                .passwordHash("encrypted")
                .rol(RolUsuario.paciente)
                .activo(true)
                .build();

        when(usuarioRepository.existsByLogin("paciente1")).thenReturn(false);
        when(passwordService.encriptar("12345678")).thenReturn("encrypted");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO result = usuarioService.crearUsuarioConPaciente(usuarioDTO, pacienteDTO);

        assertNotNull(result);
        assertEquals("paciente1", result.getLogin());
        verify(pacienteRepository).save(any(Paciente.class));
        verify(eventBus).publish(eq(AppEvent.USUARIO_CREADO), any());
    }

    @Test
    void shouldCreateUsuarioWithProfesional() {
        UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                .login("prof1")
                .password("12345678")
                .nombreCompleto("Doctor Uno")
                .rol(RolUsuario.profesional)
                .build();

        ProfesionalDTO profesionalDTO = ProfesionalDTO.builder()
                .tipo(TipoProfesional.medico)
                .especialidadNombre("Cardiología")
                .licenciaProfesional("LIC-001")
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .id(UUID.randomUUID())
                .login("prof1")
                .nombreCompleto("Doctor Uno")
                .passwordHash("encrypted")
                .rol(RolUsuario.profesional)
                .activo(true)
                .build();

        Especialidad especialidad = Especialidad.builder()
                .id(1)
                .nombre("Cardiología")
                .build();

        when(usuarioRepository.existsByLogin("prof1")).thenReturn(false);
        when(passwordService.encriptar("12345678")).thenReturn("encrypted");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(especialidadRepository.findByNombre("Cardiología")).thenReturn(Optional.of(especialidad));
        when(profesionalRepository.save(any(Profesional.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(disponibilidadRepository.save(any(DisponibilidadSemanal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO result = usuarioService.crearUsuarioConProfesional(usuarioDTO, profesionalDTO);

        assertNotNull(result);
        assertEquals("prof1", result.getLogin());
        verify(profesionalRepository).save(any(Profesional.class));
        verify(disponibilidadRepository, times(5)).save(any(DisponibilidadSemanal.class));
        verify(eventBus).publish(eq(AppEvent.USUARIO_CREADO), any());
    }

    @Test
    void shouldUpdateUsuario() {
        UUID id = UUID.randomUUID();

        Usuario usuario = Usuario.builder()
                .id(id)
                .login("juanp")
                .nombreCompleto("Juan Viejo")
                .rol(RolUsuario.paciente)
                .activo(true)
                .build();

        UsuarioDTO dto = UsuarioDTO.builder()
                .nombreCompleto("Juan Nuevo")
                .rol(RolUsuario.profesional)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioDTO result = usuarioService.actualizarUsuario(id, dto);

        assertEquals("Juan Nuevo", result.getNombreCompleto());
        assertEquals(RolUsuario.profesional, result.getRol());
        verify(eventBus).publish(eq(AppEvent.USUARIO_ACTUALIZADO), any());
    }

    @Test
    void shouldDeactivateUsuario() {
        UUID id = UUID.randomUUID();

        Usuario usuario = Usuario.builder()
                .id(id)
                .login("juanp")
                .activo(true)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.desactivarUsuario(id);

        assertFalse(usuario.getActivo());
        verify(eventBus).publish(eq(AppEvent.USUARIO_DESACTIVADO), any());
    }

    @Test
    void shouldChangePassword() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .login("juanp")
                .passwordHash("oldHash")
                .activo(true)
                .build();

        when(usuarioRepository.findByLogin("juanp")).thenReturn(Optional.of(usuario));
        when(passwordService.verificar("oldPass", "oldHash")).thenReturn(true);
        when(passwordService.encriptar("newPass123")).thenReturn("newHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = usuarioService.cambiarContrasena("juanp", "oldPass", "newPass123");

        assertTrue(result);
        assertEquals("newHash", usuario.getPasswordHash());
    }

    @Test
    void shouldRecoverPassword() {
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .login("juanp")
                .passwordHash("oldHash")
                .activo(true)
                .build();

        when(usuarioRepository.findByLogin("juanp")).thenReturn(Optional.of(usuario));
        when(passwordService.encriptar("newPass123")).thenReturn("newHash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = usuarioService.recuperarContrasena("juanp", "newPass123");

        assertTrue(result);
        assertEquals("newHash", usuario.getPasswordHash());
    }
}