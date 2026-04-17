package com.piedrazul.msusermanagement.application.service.impl;


import com.piedrazul.msusermanagement.application.service.interfaces.IUsuarioService;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;
import com.piedrazul.msusermanagement.domain.model.exceptions.LoginDuplicadoException;
import com.piedrazul.msusermanagement.domain.model.exceptions.UsuarioNoEncontradoException;
import com.piedrazul.msusermanagement.domain.model.repository.PacienteRepository;
import com.piedrazul.msusermanagement.domain.model.repository.UsuarioRepository;
import com.piedrazul.msusermanagement.infra.messaging.UserEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private final PacienteRepository pacienteRepository;
    private final UserEventPublisher userEventPublisher;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            UserEventPublisher userEventPublisher
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public Usuario crearUsuarioBase(UsuarioDTO dto) {
        if (usuarioRepository.existsByLogin(dto.getLogin())) {
            throw new LoginDuplicadoException(dto.getLogin());
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .login(dto.getLogin())
                .rol(dto.getRol())
                .activo(true)
                .build());
        userEventPublisher.publishUserRegistered(usuario);
        return usuario;
    }

    private UsuarioDTO finalizarCreacion(Usuario usuario) {
        UsuarioDTO dto = toDTO(usuario);
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
    @Override
    public UsuarioDTO buscarPorId(Long id){
        Usuario usuario=usuarioRepository.findById(id)
                .orElseThrow(()-> new UsuarioNoEncontradoException(id.toString()));
        return toDTO(usuario);
    }

    // HU 1.3 - implementacion edicion de usuario por admin
    @Override
    public UsuarioDTO actualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
        if (dto.getNombreCompleto() == null || dto.getNombreCompleto().trim().isEmpty()) {
    throw new IllegalArgumentException("El nombre no puede estar vacío");
}
        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setRol(dto.getRol());

        UsuarioDTO actualizado = toDTO(usuarioRepository.save(usuario));
        return actualizado;
    }

    @Override
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));

        usuario.setActivo(false);
        UsuarioDTO desactivado = toDTO(usuarioRepository.save(usuario));
    }


    @Override
    public void activarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
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
    public Long buscarPacienteIdPorUsuarioId(Long usuarioId) {
        return pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId.toString()))
                .getId();
    }
    @Override
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }
}
