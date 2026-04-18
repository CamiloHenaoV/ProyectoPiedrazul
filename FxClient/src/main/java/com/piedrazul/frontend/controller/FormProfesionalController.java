package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.client.EspecialidadClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.ProfesionalDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.enums.TipoProfesional;
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

    private final UsuarioClient      usuarioClient;
    private final EspecialidadClient especialidadClient;

    private UsuarioDTO usuarioNuevo;

    public FormProfesionalController(UsuarioClient usuarioClient,
                                     EspecialidadClient especialidadClient) {
        this.usuarioClient      = usuarioClient;
        this.especialidadClient = especialidadClient;
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

    public void setUsuarioNuevo(UsuarioDTO usuario) {
        this.usuarioNuevo = usuario;
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) return;

        try {
            ProfesionalDTO profesionalDTO = ProfesionalDTO.builder()
                    .licenciaProfesional(txtLicencia.getText().trim())
                    .especialidadNombre(cbEspecialidad.getValue())
                    .tipo(cbTipo.getValue())
                    .build();

            // CAMBIO: antes registroService.registrarProfesional(...)
            // Ahora: POST /usuarios/profesionales
            usuarioClient.registrarProfesional(usuarioNuevo, profesionalDTO);
            cerrarModal();

        } catch (HttpException ex) {
            if (ex.isConflict())
                mostrarError("El login ya está en uso.");
            else
                mostrarError("Error al guardar: " + ex.getMessage());
        } catch (Exception e) {
            mostrarError("Error al guardar los datos del profesional.");
        }
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
