package com.piedrazul.msusermanagement.application.service.interfaces;


import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;

public interface IRegistroService {
    UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO);
    UsuarioDTO registrarPaciente(UsuarioDTO usuarioDTO, PacienteDTO pacienteDTO);
    UsuarioDTO registrarProfesional(UsuarioDTO usuarioDTO, ProfesionalDTO profesionalDTO);
}