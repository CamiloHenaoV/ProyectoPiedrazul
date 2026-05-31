package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.client.AuthClient;
import com.piedrazul.frontend.client.EspecialidadClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.dto.UsuarioRegistroDTO;
import com.piedrazul.frontend.model.enums.TipoProfesional;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Formulario de datos adicionales del profesional.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, IRegistroService, IEspecialidadService (locales)
 * - Añadido:   UsuarioClient + EspecialidadClient (HTTP)
 * - initialize() carga especialidades desde GET /especialidades
 * - handleGuardar() → POST /usuarios/profesionales
 */
public class FormProfesionalController {

    @FXML private ComboBox<TipoProfesional> cbTipo;
    @FXML private ComboBox<String>          cbEspecialidad;
    @FXML private TextField                 txtLicencia;
    @FXML private Label                     lblError;
    @FXML private TextField                 txtIntervalo;

    private final UsuarioClient      usuarioClient;
    private final AuthClient authClient;
    private final EspecialidadClient especialidadClient;
    private final EventBus eventBus;

    private UsuarioRegistroDTO usuarioNuevo;

    public FormProfesionalController(UsuarioClient usuarioClient,
                                     AuthClient authClient,
                                     EspecialidadClient especialidadClient,
                                     EventBus eventBus) {
        this.usuarioClient      = usuarioClient;
        this.authClient      = authClient;
        this.especialidadClient = especialidadClient;
        this.eventBus         = eventBus;
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        cbTipo.setItems(FXCollections.observableArrayList(TipoProfesional.values()));

        // CAMBIO: antes especialidadService.listarNombres() (local)
        // Ahora: GET /especialidades al API Gateway
        new Thread(() -> {
            try {
                var especialidades = especialidadClient.listarNombres();
                javafx.application.Platform.runLater(() ->
                        cbEspecialidad.setItems(
                                FXCollections.observableArrayList(especialidades)));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        mostrarError("No se pudieron cargar las especialidades."));
            }
        }).start();
    }

    public void setUsuarioNuevo(UsuarioRegistroDTO usuario) {
        this.usuarioNuevo = usuario;
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) return;

        // ── Paso 1: crear perfil en MSUserManagement ──────────────────────
        UsuarioDTO creado = null;
        try {
            ProfesionalDTO profesionalDTO = ProfesionalDTO.builder()
                    .licenciaProfesional(txtLicencia.getText().trim())
                    .especialidadNombre(cbEspecialidad.getValue())
                    .tipo(cbTipo.getValue())
                    .duracionCitaMinutos(Integer.parseInt(txtIntervalo.getText().trim()))
                    .build();
            UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                    .nombreCompleto(usuarioNuevo.getNombreCompleto())
                    .login(usuarioNuevo.getLogin())
                    .rol(usuarioNuevo.getRol())
                    .activo(true)
                    .build();

            creado = usuarioClient.registrarProfesional(usuarioDTO, profesionalDTO);

        } catch (HttpException ex) {
            if (ex.isConflict())
                mostrarError("El login ya está en uso.");
            else
                mostrarError("Error al crear el perfil del profesional (paso 1): " + ex.getMessage());
            return;
        } catch (Exception e) {
            mostrarError("Error inesperado al crear el perfil del profesional.");
            return;
        }

        // ── Paso 2: registrar credenciales en MSAuth ───────────────────────
        // FIX ALTO: si este paso falla, el usuario queda en BD sin credenciales.
        // Mitigación: se intenta desactivar el usuario creado (transacción
        // compensatoria) para que no quede en un estado silencioso.
        // La solución definitiva es un endpoint atómico en MSUserManagement que
        // ejecute ambos pasos dentro de una única transacción coordinada.
        try {
            authClient.registrarCredencial(creado.getId(), usuarioNuevo.getLogin(), usuarioNuevo.getPassword());
        } catch (Exception credEx) {
            try {
                usuarioClient.desactivarUsuario(creado.getId());
            } catch (Exception rollbackEx) {
                mostrarError("Error crítico: perfil creado (id=" + creado.getId()
                        + ") pero sin credenciales y sin poder desactivarlo. "
                        + "Contacte al administrador.");
                return;
            }
            mostrarError("No se pudieron registrar las credenciales (MSAuth no disponible). "
                    + "El perfil fue revertido. Intente nuevamente.");
            return;
        }

        eventBus.publish(AppEvent.USUARIO_CREADO, creado);
        cerrarModal();
    }

    @FXML
    private void handleCancelar() {
        cerrarModal();
    }

    private boolean validarCampos() {
        if (txtLicencia.getText().isBlank()) {
            mostrarError("La licencia profesional es obligatoria."); return false;
        }
        if (cbEspecialidad.getValue() == null) {
            mostrarError("Selecciona una especialidad."); return false;
        }
        if (cbTipo.getValue() == null) {
            mostrarError("Selecciona el tipo de profesional."); return false;
        }
        String intervaloTxt = txtIntervalo.getText().trim();
        if (intervaloTxt.isBlank()) {
            mostrarError("El intervalo de cita es obligatorio."); return false;
        }
        try {
            int minutos = Integer.parseInt(intervaloTxt);
            if (minutos <= 0) {
                mostrarError("El intervalo debe ser mayor a 0."); return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("El intervalo debe ser un número entero (ej: 30)."); return false;
        }
        return true;
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void cerrarModal() {
        ((Stage) txtLicencia.getScene().getWindow()).close();
    }
}
