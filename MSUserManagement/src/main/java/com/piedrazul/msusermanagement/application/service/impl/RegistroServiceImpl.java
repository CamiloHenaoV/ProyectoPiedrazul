package com.piedrazul.msusermanagement.application.service.impl;

import com.piedrazul.msusermanagement.application.service.interfaces.IPacienteService;
import com.piedrazul.msusermanagement.application.service.interfaces.IProfesionalService;
import com.piedrazul.msusermanagement.application.service.interfaces.IRegistroService;
import com.piedrazul.msusermanagement.application.service.interfaces.IUsuarioService;
import com.piedrazul.msusermanagement.domain.model.dto.PacienteDTO;
import com.piedrazul.msusermanagement.domain.model.dto.ProfesionalDTO;
import com.piedrazul.msusermanagement.domain.model.dto.UsuarioDTO;
import com.piedrazul.msusermanagement.domain.model.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroServiceImpl implements IRegistroService {

    private final IUsuarioService usuarioService;
    private final IPacienteService pacienteService;
    private final IProfesionalService profesionalService;

    public RegistroServiceImpl(
            IUsuarioService usuarioService
            , IPacienteService pacienteService
            , IProfesionalService profesionalService){
        this.usuarioService=usuarioService;
        this.pacienteService=pacienteService;
        this.profesionalService=profesionalService;
    }

    /**
     * Registro simple de usuario (sin rol adicional)
     */
    @Transactional
    public UsuarioDTO registrarUsuario(UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioService.crearUsuarioBase(usuarioDTO);

        return finalizarRegistro(usuario);
    }

    /**
     * Registro de usuario + paciente
     */
    @Transactional
    public UsuarioDTO registrarPaciente(UsuarioDTO usuarioDTO, PacienteDTO pacienteDTO) {
        Usuario usuario = usuarioService.crearUsuarioBase(usuarioDTO);

        pacienteService.crearPaciente(usuario, pacienteDTO);

        return finalizarRegistro(usuario);
    }

    /**
     * Registro de usuario + profesional
     */
    @Transactional
    public UsuarioDTO registrarProfesional(UsuarioDTO usuarioDTO, ProfesionalDTO profesionalDTO) {
        Usuario usuario = usuarioService.crearUsuarioBase(usuarioDTO);

        profesionalService.crearProfesional(usuario, profesionalDTO);

        return finalizarRegistro(usuario);
    }

    /**
     * Punto único de salida del flujo
     */
    private UsuarioDTO finalizarRegistro(Usuario usuario) {
        UsuarioDTO dto = toDTO(usuario);

        return dto;
    }

    /**
     * Mapper (puedes moverlo a un mapper dedicado si quieres)
     */
    private UsuarioDTO toDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .login(usuario.getLogin())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .build();
    }
}