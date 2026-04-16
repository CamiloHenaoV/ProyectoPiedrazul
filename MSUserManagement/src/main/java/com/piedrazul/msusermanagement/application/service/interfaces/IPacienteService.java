package com.piedrazul.msusermanagement.application.service.interfaces;


import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;

public interface IPacienteService {
    void crearPaciente(Usuario usuario, PacienteDTO dto);
}
