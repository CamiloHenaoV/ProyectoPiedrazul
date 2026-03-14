package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DashboardPacienteController {

    @FXML private Label lblUsuario;
    @FXML private Label lblBienvenida;

    @Autowired private StageInitializer stageInitializer;

    private UsuarioDTO usuarioActual;

    @FXML
    public void initialize() { }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getNombreCompleto());
        lblBienvenida.setText("Hola, " + usuario.getNombreCompleto());
    }

    @FXML
    private void irAAgendarCita() {
        stageInitializer.cambiarVista(
                "/view/fxml/citas/agendar-cita.fxml",
                "Piedrazul - Agendar Cita",
                800, 550
        );
    }

    @FXML
    private void cerrarSesion() {
        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión",
                400, 300
        );
    }
}