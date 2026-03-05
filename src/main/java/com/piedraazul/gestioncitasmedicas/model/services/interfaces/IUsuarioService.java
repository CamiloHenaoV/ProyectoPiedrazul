package com.piedraazul.gestioncitasmedicas.model.services.interfaces;

import com.piedraazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedraazul.gestioncitasmedicas.model.entities.enums.RolUsuario;

import java.util.List;
import java.util.UUID;

public interface IUsuarioService {
    UsuarioDTO    autenticar(String login, String password);
    UsuarioDTO    crearUsuario(UsuarioDTO dto);
    UsuarioDTO    buscarPorId(UUID id);
    List<UsuarioDTO> listarTodos();
    List<UsuarioDTO> listarPorRol(RolUsuario rol);
    UsuarioDTO    actualizarUsuario(UUID id, UsuarioDTO dto);
    void          desactivarUsuario(UUID id);
    boolean       existeLogin(String login);
}