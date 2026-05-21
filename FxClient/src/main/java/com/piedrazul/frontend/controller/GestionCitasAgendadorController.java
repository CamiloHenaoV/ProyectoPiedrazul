package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.CitaClient;
import com.piedrazul.frontend.client.EspecialidadClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.CitaDTO;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.enums.EstadoCita;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controlador principal del agendador de citas.
 *
 * HU-6.1 – Consultar citas de un profesional en una fecha (búsqueda + tabla).
 * HU-6.3 – Modificar / reprogramar una cita seleccionada.
 * HU-6.4 – Cancelar una cita con confirmación, restricción de estado y notificación.
 *
 * La acción de registrar cita manual (HU-6.2) navega a RegistroCitaManualController.
 */
public class GestionCitasAgendadorController {

    // ── Búsqueda (HU-6.1) ────────────────────────────────────────────────────
    @FXML private ComboBox<String>         cbEspecialidad;
    @FXML private ComboBox<ProfesionalDTO> cbProfesional;
    @FXML private DatePicker               dpFecha;
    @FXML private Button                   btnBuscar;

    // ── Resultados (HU-6.1) ──────────────────────────────────────────────────
    @FXML private TableView<CitaDTO>           tablaCitas;
    @FXML private TableColumn<CitaDTO, String> colPaciente;
    @FXML private TableColumn<CitaDTO, String> colHora;
    @FXML private TableColumn<CitaDTO, String> colEstado;
    @FXML private Label                        lblTotalCitas;

    // ── Acciones ─────────────────────────────────────────────────────────────
    @FXML private Button btnRegistrarNueva;   // HU-6.2
    @FXML private Button btnModificar;        // HU-6.3
    @FXML private Button btnCancelar;         // HU-6.4
    @FXML private Label  lblMensaje;

    // ── Dependencias ─────────────────────────────────────────────────────────
    private final CitaClient         citaClient;
    private final EspecialidadClient especialidadClient;
    private final StageInitializer   stageInitializer;
    private final EventBus           eventBus;

    private UsuarioDTO usuarioActual;

    private static final DateTimeFormatter HORA_FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-CO"))
                    .withZone(ZoneId.systemDefault());

    public GestionCitasAgendadorController(CitaClient citaClient,
                                           EspecialidadClient especialidadClient,
                                           StageInitializer stageInitializer,
                                           EventBus eventBus) {
        this.citaClient         = citaClient;
        this.especialidadClient = especialidadClient;
        this.stageInitializer   = stageInitializer;
        this.eventBus           = eventBus;
    }

    @FXML
    public void initialize() {
        configurarTabla();
        configurarComboProfesional();
        configurarSeleccion();

        // Bloquear fechas pasadas en el selector
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().minusDays(365)));
            }
        });

        // Cascada especialidad → profesional
        cbEspecialidad.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> onEspecialidadSeleccionada(act));

        cargarEspecialidades();
    }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
    }

    // ── HU-6.1: Buscar ───────────────────────────────────────────────────────

    /**
     * HU-6.1 CA-3: Valida que profesional y fecha estén seleccionados antes de buscar.
     */
    @FXML
    private void handleBuscar() {
        ProfesionalDTO profesional = cbProfesional.getSelectionModel().getSelectedItem();
        LocalDate      fecha       = dpFecha.getValue();

        // CA-3: campos obligatorios
        if (profesional == null || fecha == null) {
            mostrarMensaje("Los campos Profesional y Fecha son obligatorios para realizar la búsqueda.", false);
            return;
        }

        lblMensaje.setText("Buscando citas...");
        tablaCitas.setItems(FXCollections.observableArrayList());
        lblTotalCitas.setText("");

        new Thread(() -> {
            try {
                List<CitaDTO> citas = citaClient.listarPorProfesionalYFecha(
                        profesional.getId(), fecha);

                Platform.runLater(() -> {
                    ObservableList<CitaDTO> obs = FXCollections.observableArrayList(citas);
                    tablaCitas.setItems(obs);

                    // CA-1 / CA-4: mostrar total y tabla
                    lblTotalCitas.setText("Total de citas: " + citas.size());

                    // CA-2: mensaje cuando no hay citas
                    if (citas.isEmpty()) {
                        mostrarMensaje("No existen citas programadas para la fecha seleccionada.", false);
                    } else {
                        lblMensaje.setText("");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        mostrarMensaje("Error al buscar citas. Verifique la conexión.", false));
            }
        }).start();
    }

    // ── HU-6.2: Navegar a registro manual ────────────────────────────────────

    @FXML
    private void handleRegistrarNueva() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/registro-cita-manual.fxml",
                "Piedrazul - Registrar Cita", 900, 620);
        RegistroCitaManualController ctrl = loader.getController();
        if (usuarioActual != null) ctrl.setUsuarioActual(usuarioActual);
    }

    // ── HU-6.3: Modificar / Reprogramar ──────────────────────────────────────

    /**
     * HU-6.3 CA-1 / CA-2 / CA-3: Navega a la vista de reprogramación.
     * Solo habilitado cuando hay una cita programada seleccionada.
     */
    @FXML
    private void handleModificar() {
        CitaDTO sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/reprogramar-cita.fxml",
                "Piedrazul - Reprogramar Cita", 900, 580);
        ReprogramarCitaController ctrl = loader.getController();
        ctrl.setCitaACambiar(sel, usuarioActual);
    }

    // ── HU-6.4: Cancelar ─────────────────────────────────────────────────────

    /**
     * HU-6.4 CA-2: Solicita confirmación antes de cancelar.
     * HU-6.4 CA-4: El botón solo está activo para citas en estado programada,
     *              por lo que el backend rechazará cualquier intento sobre citas ya atendidas.
     */
    @FXML
    private void handleCancelar() {
        CitaDTO sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        // CA-2: confirmación explícita
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar cita");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "¿Estás seguro de que deseas cancelar la cita de " +
                sel.getPacienteNombre() + " con " + sel.getProfesionalNombre() + "?\n" +
                "Esta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(respuesta -> {
            if (respuesta != ButtonType.OK) return;

            btnCancelar.setDisable(true);
            lblMensaje.setText("Cancelando cita...");

            new Thread(() -> {
                try {
                    citaClient.cancelarCita(sel.getId());

                    // CA-3: notificación de éxito y actualización de lista
                    eventBus.publish(AppEvent.CITA_CANCELADA, sel);
                    Platform.runLater(() -> {
                        mostrarMensaje("Cita cancelada correctamente. Se notificó al paciente.", false);
                        refrescarBusqueda();
                    });

                } catch (HttpException ex) {
                    Platform.runLater(() -> {
                        if (ex.getStatusCode() == 422) {
                            // CA-4: cita ya atendida o cancelada
                            mostrarMensaje("No se puede cancelar: la cita ya fue atendida o cancelada.", true);
                        } else {
                            mostrarMensaje("Error al cancelar la cita (" + ex.getStatusCode() + ").", true);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> mostrarMensaje("Error inesperado al cancelar.", true));
                } finally {
                    Platform.runLater(() -> btnCancelar.setDisable(false));
                }
            }).start();
        });
    }

    @FXML
    private void volver() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/dashboard/dashboard-agendador.fxml",
                "Piedrazul - Dashboard", 900, 600);
        DashboardAgendadorController ctrl = loader.getController();
        if (usuarioActual != null) ctrl.setUsuarioActual(usuarioActual);
    }

    // ── Configuración interna ─────────────────────────────────────────────────

    private void configurarTabla() {
        // CA-4: columnas paciente, hora, estado (HU-6.1)
        colPaciente.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPacienteNombre()));
        colHora.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFechaHora().format(HORA_FMT)));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstado().name()));

        // Colorear estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); }
                else {
                    setText(estado);
                    setStyle(switch (EstadoCita.valueOf(estado)) {
                        case programada -> "-fx-text-fill: #1565C0; -fx-font-weight: bold;";
                        case completada -> "-fx-text-fill: #2E7D32;";
                        case cancelada  -> "-fx-text-fill: #C62828;";
                    });
                }
            }
        });

        tablaCitas.setPlaceholder(new Label("Selecciona un profesional y una fecha, luego haz clic en BUSCAR."));
    }

    private void configurarComboProfesional() {
        cbProfesional.setConverter(new StringConverter<>() {
            @Override public String toString(ProfesionalDTO p) {
                return p == null ? "" : p.getNombreCompleto() + " — " + p.getEspecialidadNombre();
            }
            @Override public ProfesionalDTO fromString(String s) { return null; }
        });
    }

    private void configurarSeleccion() {
        tablaCitas.getSelectionModel().selectedItemProperty().addListener((obs, ant, sel) -> {
            boolean hay          = sel != null;
            boolean programada   = hay && sel.getEstado() == EstadoCita.programada;
            btnModificar.setDisable(!programada);
            btnCancelar.setDisable(!programada);
        });
    }

    private void cargarEspecialidades() {
        new Thread(() -> {
            try {
                List<String> especialidades = especialidadClient.listarNombres();
                Platform.runLater(() ->
                        cbEspecialidad.setItems(FXCollections.observableArrayList(especialidades)));
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar especialidades.", true));
            }
        }).start();
    }

    private void onEspecialidadSeleccionada(String especialidad) {
        cbProfesional.setItems(FXCollections.observableArrayList());
        tablaCitas.setItems(FXCollections.observableArrayList());
        lblTotalCitas.setText("");
        if (especialidad == null) return;

        new Thread(() -> {
            try {
                List<ProfesionalDTO> profs = especialidadClient.listarActivosPorEspecialidad(especialidad);
                Platform.runLater(() ->
                        cbProfesional.setItems(FXCollections.observableArrayList(profs)));
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar profesionales.", true));
            }
        }).start();
    }

    /** Re-ejecuta la última búsqueda para reflejar cambios tras cancelar o reprogramar. */
    private void refrescarBusqueda() {
        ProfesionalDTO prof  = cbProfesional.getSelectionModel().getSelectedItem();
        LocalDate      fecha = dpFecha.getValue();
        if (prof != null && fecha != null) handleBuscar();
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #1B5E20;");
    }
}
