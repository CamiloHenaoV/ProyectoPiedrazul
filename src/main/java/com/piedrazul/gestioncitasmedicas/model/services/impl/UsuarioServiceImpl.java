package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.exceptions.*;
import com.piedrazul.gestioncitasmedicas.model.repositories.UsuarioRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final IPasswordService  passwordService;
    private final EventBus          eventBus;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            IPasswordService  passwordService,
            EventBus          eventBus
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService   = passwordService;
        this.eventBus          = eventBus;
    }

    @Override
    public UsuarioDTO autenticar(String login, String password) {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.getActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordService.verificar(password, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        return toDTO(usuario);
    }

    @Override
    public UsuarioDTO crearUsuario(UsuarioDTO dto) {
        if (usuarioRepository.existsByLogin(dto.getLogin())) {
            throw new LoginDuplicadoException(dto.getLogin());
        }

        Usuario usuario = Usuario.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .login(dto.getLogin())
                .passwordHash(passwordService.encriptar(dto.getPassword()))
                .rol(dto.getRol())
                .activo(true)
                .build();

        UsuarioDTO guardado = toDTO(usuarioRepository.save(usuario));
        eventBus.publish(AppEvent.USUARIO_CREADO, guardado);
        return guardado;
    }

    @Override
    public UsuarioDTO buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
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

    @Override
    public UsuarioDTO actualizarUsuario(UUID id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));

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

    @Override
    public boolean existeLogin(String login) {
        return usuarioRepository.existsByLogin(login);
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
}