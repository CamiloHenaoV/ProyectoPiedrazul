package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.exceptions.LoginDuplicadoException;
import com.piedrazul.gestioncitasmedicas.model.exceptions.PasswordInvalidaException;
import com.piedrazul.gestioncitasmedicas.model.exceptions.UsuarioNoEncontradoException;
import com.piedrazul.gestioncitasmedicas.model.repositories.PacienteRepository;
import com.piedrazul.gestioncitasmedicas.model.repositories.UsuarioRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * Implementación del servicio de gestión de usuarios.
 * <p>
 * Centraliza toda la lógica de negocio relacionada con usuarios,
 * delegando la persistencia al repositorio y la encriptación
 * al servicio de passwords.
 */
@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final IPasswordService  passwordService;
    private final EventBus          eventBus;
    private final PacienteRepository pacienteRepository;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            IPasswordService  passwordService,
            EventBus          eventBus,
            PacienteRepository pacienteRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService   = passwordService;
        this.eventBus          = eventBus;
        this.pacienteRepository = pacienteRepository;
    }

    private static final int PASSWORD_MIN_LENGTH = 8;

    private void validarPasswordSinFormato(String password) {
    if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
        throw new PasswordInvalidaException("La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres");
    }

    if (!password.matches("^(?=.*[A-Z])(?=.*\\d).+$")) {
        throw new PasswordInvalidaException("La contraseña debe contener al menos una mayúscula y un número");
    }
}

    @Override
    public UsuarioDTO crearUsuario(UsuarioDTO dto) {
        Usuario usuario = crearUsuarioBase(dto);
        return finalizarCreacion(usuario);
    }
    @Override
    public Usuario crearUsuarioBase(UsuarioDTO dto) {
        if (usuarioRepository.existsByLogin(dto.getLogin())) {
            throw new LoginDuplicadoException(dto.getLogin());
        }

        validarPasswordSinFormato(dto.getPassword());

        return usuarioRepository.save(Usuario.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .login(dto.getLogin())
                .passwordHash(passwordService.encriptar(dto.getPassword()))
                .rol(dto.getRol())
                .activo(true)
                .build());
    }
    private UsuarioDTO finalizarCreacion(Usuario usuario) {
        UsuarioDTO dto = toDTO(usuario);
        eventBus.publish(AppEvent.USUARIO_CREADO, dto);
        return dto;
    }


    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> listarPorRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    // HU 1.3 - implementacion edicion de usuario por admin
    @Override
    public UsuarioDTO actualizarUsuario(UUID id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
        if (dto.getNombreCompleto() == null || dto.getNombreCompleto().trim().isEmpty()) {
    throw new IllegalArgumentException("El nombre no puede estar vacío");
}
        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setRol(dto.getRol());

        UsuarioDTO actualizado = toDTO(usuarioRepository.save(usuario));
        eventBus.publish(AppEvent.USUARIO_ACTUALIZADO, actualizado);
        return actualizado;
    }

    @Override
    public void desactivarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));

        usuario.setActivo(false);
        UsuarioDTO desactivado = toDTO(usuarioRepository.save(usuario));
        eventBus.publish(AppEvent.USUARIO_DESACTIVADO, desactivado);
    }


    @Override
    public void activarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        eventBus.publish(AppEvent.USUARIO_ACTUALIZADO, toDTO(usuario));
    }


    private UsuarioDTO toDTO(Usuario u) {
        return UsuarioDTO.builder()
                .id(u.getId())
                .nombreCompleto(u.getNombreCompleto())
                .login(u.getLogin())
                .rol(u.getRol())
                .activo(u.getActivo())
                .build();
    }

    @Override
    public UUID buscarPacienteIdPorUsuarioId(UUID usuarioId) {
        return pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId.toString()))
                .getId();
    }
    @Override
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }
}
