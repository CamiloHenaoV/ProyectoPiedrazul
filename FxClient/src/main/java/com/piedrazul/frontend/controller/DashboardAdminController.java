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
 * Métodos añadidos para HU-1.5 / HU-1.6 / HU-1.7 / HU-1.8:
 *   - irADisponibilidad()           → ConfiguracionDisponibilidadController
 *   - irAConfiguracionAgendamiento() → ConfiguracionAgendamientoController
 *   - irADiasNoDisponibles()        → DiasNoDisponiblesController
 */
public class DashboardAdminController implements Observer<UsuarioDTO> {

    @FXML private Label lblUsuario;
    @FXML private Label lblTotalUsuarios;

    private final UsuarioClient   usuarioClient;
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
        Platform.runLater(this::actualizarContador);
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    @FXML
    private void cerrarSesion() {
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
     * HU-1.5 / HU-1.6: navega a la pantalla de configuración de
     * disponibilidad semanal de médicos y terapistas.
     */
    @FXML
    private void irADisponibilidad() {
        stageInitializer.cambiarVista(
                "/view/fxml/configuracion/configuracion-disponibilidad.fxml",
                "Piedrazul - Disponibilidad de Profesionales", 900, 620);
    }

    /**
     * HU-1.7: navega a la pantalla de configuración de la ventana
     * de tiempo habilitada para agendamiento.
     */
    @FXML
    private void irAConfiguracionAgendamiento() {
        stageInitializer.cambiarVista(
                "/view/fxml/configuracion/configuracion-agendamiento.fxml",
                "Piedrazul - Ventana de Agendamiento", 700, 460);
    }

    /**
     * HU-1.8: navega a la pantalla de gestión de días no disponibles y festivos.
     */
    @FXML
    private void irADiasNoDisponibles() {
        stageInitializer.cambiarVista(
                "/view/fxml/configuracion/dias-no-disponibles.fxml",
                "Piedrazul - Días No Disponibles y Festivos", 900, 580);
    }

    // ── Contador de usuarios ──────────────────────────────────────────────────

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
