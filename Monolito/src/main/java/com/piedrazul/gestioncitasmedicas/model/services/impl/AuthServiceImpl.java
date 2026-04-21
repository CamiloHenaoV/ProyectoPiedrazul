package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.exceptions.CredencialesInvalidasException;
import com.piedrazul.gestioncitasmedicas.model.repositories.UsuarioRepository;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IAuthService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPasswordService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements IAuthService {
    private final UsuarioRepository usuarioRepository;
    private final IPasswordService passwordService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           IPasswordService passwordService){
        this.passwordService=passwordService;
        this.usuarioRepository=usuarioRepository;
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
