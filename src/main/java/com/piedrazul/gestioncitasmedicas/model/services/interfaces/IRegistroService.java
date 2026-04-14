package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;


public interface IRegistroService {
    UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO);
    UsuarioDTO registrarPaciente(UsuarioDTO usuarioDTO, PacienteDTO pacienteDTO);
    UsuarioDTO registrarProfesional(UsuarioDTO usuarioDTO, ProfesionalDTO profesionalDTO);
}