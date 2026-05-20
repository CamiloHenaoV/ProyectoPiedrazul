package com.piedrazul.msusermanagement.application.service.impl;

import com.piedrazul.msusermanagement.application.service.interfaces.IRegistroService;
import com.piedrazul.msusermanagement.application.service.template.RegistroContexto;
import com.piedrazul.msusermanagement.application.service.template.impl.RegistroAdminService;
import com.piedrazul.msusermanagement.application.service.template.impl.RegistroPacienteService;
import com.piedrazul.msusermanagement.application.service.template.impl.RegistroProfesionalService;
import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fachada que implementa {@link IRegistroService} y delega cada tipo de
 * registro al subclase concreta del patrón <b>Template Method</b> que
 * le corresponde.
 *
 * <p>Esta clase actúa como punto de entrada único para el controlador,
 * preservando el contrato existente de {@link IRegistroService} sin exponer
 * la jerarquía interna de templates. Internamente, construye un
 * {@link RegistroContexto} con los datos de la solicitud y se lo entrega
 * al template method de la subclase adecuada.
 *
 * <pre>
 * RegistroController
 *       │
 *       ▼  IRegistroService
 * RegistroServiceImpl  (fachada)
 *   ├──► RegistroAdminService.registrar(ctx)
 *   ├──► RegistroPacienteService.registrar(ctx)
 *   └──► RegistroProfesionalService.registrar(ctx)
 *              │
 *              ▼  RegistroTemplate.registrar()  [template method]
 *         1. crearUsuarioBase()
 *         2. vincularPerfil()       ← hook sobreescrito en cada subclase
 *         3. toDTO()
 * </pre>
 */
@Service
public class RegistroServiceImpl implements IRegistroService {

    private final RegistroAdminService       adminService;
    private final RegistroPacienteService    pacienteService;
    private final RegistroProfesionalService profesionalService;

    public RegistroServiceImpl(RegistroAdminService adminService,
                               RegistroPacienteService pacienteService,
                               RegistroProfesionalService profesionalService) {
        this.adminService       = adminService;
        this.pacienteService    = pacienteService;
        this.profesionalService = profesionalService;
    }

    /**
     * Registro de usuario administrador (sin perfil clínico).
     * Delega a {@link RegistroAdminService}, cuyo hook es un no-op.
     */
    @Override
    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO) {
        return adminService.registrar(
                new RegistroContexto(usuarioDTO, null, null)
        );
    }

    /**
     * Registro de usuario + perfil de paciente.
     * Delega a {@link RegistroPacienteService}, que en su hook crea el Paciente.
     */
    @Override
    @Transactional
    public UsuarioDTO registrarPaciente(UsuarioDTO usuarioDTO, PacienteDTO pacienteDTO) {
        return pacienteService.registrar(
                new RegistroContexto(usuarioDTO, pacienteDTO, null)
        );
    }

    /**
     * Registro de usuario + perfil de profesional.
     * Delega a {@link RegistroProfesionalService}, que en su hook crea el Profesional
     * y publica el evento de dominio {@code profesional.creado}.
     */
    @Override
    @Transactional
    public UsuarioDTO registrarProfesional(UsuarioDTO usuarioDTO, ProfesionalDTO profesionalDTO) {
        return profesionalService.registrar(
                new RegistroContexto(usuarioDTO, null, profesionalDTO)
        );
    }
}
