package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.exceptions.CredencialesInvalidasException;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    private final IUsuarioService usuarioService;
    private final StageInitializer stageInitializer;

    public LoginController(IUsuarioService usuarioService, StageInitializer stageInitializer) {
        this.usuarioService   = usuarioService;
        this.stageInitializer = stageInitializer;
    }

    @FXML private TextField     txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnIngresar;
    @FXML private Label         lblError;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        txtPassword.setOnAction(e -> handleLogin());
    }

    @FXML
    public void handleLogin() {
        String login    = txtLogin.getText().trim();
        String password = txtPassword.getText();

        lblError.setVisible(false);

        if (login.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, completa todos los campos.");
            return;
        }

        btnIngresar.setDisable(true);

        new Thread(() -> {
            try {
                UsuarioDTO usuario = usuarioService.autenticar(login, password);
                Platform.runLater(() -> navegarSegunRol(usuario));
            } catch (CredencialesInvalidasException ex) {
                Platform.runLater(() -> mostrarError("Usuario o contraseña incorrectos."));
            } catch (Exception e) {
                Platform.runLater(() -> mostrarError("Error inesperado. Intenta de nuevo."));
            } finally {
                Platform.runLater(() -> btnIngresar.setDisable(false));
            }
        }).start();
    }
    private void navegarSegunRol(UsuarioDTO usuario) {
        switch (usuario.getRol()) {
            case administrador -> {
                FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                        "/view/fxml/dashboard/dashboard-admin.fxml",
                        "Piedrazul - Dashboard",
                        900, 600
                );
                DashboardAdminController controller = loader.getController();
                controller.setUsuarioActual(usuario);
            }
            case paciente -> {
                FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                        "/view/fxml/dashboard/dashboard-paciente.fxml",
                        "Piedrazul - Mi Portal",
                        900, 600
                );
                DashboardPacienteController controller = loader.getController();
                controller.setUsuarioActual(usuario);
            }
            default -> mostrarError("Rol no soportado en esta versión.");
        }
    }
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}