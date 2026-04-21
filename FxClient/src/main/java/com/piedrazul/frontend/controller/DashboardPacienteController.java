package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

/**
 * Controlador del portal del paciente.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component
 * - Sin cambios de lógica: este controlador no llamaba servicios directamente,
 *   solo navegaba. La migración es estructural (sin Spring).
 */
public class DashboardPacienteController {

    @FXML private Label lblUsuario;
    @FXML private Label lblBienvenida;

    private final StageInitializer stageInitializer;
    private UsuarioDTO usuarioActual;

    public DashboardPacienteController(StageInitializer stageInitializer) {
        this.stageInitializer = stageInitializer;
    }

    @FXML
    public void initialize() { }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getNombreCompleto());
        lblBienvenida.setText("Hola, " + usuario.getNombreCompleto());
    }

    @FXML
    private void irAAgendarCita() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/agendar-cita.fxml",
                "Piedrazul - Agendar Cita", 800, 550);
        AgendarCitaController ctrl = loader.getController();
        ctrl.setUsuarioActual(usuarioActual);
    }

    @FXML
    private void irAMisCitas() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/lista-citas.fxml",
                "Piedrazul - Mis Citas", 800, 550);
        ListaCitasController ctrl = loader.getController();
        ctrl.setUsuarioActual(usuarioActual);
    }

    @FXML
    private void cerrarSesion() {
        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión", 400, 300);
    }
}
