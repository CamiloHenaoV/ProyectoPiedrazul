package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;

import java.util.List;
import java.util.UUID;

public interface IUsuarioService {
    UsuarioDTO    crearUsuario(UsuarioDTO dto);
    Usuario crearUsuarioBase(UsuarioDTO dto);
    List<UsuarioDTO> listarTodos();
    List<UsuarioDTO> listarPorRol(RolUsuario rol);
    UsuarioDTO    actualizarUsuario(UUID id, UsuarioDTO dto);
    void          desactivarUsuario(UUID id);
    void          activarUsuario(UUID id);
    UUID buscarPacienteIdPorUsuarioId(UUID usuarioId);
    long contarUsuariosActivos();
}