package com.piedrazul.gestioncitasmedicas.model.services.impl;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.Usuario;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPacienteService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IProfesionalService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IRegistroService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroServiceImpl implements IRegistroService {

    private final IUsuarioService usuarioService;
    private final IPacienteService pacienteService;
    private final IProfesionalService profesionalService;
    private final EventBus eventBus;

    public RegistroServiceImpl(
            IUsuarioService usuarioService
            , IPacienteService pacienteService
            , IProfesionalService profesionalService
            , EventBus eventBus){
        this.usuarioService=usuarioService;
        this.pacienteService=pacienteService;
        this.profesionalService=profesionalService;
        this.eventBus=eventBus;
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

        eventBus.publish(AppEvent.USUARIO_CREADO, dto);

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