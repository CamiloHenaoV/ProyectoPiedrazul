package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;

import java.util.List;
import java.util.UUID;

public interface IUsuarioService {
    UsuarioDTO    autenticar(String login, String password);
    UsuarioDTO    crearUsuario(UsuarioDTO dto);
    Usuario crearUsuarioBase(UsuarioDTO dto);
    UsuarioDTO    buscarPorId(UUID id);
    List<UsuarioDTO> listarTodos();
    List<UsuarioDTO> listarPorRol(RolUsuario rol);
    List<UsuarioDTO> listarPorEstado(boolean activo);
    UsuarioDTO    actualizarUsuario(UUID id, UsuarioDTO dto);
    void          desactivarUsuario(UUID id);
    void          activarUsuario(UUID id);
    boolean       existeLogin(String login);
    UUID buscarPacienteIdPorUsuarioId(UUID usuarioId);
    long contarUsuariosActivos();
    boolean recuperarContrasena(String login, String passwordNueva);
    boolean cambiarContrasena(String login, String passwordActual, String passwordNueva);
}