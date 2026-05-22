package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.DisponibilidadClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.DisponibilidadSemanalDTO;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controlador para la pantalla de configuración de disponibilidad de profesionales.
 *
 * HU-1.5: configurar días habilitados y franjas horarias.
 * HU-1.6: definir intervalos de atención entre citas.
 *
 * Flujo:
 *   1. El admin selecciona un profesional del ComboBox.
 *   2. La tabla muestra sus disponibilidades actuales.
 *   3. El admin llena el formulario y presiona GUARDAR.
 *   4. Puede editar (doble clic en la tabla) o eliminar una fila.
 */
public class ConfiguracionDisponibilidadController {

    // ── Campos del formulario ─────────────────────────────────────────────────
    @FXML private ComboBox<ProfesionalDTO>          cbProfesional;
    @FXML private ComboBox<String>                  cbDia;
    @FXML private TextField                         txtHoraInicio;
    @FXML private TextField                         txtHoraFin;
    @FXML private Spinner<Integer>                  spnIntervalo;
    @FXML private Button                            btnGuardar;
    @FXML private Button                            btnEliminar;
    @FXML private Button                            btnLimpiar;
    @FXML private Label                             lblMensaje;

    // ── Tabla ─────────────────────────────────────────────────────────────────
    @FXML private TableView<DisponibilidadSemanalDTO>        tblDisponibilidad;
    @FXML private TableColumn<DisponibilidadSemanalDTO, String>  colDia;
    @FXML private TableColumn<DisponibilidadSemanalDTO, String>  colInicio;
    @FXML private TableColumn<DisponibilidadSemanalDTO, String>  colFin;
    @FXML private TableColumn<DisponibilidadSemanalDTO, Integer> colIntervalo;

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final DisponibilidadClient disponibilidadClient;
    private final UsuarioClient        usuarioClient;
    private final StageInitializer     stageInitializer;

    // ── Estado ────────────────────────────────────────────────────────────────
    private final ObservableList<DisponibilidadSemanalDTO> disponibilidades =
            FXCollections.observableArrayList();
    private Long idEditando = null;   // null = crear; !null = editar

    private static final String[] DIAS = {
        "Domingo","Lunes","Martes","Miércoles","Jueves","Viernes","Sábado"
    };

    public ConfiguracionDisponibilidadController(DisponibilidadClient disponibilidadClient,
                                                  UsuarioClient usuarioClient,
                                                  StageInitializer stageInitializer) {
        this.disponibilidadClient = disponibilidadClient;
        this.usuarioClient        = usuarioClient;
        this.stageInitializer     = stageInitializer;
    }

    @FXML
    public void initialize() {
        configurarTabla();
        configurarFormulario();
        cargarProfesionales();
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    private void configurarTabla() {
        colDia.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getNombreDia()));
        colInicio.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getHoraInicio() != null
                                ? c.getValue().getHoraInicio().toString() : ""));
        colFin.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getHoraFin() != null
                                ? c.getValue().getHoraFin().toString() : ""));
        colIntervalo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        c.getValue().getDuracionCitaMinutos()));

        tblDisponibilidad.setItems(disponibilidades);

        // Doble clic → cargar en formulario para editar
        tblDisponibilidad.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) cargarEnFormulario();
        });
    }

    private void configurarFormulario() {
        cbDia.setItems(FXCollections.observableArrayList(DIAS));
        cbDia.getSelectionModel().selectFirst();

        spnIntervalo.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 240, 30, 5));
    }

    private void cargarProfesionales() {
        new Thread(() -> {
            try {
                List<ProfesionalDTO> profesionales = usuarioClient.listarProfesionales();
                Platform.runLater(() -> {
                    cbProfesional.setItems(FXCollections.observableArrayList(profesionales));
                    cbProfesional.setConverter(new javafx.util.StringConverter<>() {
                        public String toString(ProfesionalDTO p) {
                            return p == null ? "" : p.getNombreCompleto();
                        }
                        public ProfesionalDTO fromString(String s) { return null; }
                    });
                });
            } catch (Exception ex) {
                mostrarError("No se pudo cargar la lista de profesionales.");
            }
        }).start();
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    @FXML
    private void onProfesionalSeleccionado() {
        ProfesionalDTO prof = cbProfesional.getValue();
        if (prof != null) cargarDisponibilidades(prof.getId());
    }

    private void cargarDisponibilidades(Long profesionalId) {
        new Thread(() -> {
            try {
                List<DisponibilidadSemanalDTO> lista =
                        disponibilidadClient.listarPorProfesional(profesionalId);
                Platform.runLater(() -> {
                    disponibilidades.setAll(lista);
                    limpiarFormulario();
                });
            } catch (Exception ex) {
                mostrarError("Error al cargar disponibilidades.");
            }
        }).start();
    }

    /**
     * HU-1.5 SC-1/SC-2/SC-3/SC-4 + HU-1.6 SC-1/SC-2: guardar configuración.
     */
    @FXML
    private void onGuardar() {
        if (!validarCamposLocales()) return;

        ProfesionalDTO prof = cbProfesional.getValue();
        DisponibilidadSemanalDTO dto = construirDTO(prof.getId());

        new Thread(() -> {
            try {
                DisponibilidadSemanalDTO resultado;
                if (idEditando == null) {
                    // HU-1.5 SC-1: crear nueva configuración
                    resultado = disponibilidadClient.crearDisponibilidad(dto);
                } else {
                    // HU-1.6 SC-3: actualizar con recálculo
                    resultado = disponibilidadClient.actualizarDisponibilidad(idEditando, dto);
                }

                final DisponibilidadSemanalDTO r = resultado;
                Platform.runLater(() -> {
                    if (idEditando == null) {
                        disponibilidades.add(r);
                    } else {
                        int idx = disponibilidades.stream()
                                .filter(d -> d.getId().equals(idEditando))
                                .findFirst()
                                .map(disponibilidades::indexOf)
                                .orElse(-1);
                        if (idx >= 0) disponibilidades.set(idx, r);
                    }
                    mostrarExito(idEditando == null
                            ? "Disponibilidad guardada correctamente."
                            : "Disponibilidad actualizada. Las citas futuras existentes se respetarán.");
                    limpiarFormulario();
                });
            } catch (HttpException ex) {
                // HU-1.5 SC-2 / HU-1.6 SC-2: mensaje de validación del servidor
                mostrarError(ex.getMessage());
            } catch (Exception ex) {
                mostrarError("Error al guardar: " + ex.getMessage());
            }
        }).start();
    }

    @FXML
    private void onEliminar() {
        DisponibilidadSemanalDTO seleccionada =
                tblDisponibilidad.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccione una disponibilidad para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la disponibilidad del " + seleccionada.getNombreDia() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        disponibilidadClient.eliminarDisponibilidad(seleccionada.getId());
                        Platform.runLater(() -> {
                            disponibilidades.remove(seleccionada);
                            mostrarExito("Disponibilidad eliminada.");
                            limpiarFormulario();
                        });
                    } catch (Exception ex) {
                        mostrarError("Error al eliminar.");
                    }
                }).start();
            }
        });
    }

    @FXML
    private void onLimpiar() {
        limpiarFormulario();
    }

    @FXML
    private void onVolver() {
        stageInitializer.cambiarVista(
                "/view/fxml/dashboard/dashboard-admin.fxml",
                "Piedrazul - Panel de Administración", 900, 600);
    }

    // ── Validación local (HU-1.5 SC-2) ───────────────────────────────────────

    private boolean validarCamposLocales() {
        if (cbProfesional.getValue() == null) {
            mostrarError("Seleccione un profesional.");
            return false;
        }
        if (cbDia.getValue() == null) {
            mostrarError("Seleccione un día de atención.");
            return false;
        }
        if (txtHoraInicio.getText().isBlank()) {
            mostrarError("La hora de inicio es obligatoria.");
            return false;
        }
        if (txtHoraFin.getText().isBlank()) {
            mostrarError("La hora de fin es obligatoria.");
            return false;
        }
        try {
            LocalTime inicio = LocalTime.parse(txtHoraInicio.getText().trim());
            LocalTime fin    = LocalTime.parse(txtHoraFin.getText().trim());
            if (!fin.isAfter(inicio)) {
                mostrarError("La hora de fin debe ser posterior a la hora de inicio.");
                return false;
            }
            // HU-1.6 SC-2 local: verificar que cabe al menos una cita
            long minutos = java.time.Duration.between(inicio, fin).toMinutes();
            if (minutos < spnIntervalo.getValue()) {
                mostrarError("El intervalo de " + spnIntervalo.getValue() +
                        " min no cabe en la franja de " + minutos + " min.");
                return false;
            }
        } catch (DateTimeParseException e) {
            mostrarError("Formato de hora inválido. Use HH:mm (ej.: 08:00).");
            return false;
        }
        return true;
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private DisponibilidadSemanalDTO construirDTO(Long profesionalId) {
        DisponibilidadSemanalDTO dto = new DisponibilidadSemanalDTO();
        dto.setProfesionalId(profesionalId);
        dto.setDiaSemana(cbDia.getSelectionModel().getSelectedIndex());
        dto.setHoraInicio(LocalTime.parse(txtHoraInicio.getText().trim()));
        dto.setHoraFin(LocalTime.parse(txtHoraFin.getText().trim()));
        dto.setDuracionCitaMinutos(spnIntervalo.getValue());
        return dto;
    }

    private void cargarEnFormulario() {
        DisponibilidadSemanalDTO sel =
                tblDisponibilidad.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        idEditando = sel.getId();
        cbDia.getSelectionModel().select(sel.getDiaSemana());
        txtHoraInicio.setText(sel.getHoraInicio() != null ? sel.getHoraInicio().toString() : "");
        txtHoraFin.setText(sel.getHoraFin() != null ? sel.getHoraFin().toString() : "");
        spnIntervalo.getValueFactory().setValue(
                sel.getDuracionCitaMinutos() != null ? sel.getDuracionCitaMinutos() : 30);
        btnGuardar.setText("Actualizar");
        lblMensaje.setText("");
    }

    private void limpiarFormulario() {
        idEditando = null;
        cbDia.getSelectionModel().selectFirst();
        txtHoraInicio.clear();
        txtHoraFin.clear();
        spnIntervalo.getValueFactory().setValue(30);
        btnGuardar.setText("Guardar");
        lblMensaje.setText("");
        tblDisponibilidad.getSelectionModel().clearSelection();
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
