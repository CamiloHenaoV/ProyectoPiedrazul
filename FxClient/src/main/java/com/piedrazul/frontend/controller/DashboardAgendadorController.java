package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

/**
 * Dashboard del agendador de citas.
 * Punto de entrada tras el login para el rol {@code agendador}.
 * Desde aquí se accede a la gestión de citas (HU-6.1 a HU-6.4).
 */
public class DashboardAgendadorController {

    @FXML private Label lblUsuario;

    private final StageInitializer stageInitializer;
    private UsuarioDTO usuarioActual;

    public DashboardAgendadorController(StageInitializer stageInitializer) {
        this.stageInitializer = stageInitializer;
    }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getNombreCompleto());
    }

    /** HU-6.1 / 6.3 / 6.4 – Navega a la vista de gestión de citas. */
    @FXML
    private void irAGestionCitas() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/gestion-citas-agendador.fxml",
                "Piedrazul - Gestión de Citas", 1050, 680);
        GestionCitasAgendadorController ctrl = loader.getController();
        if (usuarioActual != null) ctrl.setUsuarioActual(usuarioActual);
    }

    @FXML
    private void cerrarSesion() {
        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión", 400, 300);
    }
}
