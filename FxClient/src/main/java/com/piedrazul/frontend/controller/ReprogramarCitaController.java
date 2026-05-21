package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.CitaClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.CitaDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * HU-6.3 – Reprogramación de una cita existente.
 *
 * Muestra la información actual de la cita (solo lectura) y permite
 * al agendador seleccionar una nueva fecha y horario disponible.
 *
 * Casos de aceptación:
 *   CA-1: Reprogramación exitosa → PUT /api/scheduling/citas/{id}.
 *   CA-2: Nuevo horario ocupado → 409 del backend, mensaje informativo.
 *   CA-3: Datos inválidos (fecha nula, horario no seleccionado) → validación local.
 *   CA-4: Notificación al completar (log + EventBus CITA_ACTUALIZADA).
 */
public class ReprogramarCitaController {

    // ── Información de la cita actual (solo lectura) ──────────────────────────
    @FXML private Label lblPaciente;
    @FXML private Label lblProfesional;
    @FXML private Label lblFechaActual;

    // ── Nueva fecha y horario ─────────────────────────────────────────────────
    @FXML private DatePicker              dpNuevaFecha;
    @FXML private ListView<ZonedDateTime> lvNuevosHorarios;

    // ── Acciones ─────────────────────────────────────────────────────────────
    @FXML private Button btnActualizar;
    @FXML private Label  lblMensaje;

    // ── Dependencias ─────────────────────────────────────────────────────────
    private final CitaClient       citaClient;
    private final StageInitializer stageInitializer;
    private final EventBus         eventBus;

    private CitaDTO    citaOriginal;
    private UsuarioDTO usuarioActual;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.forLanguageTag("es-CO"))
                    .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter HORA_FMT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("es-CO"))
                    .withZone(ZoneId.systemDefault());

    public ReprogramarCitaController(CitaClient citaClient,
                                     StageInitializer stageInitializer,
                                     EventBus eventBus) {
        this.citaClient       = citaClient;
        this.stageInitializer = stageInitializer;
        this.eventBus         = eventBus;
    }

    @FXML
    public void initialize() {
        btnActualizar.setDisable(true);
        lvNuevosHorarios.setDisable(true);

        // Bloquear fechas pasadas
        dpNuevaFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        dpNuevaFecha.valueProperty()
                .addListener((obs, ant, act) -> onFechaSeleccionada(act));
        lvNuevosHorarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, act) -> btnActualizar.setDisable(act == null));

        lvNuevosHorarios.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ZonedDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(HORA_FMT));
            }
        });
    }

    /**
     * Recibe la cita a reprogramar y el usuario activo del agendador.
     * Debe llamarse inmediatamente después de cargar el FXML.
     */
    public void setCitaACambiar(CitaDTO cita, UsuarioDTO usuarioActual) {
        this.citaOriginal  = cita;
        this.usuarioActual = usuarioActual;

        lblPaciente.setText(cita.getPacienteNombre());
        lblProfesional.setText(cita.getProfesionalNombre());
        lblFechaActual.setText(cita.getFechaHora().format(DT_FMT));
    }

    // ── CA-1 / CA-2 / CA-3 / CA-4: Actualizar ────────────────────────────────

    @FXML
    private void handleActualizar() {
        LocalDate     nuevaFecha   = dpNuevaFecha.getValue();
        ZonedDateTime nuevoHorario = lvNuevosHorarios.getSelectionModel().getSelectedItem();

        // CA-3: validar datos
        if (nuevaFecha == null || nuevoHorario == null) {
            mostrarMensaje("Debes seleccionar una nueva fecha y un horario disponible.", true);
            return;
        }

        btnActualizar.setDisable(true);
        lblMensaje.setText("Actualizando cita...");

        new Thread(() -> {
            try {
                CitaDTO dtoActualizado = CitaDTO.builder()
                        .id(citaOriginal.getId())
                        .pacienteId(citaOriginal.getPacienteId())
                        .profesionalId(citaOriginal.getProfesionalId())
                        .fechaHora(nuevoHorario)
                        .build();

                CitaDTO resultado = citaClient.actualizarCita(citaOriginal.getId(), dtoActualizado);

                // CA-4: notificación de cambio exitoso
                eventBus.publish(AppEvent.CITA_ACTUALIZADA, resultado);

                Platform.runLater(() -> {
                    // CA-1: éxito
                    mostrarMensaje("Cita reprogramada correctamente para el " +
                            nuevoHorario.format(DT_FMT) + ".", false);
                    // Regresar a gestión tras 1.5 s
                    new Thread(() -> {
                        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::volverAGestion);
                    }).start();
                });

            } catch (HttpException ex) {
                Platform.runLater(() -> {
                    if (ex.getStatusCode() == 409) {
                        // CA-2: horario ocupado
                        mostrarMensaje("El horario seleccionado ya está ocupado. " +
                                "Por favor elige otro.", true);
                        onFechaSeleccionada(nuevaFecha); // refrescar slots
                    } else if (ex.getStatusCode() == 422) {
                        mostrarMensaje("La cita no puede ser reprogramada porque ya fue " +
                                "atendida o cancelada.", true);
                    } else {
                        mostrarMensaje("Error al reprogramar la cita (" + ex.getStatusCode() + ").", true);
                    }
                    btnActualizar.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarMensaje("Error inesperado. Intenta de nuevo.", true);
                    btnActualizar.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancelar() {
        volverAGestion();
    }

    // ── Carga de horarios disponibles ─────────────────────────────────────────

    private void onFechaSeleccionada(LocalDate fecha) {
        lvNuevosHorarios.setItems(FXCollections.observableArrayList());
        lvNuevosHorarios.setDisable(true);
        btnActualizar.setDisable(true);

        if (fecha == null || citaOriginal == null) return;

        lblMensaje.setText("Cargando horarios disponibles...");
        new Thread(() -> {
            try {
                List<String> isoHorarios =
                        citaClient.obtenerHorariosDisponibles(citaOriginal.getProfesionalId(), fecha);
                List<ZonedDateTime> horarios = isoHorarios.stream()
                        .map(ZonedDateTime::parse)
                        .toList();
                Platform.runLater(() -> {
                    lvNuevosHorarios.setItems(FXCollections.observableArrayList(horarios));
                    lvNuevosHorarios.setDisable(false);
                    lblMensaje.setText(horarios.isEmpty()
                            ? "No hay horarios disponibles para esta fecha."
                            : "Selecciona el nuevo horario.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> mostrarMensaje("Error al cargar horarios.", true));
            }
        }).start();
    }

    private void volverAGestion() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/citas/gestion-citas-agendador.fxml",
                "Piedrazul - Gestión de Citas", 1050, 680);
        GestionCitasAgendadorController ctrl = loader.getController();
        if (usuarioActual != null) ctrl.setUsuarioActual(usuarioActual);
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #1B5E20;");
    }
}
