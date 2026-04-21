package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
import com.piedrazul.frontend.observer.Observer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador del dashboard del administrador.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, IUsuarioService (local)
 * - Añadido:   UsuarioClient (HTTP a user-service)
 * - actualizarContador() ahora hace GET /usuarios/count/activos
 * - Los errores de red se manejan silenciosamente (muestra "—")
 */
public class DashboardAdminController implements Observer<UsuarioDTO> {

    @FXML private Label lblUsuario;
    @FXML private Label lblTotalUsuarios;

    private final UsuarioClient  usuarioClient;
    private final StageInitializer stageInitializer;
    private final EventBus         eventBus;

    public DashboardAdminController(UsuarioClient usuarioClient,
                                    StageInitializer stageInitializer,
                                    EventBus eventBus) {
        this.usuarioClient   = usuarioClient;
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
        lblUsuario.setText(usuario.getNombreCompleto());
    }

    @Override
    public void onEvent(AppEvent event, UsuarioDTO data) {
        // El EventBus puede publicar desde cualquier hilo; asegurar hilo UI
        Platform.runLater(this::actualizarContador);
    }

    @FXML
    private void cerrarSesion() {
        // Desuscribir antes de salir para evitar referencias colgantes
        eventBus.unsubscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.unsubscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.unsubscribe(AppEvent.USUARIO_DESACTIVADO, this);

        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión", 400, 300);
    }

    @FXML
    private void irAUsuarios() {
        stageInitializer.cambiarVista(
                "/view/fxml/usuarios/lista-usuarios.fxml",
                "Piedrazul - Gestión de Usuarios", 1000, 650);
    }

    /**
     * Llama al user-service vía HTTP.
     * Antes: usuarioService.contarUsuariosActivos() (llamada local)
     * Ahora: GET /usuarios/count/activos al API Gateway
     */
    private void actualizarContador() {
        new Thread(() -> {
            try {
                long total = usuarioClient.contarActivos();
                Platform.runLater(() -> lblTotalUsuarios.setText(String.valueOf(total)));
            } catch (Exception e) {
                Platform.runLater(() -> lblTotalUsuarios.setText("—"));
            }
        }).start();
    }
}
