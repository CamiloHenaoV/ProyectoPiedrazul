package com.piedraazul.gestioncitasmedicas.model.services.impl;
import com.piedraazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedraazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedraazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedraazul.gestioncitasmedicas.model.exceptions.*;
import com.piedraazul.gestioncitasmedicas.model.repositories.UsuarioRepository;
import com.piedraazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import com.piedraazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final IPasswordService passwordService;   // depende de la interfaz

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            IPasswordService  passwordService              // Spring inyecta BCryptPasswordServiceImpl
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService   = passwordService;
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
                .passwordHash(passwordService.encriptar(dto.getPassword())) // delega
                .rol(dto.getRol())
                .activo(true)
                .build();

        return toDTO(usuarioRepository.save(usuario));
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
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> listarPorRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioDTO actualizarUsuario(UUID id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));

        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setRol(dto.getRol());

        return toDTO(usuarioRepository.save(usuario));
    }

    @Override
    public void desactivarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id.toString()));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    public boolean existeLogin(String login) {
        return usuarioRepository.existsByLogin(login);
    }

    // Conversión entidad → DTO (el controller nunca ve la entidad)
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
