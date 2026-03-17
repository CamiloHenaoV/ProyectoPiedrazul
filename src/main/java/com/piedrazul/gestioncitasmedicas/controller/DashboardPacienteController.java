package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;
import javafx.fxml.FXMLLoader;
import com.piedrazul.gestioncitasmedicas.controller.AgendarCitaController;

/**
 * Controlador JavaFX del portal del paciente
 * ({@code /view/fxml/dashboard/dashboard-paciente.fxml}).
 *
 * <p>Presenta al paciente autenticado un saludo personalizado y ofrece acceso
 * a las funcionalidades disponibles para su rol: agendar citas y cerrar sesión.</p>
 *
 * <p>El DTO del usuario se recibe desde {@link LoginController} mediante
 * {@link #setUsuarioActual(UsuarioDTO)}, llamado inmediatamente después de que
 * JavaFX carga el FXML. Este controlador no implementa {@link com.piedrazul.gestioncitasmedicas.observer.Observer}
 * porque no necesita reaccionar a eventos del sistema en su estado actual.</p>
 *
 * @see StageInitializer
 */
@Component
public class DashboardPacienteController {

    @FXML private Label lblUsuario;
    @FXML private Label lblBienvenida;

    private final StageInitializer stageInitializer;

    private UsuarioDTO usuarioActual;
    public DashboardPacienteController(StageInitializer stageInitializer){
        this.stageInitializer=stageInitializer;
    }
    /**
     * Inicializa la vista una vez que el FXML ha sido cargado.
     *
     * <p>No realiza ninguna operación en este momento. Las etiquetas de bienvenida
     * se actualizan de forma diferida en {@link #setUsuarioActual(UsuarioDTO)}.</p>
     */
    @FXML
    public void initialize() { }

    /**
     * Recibe el usuario autenticado y personaliza las etiquetas de la vista.
     *
     * <p>Establece {@code lblUsuario} con el nombre completo del paciente y
     * {@code lblBienvenida} con el saludo {@code "Hola, <nombreCompleto>"}.</p>
     *
     * <p>Es invocado por {@link LoginController} tras cargar esta vista con
     * {@link StageInitializer#cambiarVistaConLoader(String, String, double, double)}.</p>
     *
     * @param usuario DTO del paciente autenticado; no debe ser {@code null}
     */
    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        lblUsuario.setText(usuario.getNombreCompleto());
        lblBienvenida.setText("Hola, " + usuario.getNombreCompleto());
    }

    /**
     * Navega a la vista de agendamiento de citas
     * ({@code /view/fxml/citas/agendar-cita.fxml}, 800 × 550).
     *
     * <p><strong>Nota:</strong> la vista de destino aún está en desarrollo.
     * Su controlador será {@code AgendarCitaController}.</p>
     */
    @FXML
    private void irAAgendarCita() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/agendar-cita.fxml",
                "Piedrazul - Agendar Cita",
                800, 550
        );
        AgendarCitaController controller = loader.getController();
        controller.setUsuarioActual(usuarioActual);
    }

    /**
     * Cierra la sesión del paciente y regresa a la pantalla de login
     * ({@code /view/fxml/auth/login.fxml}, 400 × 300).
     */
    @FXML
    private void cerrarSesion() {
        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión",
                400, 300
        );
    }
}