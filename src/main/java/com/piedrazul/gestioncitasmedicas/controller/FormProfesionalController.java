package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.model.dto.ProfesionalDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.TipoProfesional;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IEspecialidadService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IProfesionalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class FormProfesionalController {

    @FXML private TextField         txtLicencia;
    @FXML private ComboBox<String>  cbEspecialidad;
    @FXML private ComboBox<TipoProfesional> cbTipo;
    @FXML private Label             lblError;

    private final IProfesionalService  profesionalService;
    private final IEspecialidadService especialidadService;

    private UsuarioDTO usuarioCreado;

    public FormProfesionalController(IProfesionalService  profesionalService,
                                     IEspecialidadService especialidadService) {
        this.profesionalService  = profesionalService;
        this.especialidadService = especialidadService;
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        cbTipo.setItems(FXCollections.observableArrayList(TipoProfesional.values()));
        cbEspecialidad.setItems(
                FXCollections.observableArrayList(especialidadService.listarNombres()));
    }

    public void setUsuarioCreado(UsuarioDTO usuario) {
        this.usuarioCreado = usuario;
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) return;

        try {
            ProfesionalDTO dto = ProfesionalDTO.builder()
                    .licenciaProfesional(txtLicencia.getText().trim())
                    .especialidadNombre(cbEspecialidad.getValue())
                    .tipo(cbTipo.getValue())
                    .build();

            profesionalService.crearProfesional(usuarioCreado.getId(), dto);
            cerrarModal();

        } catch (Exception e) {
            mostrarError("Error al guardar los datos del profesional.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarModal();
    }

    private boolean validarCampos() {
        if (txtLicencia.getText().isBlank()) {
            mostrarError("La licencia profesional es obligatoria.");
            return false;
        }
        if (cbEspecialidad.getValue() == null) {
            mostrarError("Selecciona una especialidad.");
            return false;
        }
        if (cbTipo.getValue() == null) {
            mostrarError("Selecciona el tipo de profesional.");
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void cerrarModal() {
        ((Stage) txtLicencia.getScene().getWindow()).close();
    }
}