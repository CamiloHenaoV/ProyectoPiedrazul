package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.CitaClient;
import com.piedrazul.frontend.client.EspecialidadClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.CitaDTO;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.enums.RolUsuario;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
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
 * HU-6.2 – Registro manual de cita por parte del agendador.
 *
 * Flujo:
 *   1. Seleccionar paciente de la lista de usuarios con rol PACIENTE.
 *   2. Seleccionar especialidad → profesional (misma cascada que AgendarCita).
 *   3. Seleccionar fecha → horarios disponibles.
 *   4. Clic GUARDAR → POST /api/scheduling/citas.
 *
 * Casos de aceptación implementados:
 *   CA-1: Registro exitoso con confirmación.
 *   CA-2: Validación de campos obligatorios (paciente, profesional, fecha, horario).
 *   CA-3: Rechazo si el horario ya está ocupado (409).
 *   CA-4: Paciente sin fecha de nacimiento / correo → campos opcionales, no bloquea.
 *   CA-5: Validación del intervalo del profesional (el backend la realiza; 409 si no encaja).
 */
public class RegistroCitaManualController {

    // ── Selección de paciente ─────────────────────────────────────────────────
    @FXML private ComboBox<UsuarioDTO>     cbPaciente;

    // ── Selección de profesional ──────────────────────────────────────────────
    @FXML private ComboBox<String>         cbEspecialidad;
    @FXML private ComboBox<ProfesionalDTO> cbProfesional;

    // ── Selección de fecha y horario ──────────────────────────────────────────
    @FXML private DatePicker               dpFecha;
    @FXML private ListView<ZonedDateTime>  lvHorarios;

    // ── Acciones ─────────────────────────────────────────────────────────────
    @FXML private Button btnGuardar;
    @FXML private Label  lblMensaje;

    // ── Dependencias ─────────────────────────────────────────────────────────
    private final CitaClient         citaClient;
    private final EspecialidadClient especialidadClient;
    private final UsuarioClient      usuarioClient;
    private final StageInitializer   stageInitializer;
    private final EventBus           eventBus;

    private UsuarioDTO usuarioActual; // el agendador que está logueado

    private static final DateTimeFormatter HORA_FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-CO"))
                    .withZone(ZoneId.systemDefault());

    public RegistroCitaManualController(CitaClient citaClient,
                                        EspecialidadClient especialidadClient,
                                        UsuarioClient usuarioClient,
                                        StageInitializer stageInitializer,
                                        EventBus eventBus) {
        this.citaClient         = citaClient;
        this.especialidadClient = especialidadClient;
        this.usuarioClient      = usuarioClient;
        this.stageInitializer   = stageInitializer;
        this.eventBus           = eventBus;
    }

    @FXML
    public void initialize() {
        btnGuardar.setDisable(true);
        dpFecha.setDisable(true);
        lvHorarios.setDisable(true);

        configurarComboPaciente();
        configurarComboProfesional();
        configurarListaHorarios();

        // Bloquear fechas pasadas
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Cascadas de selección
        cbEspecialidad.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> onEspecialidadSeleccionada(act));
        cbProfesional.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> {
                    dpFecha.setValue(null);
                    lvHorarios.setItems(FXCollections.observableArrayList());
                    dpFecha.setDisable(act == null);
                });
        dpFecha.valueProperty()
                .addListener((obs, ant, act) -> onFechaSeleccionada(act));
        lvHorarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> actualizarBotonGuardar());

        cargarPacientes();
        cargarEspecialidades();
    }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
    }

    // ── CA-1 / CA-2 / CA-3 / CA-5: Guardar ──────────────────────────────────

    @FXML
    private void handleGuardar() {
        UsuarioDTO     paciente    = cbPaciente.getSelectionModel().getSelectedItem();
        ProfesionalDTO profesional = cbProfesional.getSelectionModel().getSelectedItem();
        ZonedDateTime  horario     = lvHorarios.getSelectionModel().getSelectedItem();

        // CA-2: validar campos obligatorios
        if (paciente == null || profesional == null || dpFecha.getValue() == null || horario == null) {
            mostrarMensaje("Todos los campos son obligatorios: paciente, profesional, fecha y horario.", true);
            return;
        }

        btnGuardar.setDisable(true);
        lblMensaje.setText("Registrando cita...");

        new Thread(() -> {
            try {
                CitaDTO dto = CitaDTO.builder()
                        .pacienteId(paciente.getId())
                        .profesionalId(profesional.getId())
                        .fechaHora(horario)
                        .build();

                CitaDTO registrada = citaClient.agendarCita(dto);
                eventBus.publish(AppEvent.CITA_AGENDADA, registrada);

                Platform.runLater(() -> {
                    // CA-1: confirmación de registro exitoso
                    mostrarMensaje("Cita registrada correctamente para " +
                            paciente.getNombreCompleto() + ".", false);
                    limpiarFormulario();
                });

            } catch (HttpException ex) {
                Platform.runLater(() -> {
                    if (ex.getStatusCode() == 409) {
                        // CA-3 / CA-5: horario ocupado o fuera del intervalo
                        mostrarMensaje("El horario seleccionado ya no está disponible. " +
                                "Por favor selecciona otro.", true);
                        onFechaSeleccionada(dpFecha.getValue()); // refrescar slots
                    } else if (ex.getStatusCode() == 422) {
                        mostrarMensaje("El paciente o profesional no está registrado " +
                                "en el sistema de agendamiento. Intenta de nuevo en unos segundos.", true);
                    } else {
                        mostrarMensaje("Error al registrar la cita (" + ex.getStatusCode() + ").", true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        mostrarMensaje("Error inesperado. Intenta de nuevo.", true));
            } finally {
                Platform.runLater(() -> btnGuardar.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void handleCancelar() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/gestion-citas-agendador.fxml",
                "Piedrazul - Gestión de Citas", 1050, 680);
        GestionCitasAgendadorController ctrl = loader.getController();
        if (usuarioActual != null) ctrl.setUsuarioActual(usuarioActual);
    }

    // ── Carga de datos ────────────────────────────────────────────────────────

    private void cargarPacientes() {
        new Thread(() -> {
            try {
                List<UsuarioDTO> pacientes = usuarioClient.listarTodos()
                        .stream()
                        .filter(u -> u.getRol() == RolUsuario.paciente && Boolean.TRUE.equals(u.getActivo()))
                        .toList();
                Platform.runLater(() ->
                        cbPaciente.setItems(FXCollections.observableArrayList(pacientes)));
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar lista de pacientes.", true));
            }
        }).start();
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
        dpFecha.setDisable(true);
        lvHorarios.setItems(FXCollections.observableArrayList());
        btnGuardar.setDisable(true);
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

    private void onFechaSeleccionada(LocalDate fecha) {
        lvHorarios.setItems(FXCollections.observableArrayList());
        lvHorarios.setDisable(true);
        btnGuardar.setDisable(true);

        ProfesionalDTO profesional = cbProfesional.getSelectionModel().getSelectedItem();
        if (fecha == null || profesional == null) return;

        lblMensaje.setText("Cargando horarios disponibles...");
        new Thread(() -> {
            try {
                List<String> isoHorarios =
                        citaClient.obtenerHorariosDisponibles(profesional.getId(), fecha);
                List<ZonedDateTime> horarios = isoHorarios.stream()
                        .map(ZonedDateTime::parse)
                        .toList();
                Platform.runLater(() -> {
                    lvHorarios.setItems(FXCollections.observableArrayList(horarios));
                    lvHorarios.setDisable(false);
                    lblMensaje.setText(horarios.isEmpty()
                            ? "No hay horarios disponibles para esta fecha."
                            : "Selecciona un horario.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar horarios.", true));
            }
        }).start();
    }

    // ── Configuración de componentes ──────────────────────────────────────────

    private void configurarComboPaciente() {
        cbPaciente.setConverter(new StringConverter<>() {
            @Override public String toString(UsuarioDTO u) {
                return u == null ? "" : u.getNombreCompleto() + " (" + u.getLogin() + ")";
            }
            @Override public UsuarioDTO fromString(String s) { return null; }
        });
    }

    private void configurarComboProfesional() {
        cbProfesional.setConverter(new StringConverter<>() {
            @Override public String toString(ProfesionalDTO p) {
                return p == null ? "" : p.getNombreCompleto() + " — " + p.getEspecialidadNombre();
            }
            @Override public ProfesionalDTO fromString(String s) { return null; }
        });
    }

    private void configurarListaHorarios() {
        lvHorarios.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ZonedDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(HORA_FMT));
            }
        });
    }

    private void actualizarBotonGuardar() {
        boolean listo = cbPaciente.getSelectionModel().getSelectedItem() != null
                && cbProfesional.getSelectionModel().getSelectedItem() != null
                && dpFecha.getValue() != null
                && lvHorarios.getSelectionModel().getSelectedItem() != null;
        btnGuardar.setDisable(!listo);
    }

    private void limpiarFormulario() {
        cbPaciente.getSelectionModel().clearSelection();
        cbEspecialidad.getSelectionModel().clearSelection();
        cbProfesional.setItems(FXCollections.observableArrayList());
        dpFecha.setValue(null);
        lvHorarios.setItems(FXCollections.observableArrayList());
        dpFecha.setDisable(true);
        lvHorarios.setDisable(true);
        btnGuardar.setDisable(true);
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #1B5E20;");
    }
}
