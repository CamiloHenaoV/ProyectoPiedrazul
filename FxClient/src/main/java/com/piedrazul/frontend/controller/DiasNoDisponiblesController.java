package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.DisponibilidadClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.DiaNoDisponibleDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la pantalla de gestión de días no disponibles y festivos.
 *
 * HU-1.8:
 *   SC-1: registrar un día no disponible → bloquea agendamiento en esa fecha.
 *   SC-2: al consultar disponibilidad, el backend valida festivos registrados.
 *   SC-3: eliminar restricción → la fecha vuelve a estar disponible.
 */
public class DiasNoDisponiblesController {

    // ── Formulario ────────────────────────────────────────────────────────────
    @FXML private DatePicker               dpFecha;
    @FXML private TextField                txtMotivo;
    @FXML private ComboBox<String>         cbTipo;
    @FXML private Button                   btnRegistrar;
    @FXML private Button                   btnEliminar;
    @FXML private Label                    lblMensaje;

    // ── Tabla ─────────────────────────────────────────────────────────────────
    @FXML private TableView<DiaNoDisponibleDTO>                  tblDias;
    @FXML private TableColumn<DiaNoDisponibleDTO, String>        colFecha;
    @FXML private TableColumn<DiaNoDisponibleDTO, String>        colMotivo;
    @FXML private TableColumn<DiaNoDisponibleDTO, String>        colTipo;

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final DisponibilidadClient disponibilidadClient;
    private final StageInitializer     stageInitializer;

    private final ObservableList<DiaNoDisponibleDTO> dias =
            FXCollections.observableArrayList();

    public DiasNoDisponiblesController(DisponibilidadClient disponibilidadClient,
                                        StageInitializer stageInitializer) {
        this.disponibilidadClient = disponibilidadClient;
        this.stageInitializer     = stageInitializer;
    }

    @FXML
    public void initialize() {
        configurarTabla();
        cbTipo.setItems(FXCollections.observableArrayList("FESTIVO", "BLOQUEO_MANUAL"));
        cbTipo.getSelectionModel().select("BLOQUEO_MANUAL");
        dpFecha.setValue(LocalDate.now());
        cargarDias();
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    private void configurarTabla() {
        colFecha.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFecha() != null
                                ? c.getValue().getFecha().toString() : ""));
        colMotivo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getMotivo() != null ? c.getValue().getMotivo() : "—"));
        colTipo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getTipoLabel()));

        tblDias.setItems(dias);
    }

    private void cargarDias() {
        new Thread(() -> {
            try {
                List<DiaNoDisponibleDTO> lista = disponibilidadClient.listarDiasNoDisponibles();
                Platform.runLater(() -> dias.setAll(lista));
            } catch (Exception ex) {
                mostrarError("Error al cargar días no disponibles.");
            }
        }).start();
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    /**
     * HU-1.8 SC-1: registrar fecha como no disponible.
     */
    @FXML
    private void onRegistrar() {
        if (!validarFormulario()) return;

        DiaNoDisponibleDTO dto = new DiaNoDisponibleDTO();
        dto.setFecha(dpFecha.getValue());
        dto.setMotivo(txtMotivo.getText().trim().isEmpty() ? null : txtMotivo.getText().trim());
        dto.setTipo(cbTipo.getValue());

        new Thread(() -> {
            try {
                DiaNoDisponibleDTO guardado = disponibilidadClient.registrarDiaNoDisponible(dto);
                Platform.runLater(() -> {
                    dias.add(guardado);
                    // Ordenar por fecha
                    dias.sort((a, b) -> a.getFecha().compareTo(b.getFecha()));
                    limpiarFormulario();
                    mostrarExito("Fecha " + guardado.getFecha() +
                            " registrada como no disponible.");
                });
            } catch (HttpException ex) {
                mostrarError(ex.getMessage());
            } catch (Exception ex) {
                mostrarError("Error al registrar: " + ex.getMessage());
            }
        }).start();
    }

    /**
     * HU-1.8 SC-3: eliminar restricción → fecha habilitada nuevamente.
     */
    @FXML
    private void onEliminar() {
        DiaNoDisponibleDTO seleccionado = tblDias.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione un día para habilitar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Habilitar de nuevo la fecha " + seleccionado.getFecha() + "?\n" +
                "Los pacientes podrán agendar citas en esa fecha.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        disponibilidadClient.eliminarDiaNoDisponible(seleccionado.getId());
                        Platform.runLater(() -> {
                            dias.remove(seleccionado);
                            mostrarExito("Fecha " + seleccionado.getFecha() +
                                    " habilitada nuevamente.");
                        });
                    } catch (Exception ex) {
                        mostrarError("Error al eliminar restricción.");
                    }
                }).start();
            }
        });
    }

    @FXML
    private void onVolver() {
        stageInitializer.cambiarVista(
                "/view/fxml/dashboard/dashboard-admin.fxml",
                "Piedrazul - Panel de Administración", 900, 600);
    }

    // ── Validación ────────────────────────────────────────────────────────────

    private boolean validarFormulario() {
        if (dpFecha.getValue() == null) {
            mostrarError("La fecha es obligatoria.");
            return false;
        }
        if (cbTipo.getValue() == null) {
            mostrarError("El tipo es obligatorio.");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        dpFecha.setValue(LocalDate.now());
        txtMotivo.clear();
        cbTipo.getSelectionModel().select("BLOQUEO_MANUAL");
        lblMensaje.setText("");
    }

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
