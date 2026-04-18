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
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, @Autowired, IAuthService, IUsuarioService
 * - Añadido:   AuthClient (HTTP), SessionManager (guarda el JWT)
 * - El token JWT se guarda en SessionManager tras login exitoso.
 * - Los errores ahora son HttpException con códigos HTTP (401, 503, etc.)
 */
public class LoginController {

    @FXML private TextField     txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtPasswordVisible;
    @FXML private CheckBox      chkMostrarPassword;
    @FXML private Button        btnIngresar;
    @FXML private Label         lblError;

    private final AuthClient     authClient;
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

                // Guardar JWT en SessionManager para que ApiClient lo añada
                // automáticamente a todas las peticiones siguientes
                session.setToken(respuesta.getAccessToken());

                UsuarioDTO usuario = new UsuarioDTO();
                usuario.setId(respuesta.getUsuarioId());
                usuario.setLogin(respuesta.getLogin());
                usuario.setRol(RolUsuario.valueOf(respuesta.getRol()));
                usuario.setActivo(true);
                session.setUsuarioActual(usuario);

                Platform.runLater(() -> navegarSegunRol(usuario));

            } catch (HttpException ex) {
                Platform.runLater(() -> {
                    if (ex.isUnauthorized())
                        mostrarError("Usuario o contraseña incorrectos.");
                    else if (ex.isUnavailable())
                        mostrarError("Servicio no disponible. Intenta más tarde.");
                    else
                        mostrarError("Error inesperado (" + ex.getStatusCode() + "). Intenta de nuevo.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> mostrarError("No se pudo conectar con el servidor."));
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
            default -> mostrarError("Rol no soportado en esta versión.");
        }
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

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
