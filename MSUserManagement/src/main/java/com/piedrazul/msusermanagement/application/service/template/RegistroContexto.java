package com.piedrazul.msusermanagement.application.service.template;

import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;

/**
 * Objeto de contexto que agrupa todos los datos posibles de una solicitud
 * de registro. Cada campo opcional es null cuando no aplica al tipo de
 * registro concreto.
 *
 * <ul>
 *   <li>{@code usuarioDTO}      — siempre presente (datos del usuario base).</li>
 *   <li>{@code pacienteDTO}     — presente solo en registro de paciente.</li>
 *   <li>{@code profesionalDTO}  — presente solo en registro de profesional.</li>
 * </ul>
 * <p>
 * Al centralizar los datos aquí, el template method puede tener una firma
 * uniforme independientemente del tipo de registro.
 *
 * @param pacienteDTO    null si no aplica
 * @param profesionalDTO null si no aplica
 */
public record RegistroContexto(UsuarioDTO usuarioDTO, PacienteDTO pacienteDTO, ProfesionalDTO profesionalDTO) {

}
