package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.AuthClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.http.SessionManager;
import com.piedrazul.frontend.model.dto.LoginResponseDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.enums.RolUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

/**
 * Controlador de la vista de login.
 *
 * CAMBIOS RESPECTO A LA ITERACIÓN ANTERIOR:
 * - Añadido case {@code agendador} en el switch de routing post-login
 *   → navega a dashboard-agendador.fxml (HU-6.1 a HU-6.4).
 */
public class LoginController {

    @FXML private TextField     txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtPasswordVisible;
    @FXML private CheckBox      chkMostrarPassword;
    @FXML private Button        btnIngresar;
    @FXML private Label         lblError;

    private final AuthClient       authClient;
    private final StageInitializer stageInitializer;
    private final SessionManager   session;

    private boolean passwordVisible = false;

    public LoginController(AuthClient authClient,
                           StageInitializer stageInitializer,
                           SessionManager session) {
        this.authClient      = authClient;
        this.stageInitializer = stageInitializer;
        this.session          = session;
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        txtPassword.setOnAction(e -> handleLogin());
    }

    @FXML
    public void handleLogin() {
        String login    = txtLogin.getText().trim();
        String password = passwordVisible
                ? txtPasswordVisible.getText()
                : txtPassword.getText();

        lblError.setVisible(false);

        if (login.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, completa todos los campos.");
            return;
        }

        btnIngresar.setDisable(true);

        new Thread(() -> {
            try {
                LoginResponseDTO respuesta = authClient.login(login, password);

                session.setToken(respuesta.getAccessToken());

                UsuarioDTO usuario = new UsuarioDTO();
                usuario.setId(respuesta.getUsuarioId());
                usuario.setLogin(respuesta.getLogin());
                usuario.setNombreCompleto(respuesta.getNombreCompleto());
                usuario.setRol(RolUsuario.valueOf(respuesta.getRol()));
                usuario.setActivo(true);
                session.setUsuarioActual(usuario);

                Platform.runLater(() -> redirigirSegunRol(usuario));

            } catch (HttpException ex) {
                Platform.runLater(() -> {
                    if (ex.getStatusCode() == 401) {
                        mostrarError("Credenciales incorrectas. Verifica tu usuario y contraseña.");
                    } else {
                        mostrarError("Error al conectar con el servidor (" + ex.getStatusCode() + ").");
                    }
                    btnIngresar.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarError("No se pudo conectar. Verifica que los servicios estén en línea.");
                    btnIngresar.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleMostrarPassword() {
        passwordVisible = chkMostrarPassword.isSelected();
        if (passwordVisible) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
        }
    }

    // ── Routing por rol ───────────────────────────────────────────────────────

    private void redirigirSegunRol(UsuarioDTO usuario) {
        switch (usuario.getRol()) {

            case administrador -> {
                FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                        "/view/fxml/dashboard/dashboard-admin.fxml",
                        "Piedrazul - Dashboard", 900, 600);
                DashboardAdminController ctrl = loader.getController();
                ctrl.setUsuarioActual(usuario);
            }

            case paciente -> {
                FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                        "/view/fxml/dashboard/dashboard-paciente.fxml",
                        "Piedrazul - Mi Portal", 900, 600);
                DashboardPacienteController ctrl = loader.getController();
                ctrl.setUsuarioActual(usuario);
            }

            // HU-6.x: el rol agendador accede al panel de gestión de citas
            case agendador -> {
                FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                        "/view/fxml/dashboard/dashboard-agendador.fxml",
                        "Piedrazul - Panel Agendador", 900, 600);
                DashboardAgendadorController ctrl = loader.getController();
                ctrl.setUsuarioActual(usuario);
            }

            default -> mostrarError("Rol no soportado en esta versión.");
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
