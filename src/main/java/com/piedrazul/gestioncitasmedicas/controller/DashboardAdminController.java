package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import com.piedrazul.gestioncitasmedicas.observer.Observer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DashboardAdminController implements Observer<UsuarioDTO> {

    @FXML private Label lblUsuario;
    @FXML private Label lblTotalUsuarios;

    private final IUsuarioService  usuarioService;
    private final StageInitializer stageInitializer;
    private final EventBus         eventBus;

    private UsuarioDTO usuarioActual;

    public DashboardAdminController(IUsuarioService usuarioService,
                                    StageInitializer stageInitializer,
                                    EventBus eventBus) {
        this.usuarioService   = usuarioService;
        this.stageInitializer = stageInitializer;
        this.eventBus         = eventBus;
    }
    @FXML
    public void initialize() {
        eventBus.subscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.subscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.subscribe(AppEvent.USUARIO_DESACTIVADO, this);
        actualizarContador();
    }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getNombreCompleto());
    }

    @Override
    public void onEvent(AppEvent event, UsuarioDTO data) {
        actualizarContador();
    }

    @FXML
    private void cerrarSesion() {
        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión",
                400, 300
        );
    }

    @FXML
    private void irAUsuarios() {
        stageInitializer.cambiarVista(
                "/view/fxml/usuarios/lista-usuarios.fxml",
                "Piedrazul - Gestión de Usuarios",
                1000, 650
        );
    }

    private void actualizarContador() {
        int total = usuarioService.listarTodos().size();
        lblTotalUsuarios.setText(String.valueOf(total));
    }
}