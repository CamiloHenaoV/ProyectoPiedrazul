package com.piedrazul.gestioncitasmedicas.model.services.interfaces;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Paciente;
import java.util.UUID;

public interface IPacienteService {
    PacienteDTO crearPaciente(UUID usuarioId, PacienteDTO dto);
}
