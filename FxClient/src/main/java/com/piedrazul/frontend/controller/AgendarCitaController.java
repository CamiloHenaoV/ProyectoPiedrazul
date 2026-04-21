package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.CitaClient;
import com.piedrazul.frontend.client.EspecialidadClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.http.SessionManager;
import com.piedrazul.frontend.model.dto.CitaDTO;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controlador de agendamiento de citas.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, IEspecialidadService, IProfesionalService,
 *              ICitaService, IUsuarioService (todos locales)
 * - Añadido:   EspecialidadClient, CitaClient, UsuarioClient (HTTP)
 * - Los horarios disponibles llegan como List<String> ISO-8601 desde el gateway
 *   y se parsean a ZonedDateTime localmente
 * - HorarioOcupadoException → HttpException con statusCode 409
 * - Toda la lógica async ya existía; solo cambia el origen de los datos
 */
public class AgendarCitaController {

    @FXML private ComboBox<String>         cbEspecialidad;
    @FXML private ComboBox<ProfesionalDTO> cbProfesional;
    @FXML private DatePicker               dpFecha;
    @FXML private ListView<ZonedDateTime>  lvHorarios;
    @FXML private Button                   btnConfirmar;
    @FXML private Label                    lblEstado;

    private final EspecialidadClient especialidadClient;
    private final CitaClient         citaClient;
    private final UsuarioClient      usuarioClient;
    private final StageInitializer   stageInitializer;
    private final SessionManager     session;

    private Long pacienteId;
    private UsuarioDTO usuarioActual;

    private static final DateTimeFormatter HORA_FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-CO"))
                    .withZone(ZoneId.systemDefault());

    public AgendarCitaController(EspecialidadClient especialidadClient,
                                 CitaClient citaClient,
                                 UsuarioClient usuarioClient,
                                 StageInitializer stageInitializer,
                                 SessionManager session) {
        this.especialidadClient = especialidadClient;
        this.citaClient         = citaClient;
        this.usuarioClient      = usuarioClient;
        this.stageInitializer   = stageInitializer;
        this.session            = session;
    }

    @FXML
    public void initialize() {
        btnConfirmar.setDisable(true);
        dpFecha.setDisable(true);
        lvHorarios.setDisable(true);

        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        cbProfesional.setConverter(new StringConverter<>() {
            @Override public String toString(ProfesionalDTO p) {
                return p == null ? "" : p.getNombreCompleto() + " — " + p.getEspecialidadNombre();
            }
            @Override public ProfesionalDTO fromString(String s) { return null; }
        });

        lvHorarios.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ZonedDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(HORA_FMT));
            }
        });

        cbEspecialidad.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> onEspecialidadSeleccionada(act));
        cbProfesional.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> onProfesionalSeleccionado(act));
        dpFecha.valueProperty()
                .addListener((obs, ant, act) -> onFechaSeleccionada(act));
        lvHorarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> btnConfirmar.setDisable(act == null));

        cargarEspecialidades();
    }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        this.pacienteId = usuario.getId();
    }

    @FXML
    private void handleConfirmar() {
        ZonedDateTime  horario    = lvHorarios.getSelectionModel().getSelectedItem();
        ProfesionalDTO profesional = cbProfesional.getSelectionModel().getSelectedItem();
        if (horario == null || profesional == null || pacienteId == null) return;

        btnConfirmar.setDisable(true);
        lblEstado.setText("Agendando cita...");

        new Thread(() -> {
            try {
                CitaDTO dto = CitaDTO.builder()
                        .pacienteId(pacienteId)
                        .profesionalId(profesional.getId())
                        .fechaHora(horario)
                        .build();

                // POST /citas al API Gateway
                citaClient.agendarCita(dto);
                Platform.runLater(() -> {
                    lblEstado.setText("Cita agendada correctamente.");
                    limpiarFormulario();
                });

            } catch (HttpException ex) {
                Platform.runLater(() -> {
                    if (ex.isConflict()) {
                        // 409 = HorarioOcupadoException
                        lblEstado.setText("El horario ya no está disponible. Selecciona otro.");
                        onFechaSeleccionada(dpFecha.getValue());
                    } else {
                        lblEstado.setText("Error al agendar (" + ex.getStatusCode() + ").");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEstado.setText("Error inesperado. Intenta de nuevo."));
            } finally {
                Platform.runLater(() -> btnConfirmar.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void volver() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/dashboard/dashboard-paciente.fxml",
                "Piedrazul - Mi Portal", 900, 600);
        DashboardPacienteController ctrl = loader.getController();
        if (usuarioActual != null) ctrl.setUsuarioActual(usuarioActual);
    }

    private void cargarEspecialidades() {
        new Thread(() -> {
            try {
                // CAMBIO: antes especialidadService.listarNombres() (local)
                // Ahora: GET /especialidades
                List<String> especialidades = especialidadClient.listarNombres();
                Platform.runLater(() ->
                        cbEspecialidad.setItems(FXCollections.observableArrayList(especialidades)));
            } catch (Exception e) {
                Platform.runLater(() -> lblEstado.setText("Error al cargar especialidades."));
            }
        }).start();
    }

    private void onEspecialidadSeleccionada(String especialidad) {
        cbProfesional.setItems(FXCollections.observableArrayList());
        dpFecha.setDisable(true);
        lvHorarios.setItems(FXCollections.observableArrayList());
        lvHorarios.setDisable(true);
        btnConfirmar.setDisable(true);
        if (especialidad == null) return;

        lblEstado.setText("Cargando profesionales...");
        new Thread(() -> {
            try {
                // CAMBIO: antes profesionalService.listarActivosPorEspecialidad()
                // Ahora: GET /profesionales/activos?especialidad=...
                List<ProfesionalDTO> profesionales =
                        especialidadClient.listarActivosPorEspecialidad(especialidad);
                Platform.runLater(() -> {
                    cbProfesional.setItems(FXCollections.observableArrayList(profesionales));
                    lblEstado.setText(profesionales.isEmpty()
                            ? "No hay profesionales disponibles." : "");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEstado.setText("Error al cargar profesionales."));
            }
        }).start();
    }

    private void onProfesionalSeleccionado(ProfesionalDTO profesional) {
        dpFecha.setValue(null);
        lvHorarios.setItems(FXCollections.observableArrayList());
        lvHorarios.setDisable(true);
        btnConfirmar.setDisable(true);
        dpFecha.setDisable(profesional == null);
    }

    private void onFechaSeleccionada(LocalDate fecha) {
        lvHorarios.setItems(FXCollections.observableArrayList());
        lvHorarios.setDisable(true);
        btnConfirmar.setDisable(true);

        ProfesionalDTO profesional = cbProfesional.getSelectionModel().getSelectedItem();
        if (fecha == null || profesional == null) return;

        lblEstado.setText("Cargando horarios disponibles...");
        new Thread(() -> {
            try {
                // CAMBIO: antes citaService.obtenerHorariosDisponibles() → List<ZonedDateTime> local
                // Ahora: GET /citas/horarios?profesionalId=&fecha= → List<String> ISO-8601
                List<String> isoHorarios =
                        citaClient.obtenerHorariosDisponibles(profesional.getId(), fecha);
                List<ZonedDateTime> horarios = isoHorarios.stream()
                        .map(ZonedDateTime::parse)
                        .toList();
                Platform.runLater(() -> {
                    lvHorarios.setItems(FXCollections.observableArrayList(horarios));
                    lvHorarios.setDisable(false);
                    lblEstado.setText(horarios.isEmpty()
                            ? "No hay horarios disponibles." : "Selecciona un horario.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEstado.setText("Error al cargar horarios."));
            }
        }).start();
    }

    private void limpiarFormulario() {
        cbEspecialidad.getSelectionModel().clearSelection();
        cbProfesional.setItems(FXCollections.observableArrayList());
        dpFecha.setValue(null);
        lvHorarios.setItems(FXCollections.observableArrayList());
        dpFecha.setDisable(true);
        lvHorarios.setDisable(true);
        btnConfirmar.setDisable(true);
    }
}