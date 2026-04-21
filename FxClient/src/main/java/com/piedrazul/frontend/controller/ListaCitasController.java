package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.CitaClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.http.SessionManager;
import com.piedrazul.frontend.model.dto.CitaDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.enums.EstadoCita;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
import com.piedrazul.frontend.observer.Observer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controlador de listado de citas del paciente.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, ICitaService, IUsuarioService (locales)
 * - Añadido:   CitaClient, UsuarioClient (HTTP), SessionManager
 * - setUsuarioActual() resuelve el pacienteId vía HTTP async
 * - cargarCitas() → GET /citas/paciente/{pacienteId} async
 * - handleCancelar() → PATCH /citas/{id}/cancelar async
 */
public class ListaCitasController implements Observer<CitaDTO> {

    @FXML private TableView<CitaDTO>           tablaCitas;
    @FXML private TableColumn<CitaDTO, String> colFecha;
    @FXML private TableColumn<CitaDTO, String> colHora;
    @FXML private TableColumn<CitaDTO, String> colProfesional;
    @FXML private TableColumn<CitaDTO, String> colEstado;
    @FXML private Button                       btnCancelar;
    @FXML private Button                       btnDetalle;
    @FXML private Label                        lblEstado;

    private final CitaClient     citaClient;
    private final UsuarioClient  usuarioClient;
    private final EventBus       eventBus;
    private final StageInitializer stageInitializer;
    private final SessionManager   session;

    private Long pacienteId;
    private UsuarioDTO usuarioActual;

    private static final DateTimeFormatter HORA_FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-CO"))
                    .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FECHA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    public ListaCitasController(CitaClient citaClient,
                                UsuarioClient usuarioClient,
                                EventBus eventBus,
                                StageInitializer stageInitializer,
                                SessionManager session) {
        this.citaClient      = citaClient;
        this.usuarioClient   = usuarioClient;
        this.eventBus        = eventBus;
        this.stageInitializer = stageInitializer;
        this.session          = session;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSeleccion();
        eventBus.subscribe(AppEvent.CITA_AGENDADA,  this);
        eventBus.subscribe(AppEvent.CITA_CANCELADA, this);
    }

    /**
     * CAMBIO: antes usuarioService.buscarPacienteIdPorUsuarioId() (local síncrono)
     * Ahora: GET /usuarios/{id}/paciente-id en hilo secundario; carga citas al terminar.
     */
    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        this.pacienteId = usuario.getId();
        cargarCitas();
    }

    @Override
    public void onEvent(AppEvent event, CitaDTO data) {
        Platform.runLater(this::cargarCitas);
    }

    @FXML
    private void handleCancelar() {
        CitaDTO sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar cita");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que deseas cancelar esta cita?");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                btnCancelar.setDisable(true);
                new Thread(() -> {
                    try {
                        // CAMBIO: antes citaService.cancelarCita(id) (local)
                        // Ahora: PATCH /citas/{id}/cancelar
                        citaClient.cancelarCita(sel.getId());
                        eventBus.publish(AppEvent.CITA_CANCELADA, sel);
                        Platform.runLater(() -> {
                            lblEstado.setText("Cita cancelada correctamente.");
                            cargarCitas();
                        });
                    } catch (HttpException ex) {
                        Platform.runLater(() -> lblEstado.setText(
                                "Error al cancelar (" + ex.getStatusCode() + ")."));
                    } catch (Exception e) {
                        Platform.runLater(() -> lblEstado.setText("Error al cancelar la cita."));
                    } finally {
                        Platform.runLater(() -> btnCancelar.setDisable(false));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleDetalle() {
        CitaDTO sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert detalle = new Alert(Alert.AlertType.INFORMATION);
        detalle.setTitle("Detalle de cita");
        detalle.setHeaderText("Información de la cita");
        detalle.setContentText(
                "Profesional: " + sel.getProfesionalNombre() + "\n" +
                        "Fecha:       " + sel.getFechaHora().format(FECHA_FMT) + "\n" +
                        "Hora:        " + sel.getFechaHora().format(HORA_FMT)  + "\n" +
                        "Estado:      " + sel.getEstado().name()
        );
        detalle.showAndWait();
    }

    @FXML
    private void volver() {
        eventBus.unsubscribe(AppEvent.CITA_AGENDADA,  this);
        eventBus.unsubscribe(AppEvent.CITA_CANCELADA, this);
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/dashboard/dashboard-paciente.fxml",
                "Piedrazul - Mi Portal", 900, 600);
        DashboardPacienteController ctrl = loader.getController();
        if (usuarioActual != null) ctrl.setUsuarioActual(usuarioActual);
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFechaHora().format(FECHA_FMT)));
        colHora.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFechaHora().format(HORA_FMT)));
        colProfesional.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getProfesionalNombre()));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstado().name()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setText(null); setStyle(""); }
                else {
                    setText(estado);
                    setStyle(switch (EstadoCita.valueOf(estado)) {
                        case programada -> "-fx-text-fill: #2196F3;";
                        case completada -> "-fx-text-fill: #4CAF50;";
                        case cancelada  -> "-fx-text-fill: #F44336;";
                    });
                }
            }
        });
    }

    private void configurarSeleccion() {
        tablaCitas.getSelectionModel().selectedItemProperty().addListener(
                (obs, ant, act) -> {
                    boolean hay       = act != null;
                    boolean cancelable = hay && act.getEstado() == EstadoCita.programada;
                    btnDetalle.setDisable(!hay);
                    btnCancelar.setDisable(!cancelable);
                });
    }

    /**
     * CAMBIO: antes citaService.listarPorPaciente(pacienteId) (local síncrono)
     * Ahora: GET /citas/paciente/{pacienteId} en hilo secundario.
     */
    private void cargarCitas() {
        if (pacienteId == null) return;
        new Thread(() -> {
            try {
                List<CitaDTO> lista = citaClient.listarPorPaciente(pacienteId);
                Platform.runLater(() -> {
                    ObservableList<CitaDTO> citas = FXCollections.observableArrayList(lista);
                    tablaCitas.setItems(citas);
                    lblEstado.setText("Total: " + citas.size() + " citas");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEstado.setText("Error al cargar citas."));
            }
        }).start();
    }
}