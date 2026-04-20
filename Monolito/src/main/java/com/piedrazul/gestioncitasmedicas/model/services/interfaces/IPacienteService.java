package com.piedrazul.gestioncitasmedicas.model.services.interfaces;


import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;

public interface IPacienteService {
    void crearPaciente(Usuario usuario, PacienteDTO dto);
}
