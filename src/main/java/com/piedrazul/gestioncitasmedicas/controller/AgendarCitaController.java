package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.CitaDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.exceptions.HorarioOcupadoException;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.ICitaService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IEspecialidadService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IProfesionalService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public class AgendarCitaController {

    @FXML private ComboBox<String>          cbEspecialidad;
    @FXML private ComboBox<ProfesionalDTO>  cbProfesional;
    @FXML private DatePicker                dpFecha;
    @FXML private ListView<ZonedDateTime>   lvHorarios;
    @FXML private Button                    btnConfirmar;
    @FXML private Label                     lblEstado;

    private final IEspecialidadService especialidadService;
    private final IProfesionalService  profesionalService;
    private final ICitaService         citaService;
    private final IUsuarioService      usuarioService;
    private final StageInitializer     stageInitializer;

    private UsuarioDTO usuarioActual;
    private UUID       pacienteId;

    private static final DateTimeFormatter HORA_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public AgendarCitaController(IEspecialidadService especialidadService,
                                 IProfesionalService  profesionalService,
                                 ICitaService         citaService,
                                 IUsuarioService      usuarioService,
                                 StageInitializer     stageInitializer) {
        this.especialidadService = especialidadService;
        this.profesionalService  = profesionalService;
        this.citaService         = citaService;
        this.usuarioService      = usuarioService;
        this.stageInitializer    = stageInitializer;
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

        cbEspecialidad.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> onEspecialidadSeleccionada(actual));

        cbProfesional.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> onProfesionalSeleccionado(actual));

        dpFecha.valueProperty().addListener(
                (obs, anterior, actual) -> onFechaSeleccionada(actual));

        lvHorarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> btnConfirmar.setDisable(actual == null));

        cargarEspecialidades();
    }

    public void setUsuarioActual(UsuarioDTO usuario) {
        this.usuarioActual = usuario;
        this.pacienteId    = usuarioService.buscarPacienteIdPorUsuarioId(usuario.getId());
    }

    @FXML
    private void handleConfirmar() {
        ZonedDateTime horario     = lvHorarios.getSelectionModel().getSelectedItem();
        ProfesionalDTO profesional = cbProfesional.getSelectionModel().getSelectedItem();

        if (horario == null || profesional == null) return;

        btnConfirmar.setDisable(true);
        lblEstado.setText("Agendando cita...");

        new Thread(() -> {
            try {
                CitaDTO dto = CitaDTO.builder()
                        .pacienteId(pacienteId)
                        .profesionalId(profesional.getId())
                        .fechaHora(horario)
                        .build();

                citaService.agendarCita(dto);
                Platform.runLater(() -> {
                    lblEstado.setText("Cita agendada correctamente.");
                    limpiarFormulario();
                });

            } catch (HorarioOcupadoException e) {
                Platform.runLater(() -> {
                    lblEstado.setText("El horario ya no está disponible. Seleccioná otro.");
                    onFechaSeleccionada(dpFecha.getValue());
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        lblEstado.setText("Error inesperado. Intentá de nuevo."));
            } finally {
                Platform.runLater(() -> btnConfirmar.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void volver() {
        stageInitializer.cambiarVistaConLoader(
                "/view/fxml/dashboard/dashboard-paciente.fxml",
                "Piedrazul - Mi Portal",
                900, 600
        ).getController();
    }

    private void cargarEspecialidades() {
        new Thread(() -> {
            List<String> especialidades = especialidadService.listarNombres();
            Platform.runLater(() ->
                    cbEspecialidad.setItems(FXCollections.observableArrayList(especialidades)));
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
            List<ProfesionalDTO> profesionales =
                    profesionalService.listarActivosPorEspecialidad(especialidad);
            Platform.runLater(() -> {
                cbProfesional.setItems(FXCollections.observableArrayList(profesionales));
                lblEstado.setText(profesionales.isEmpty()
                        ? "No hay profesionales disponibles para esta especialidad."
                        : "");
            });
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
            List<ZonedDateTime> horarios =
                    citaService.obtenerHorariosDisponibles(profesional.getId(), fecha);
            Platform.runLater(() -> {
                lvHorarios.setItems(FXCollections.observableArrayList(horarios));
                lvHorarios.setDisable(false);
                lblEstado.setText(horarios.isEmpty()
                        ? "No hay horarios disponibles para esta fecha."
                        : "Seleccioná un horario.");
            });
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