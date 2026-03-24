package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.exceptions.CredencialesInvalidasException;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
/**
 * Controlador JavaFX para la vista de inicio de sesión ({@code /view/fxml/auth/login.fxml}).
 *
 * <p>Gestiona la autenticación del usuario contra {@link IUsuarioService} y redirige
 * a la vista correspondiente según el rol obtenido. La operación de autenticación se
 * ejecuta en un hilo secundario para no bloquear el hilo de la interfaz gráfica (JavaFX
 * Application Thread); los cambios de UI posteriores se despachan con
 * {@link Platform#runLater(Runnable)}.</p>
 *
 * <p>Roles soportados en esta versión:
 * <ul>
 *   <li>{@code administrador} → {@code dashboard-admin.fxml} (900 × 600)</li>
 *   <li>{@code paciente}      → {@code dashboard-paciente.fxml} (900 × 600)</li>
 * </ul>
 * Cualquier otro rol muestra un mensaje de error informando que no está disponible.</p>
 *
 * <p>El controlador es un bean de Spring ({@link Component}) inyectado por
 * {@code context::getBean} desde {@link StageInitializer}; recibe sus dependencias
 * mediante inyección por constructor.</p>
 *
 * @see IUsuarioService#autenticar(String, String)
 * @see StageInitializer#cambiarVistaConLoader(String, String, double, double)
 */
// HU 5.1 - validacion de autenticacion
@Component
public class LoginController {

    private final IUsuarioService usuarioService;
    private final StageInitializer stageInitializer;

    /**
     * Construye el controlador con las dependencias requeridas.
     *
     * @param usuarioService   servicio de autenticación y gestión de usuarios
     * @param stageInitializer gestor de navegación entre vistas JavaFX
     */
    public LoginController(IUsuarioService usuarioService, StageInitializer stageInitializer) {
        this.usuarioService   = usuarioService;
        this.stageInitializer = stageInitializer;
    }

    @FXML private TextField     txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnIngresar;
    @FXML private Label         lblError;
    @FXML private TextField txtPasswordVisible;
    @FXML private CheckBox chkMostrarPassword;

    private boolean passwordVisible = false;
    /**
        * Inicializa la vista una vez que el FXML ha sido cargado.
     *
     * <p>Oculta la etiqueta de error y registra un atajo de teclado en
     * {@code txtPassword} para que pulsar {@code Enter} invoque {@link #handleLogin()}
     * sin necesidad de hacer clic en el botón.</p>
     */
    @FXML
    public void initialize() {
        lblError.setVisible(false);
        txtPassword.setOnAction(e -> handleLogin());
    }

    /**
     * Maneja el intento de inicio de sesión iniciado por el usuario.
     *
     * <p>Flujo de ejecución:
     * <ol>
     *   <li>Valida que ningún campo esté vacío; si alguno lo está muestra un error y aborta.</li>
     *   <li>Deshabilita {@code btnIngresar} para evitar envíos duplicados.</li>
     *   <li>Lanza un hilo secundario que llama a {@link IUsuarioService#autenticar(String, String)}.</li>
     *   <li>En caso de éxito, regresa al JavaFX Application Thread con
     *       {@link Platform#runLater(Runnable)} y delega en {@link #navegarSegunRol(UsuarioDTO)}.</li>
     *   <li>En caso de {@link CredencialesInvalidasException}, muestra el mensaje de credenciales
     *       incorrectas; cualquier otra excepción muestra un error genérico.</li>
     *   <li>El bloque {@code finally} rehabilita el botón siempre, independientemente del resultado.</li>
     * </ol>
     * </p>
     */
    @FXML
    public void handleLogin() {
        String login    = txtLogin.getText().trim();
        String password = passwordVisible ? txtPasswordVisible.getText() : txtPassword.getText();

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

    /**
     * Redirige al dashboard correspondiente según el rol del usuario autenticado.
     *
     * <p>Usa {@link StageInitializer#cambiarVistaConLoader(String, String, double, double)} para
     * obtener el {@link FXMLLoader} tras la carga del FXML y así pasar el {@link UsuarioDTO}
     * al controlador destino mediante su método {@code setUsuarioActual}.</p>
     *
     * <p>Roles y destinos:
     * <ul>
     *   <li>{@code administrador} → {@code DashboardAdminController#setUsuarioActual(UsuarioDTO)}</li>
     *   <li>{@code paciente}      → {@code DashboardPacienteController#setUsuarioActual(UsuarioDTO)}</li>
     *   <li>Cualquier otro rol    → mensaje de error en la vista de login.</li>
     * </ul>
     * </p>
     *
     * @param usuario DTO del usuario autenticado, con su rol definido
     */

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
    /**
     * Muestra un mensaje de error en la etiqueta {@code lblError} y la hace visible.
     *
     * @param mensaje texto descriptivo del error a mostrar al usuario
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
