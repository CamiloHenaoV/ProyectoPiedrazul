package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.EstadoCita;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.ICitaService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import com.piedrazul.gestioncitasmedicas.observer.Observer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ListaCitasController implements Observer<CitaDTO> {

    @FXML private TableView<CitaDTO>           tablaCitas;
    @FXML private TableColumn<CitaDTO, String> colFecha;
    @FXML private TableColumn<CitaDTO, String> colHora;
    @FXML private TableColumn<CitaDTO, String> colProfesional;
    @FXML private TableColumn<CitaDTO, String> colEstado;
    @FXML private Button                       btnCancelar;
    @FXML private Button                       btnDetalle;
    @FXML private Label                        lblEstado;

    private final ICitaService     citaService;
    private final IUsuarioService  usuarioService;
    private final EventBus         eventBus;
    private final StageInitializer stageInitializer;

    private UUID pacienteId;

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    public ListaCitasController(ICitaService     citaService,
                                IUsuarioService  usuarioService,
                                EventBus         eventBus,
                                StageInitializer stageInitializer) {
        this.citaService     = citaService;
        this.usuarioService  = usuarioService;
        this.eventBus        = eventBus;
        this.stageInitializer = stageInitializer;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSeleccion();

        eventBus.subscribe(AppEvent.CITA_AGENDADA,  this);
        eventBus.subscribe(AppEvent.CITA_CANCELADA, this);
    }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.pacienteId = usuarioService.buscarPacienteIdPorUsuarioId(usuario.getId());
        cargarCitas();
    }

    @Override
    public void onEvent(AppEvent event, CitaDTO data) {
        Platform.runLater(this::cargarCitas);
    }

    @FXML
    private void handleCancelar() {
        CitaDTO seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar cita");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que deseas cancelar esta cita?");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    citaService.cancelarCita(seleccionada.getId());
                    lblEstado.setText("Cita cancelada correctamente.");
                } catch (Exception e) {
                    lblEstado.setText("Error al cancelar la cita.");
                }
            }
        });
    }

    @FXML
    private void handleDetalle() {
        CitaDTO seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        Alert detalle = new Alert(Alert.AlertType.INFORMATION);
        detalle.setTitle("Detalle de cita");
        detalle.setHeaderText("Información de la cita");
        detalle.setContentText(
                "Profesional: " + seleccionada.getProfesionalNombre() + "\n" +
                        "Fecha:       " + seleccionada.getFechaHora().format(FECHA_FMT) + "\n" +
                        "Hora:        " + seleccionada.getFechaHora().format(HORA_FMT)  + "\n" +
                        "Estado:      " + seleccionada.getEstado().name()
        );
        detalle.showAndWait();
    }

    @FXML
    private void volver() {
        eventBus.unsubscribe(AppEvent.CITA_AGENDADA,  this);
        eventBus.unsubscribe(AppEvent.CITA_CANCELADA, this);
        stageInitializer.cambiarVistaConLoader(
                "/view/fxml/dashboard/dashboard-paciente.fxml",
                "Piedrazul - Mi Portal",
                900, 600
        ).getController();
    }

    private void configurarColumnas() {
        colFecha.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFechaHora().format(FECHA_FMT)));

        colHora.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFechaHora().format(HORA_FMT)));

        colProfesional.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getProfesionalNombre()));

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getEstado().name()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    setStyle(switch (EstadoCita.valueOf(estado)) {
                        case programada  -> "-fx-text-fill: #2196F3;";
                        case completada  -> "-fx-text-fill: #4CAF50;";
                        case cancelada   -> "-fx-text-fill: #F44336;";
                    });
                }
            }
        });
    }

    private void configurarSeleccion() {
        tablaCitas.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> {
                    boolean haySeleccion = actual != null;
                    btnDetalle.setDisable(!haySeleccion);

                    boolean cancelable = haySeleccion &&
                            actual.getEstado() == EstadoCita.programada;
                    btnCancelar.setDisable(!cancelable);
                }
        );
    }

    private void cargarCitas() {
        if (pacienteId == null) return;
        ObservableList<CitaDTO> citas = FXCollections.observableArrayList(
                citaService.listarPorPaciente(pacienteId));
        tablaCitas.setItems(citas);
        lblEstado.setText("Total: " + citas.size() + " citas");
    }
}