package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.DisponibilidadClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.ConfiguracionAgendamientoDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controlador para la pantalla de configuración de la ventana de agendamiento.
 *
 * HU-1.7: el administrador define cuántas semanas hacia adelante pueden
 *         reservarse citas.
 *
 * Escenarios cubiertos:
 *   SC-1: guardar la configuración de semanas habilitadas.
 *   SC-2: el sistema bloquea fechas fuera del rango (validación en backend).
 *   SC-3: modificar la ventana → el sistema actualiza las restricciones.
 */
public class ConfiguracionAgendamientoController {

    @FXML private Spinner<Integer> spnSemanas;
    @FXML private Label            lblFechaMaxima;
    @FXML private Label            lblMensaje;
    @FXML private Button           btnGuardar;

    private final DisponibilidadClient disponibilidadClient;
    private final StageInitializer     stageInitializer;

    public ConfiguracionAgendamientoController(DisponibilidadClient disponibilidadClient,
                                                StageInitializer stageInitializer) {
        this.disponibilidadClient = disponibilidadClient;
        this.stageInitializer     = stageInitializer;
    }

    @FXML
    public void initialize() {
        spnSemanas.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 4, 1));
        spnSemanas.setEditable(true);
        cargarConfiguracion();
    }

    // ── Carga inicial ─────────────────────────────────────────────────────────

    private void cargarConfiguracion() {
        new Thread(() -> {
            try {
                ConfiguracionAgendamientoDTO config =
                        disponibilidadClient.obtenerConfiguracion();
                java.time.LocalDate fechaMax =
                        disponibilidadClient.obtenerFechaMaximaAgendamiento();

                Platform.runLater(() -> {
                    spnSemanas.getValueFactory().setValue(config.getSemanasHabilitadas());
                    lblFechaMaxima.setText("Fecha máxima actual: " + fechaMax.toString());
                    lblMensaje.setText("");
                });
            } catch (Exception ex) {
                mostrarError("Error al cargar configuración: " + ex.getMessage());
            }
        }).start();
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    /**
     * HU-1.7 SC-1/SC-3: guardar/actualizar la ventana de agendamiento.
     */
    @FXML
    private void onGuardar() {
        Integer semanas = spnSemanas.getValue();

        // Validación local
        if (semanas == null || semanas < 1 || semanas > 52) {
            mostrarError("Las semanas habilitadas deben estar entre 1 y 52.");
            return;
        }

        ConfiguracionAgendamientoDTO dto = new ConfiguracionAgendamientoDTO(null, semanas);

        new Thread(() -> {
            try {
                ConfiguracionAgendamientoDTO guardada =
                        disponibilidadClient.actualizarConfiguracion(dto);
                java.time.LocalDate nuevaFechaMax = java.time.LocalDate.now()
                        .plusWeeks(guardada.getSemanasHabilitadas());

                Platform.runLater(() -> {
                    lblFechaMaxima.setText("Fecha máxima: " + nuevaFechaMax);
                    mostrarExito("Configuración guardada. Los pacientes podrán agendar " +
                            "hasta " + nuevaFechaMax + ".");
                });
            } catch (HttpException ex) {
                mostrarError(ex.getMessage());
            } catch (Exception ex) {
                mostrarError("Error al guardar: " + ex.getMessage());
            }
        }).start();
    }

    @FXML
    private void onVolver() {
        stageInitializer.cambiarVista(
                "/view/fxml/dashboard/dashboard-admin.fxml",
                "Piedrazul - Panel de Administración", 900, 600);
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private void mostrarError(String msg) {
        Platform.runLater(() -> {
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setText(msg);
        });
    }

    private void mostrarExito(String msg) {
        Platform.runLater(() -> {
            lblMensaje.setStyle("-fx-text-fill: green;");
            lblMensaje.setText(msg);
        });
    }
}
