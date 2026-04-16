package com.piedrazul.msusermanagement.application.service.interfaces;



import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import com.piedrazul.msusermanagement.domain.model.entity.enums.RolUsuario;

import java.util.List;

public interface IUsuarioService {
    UsuarioDTO crearUsuario(UsuarioDTO dto);
    Usuario crearUsuarioBase(UsuarioDTO dto);
    List<UsuarioDTO> listarTodos();
    List<UsuarioDTO> listarPorRol(RolUsuario rol);
    UsuarioDTO    actualizarUsuario(Long id, UsuarioDTO dto);
    void          desactivarUsuario(Long id);
    void          activarUsuario(Long id);
    Long buscarPacienteIdPorUsuarioId(Long usuarioId);
    long contarUsuariosActivos();
}