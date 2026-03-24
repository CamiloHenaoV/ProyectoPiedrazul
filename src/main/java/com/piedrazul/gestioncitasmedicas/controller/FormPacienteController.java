package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.model.dto.PacienteDTO;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IPacienteService;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class FormPacienteController {

    @FXML private TextField  txtNombre;
    @FXML private TextField  txtCedula;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField  txtTelefono;
    @FXML private TextField  txtEmail;
    @FXML private TextField  txtDireccion;
    @FXML private Label      lblError;

    private final IUsuarioService usuarioService;

    private UsuarioDTO usuarioNuevo;

    public FormPacienteController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
    }

    public void setUsuarioNuevo(UsuarioDTO usuario) {
        this.usuarioNuevo = usuario;
        txtNombre.setText(usuario.getNombreCompleto());
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) return;

        try {
            PacienteDTO pacienteDTO = PacienteDTO.builder()
                    .nombreCompleto(txtNombre.getText().trim())
                    .cedulaIdentidad(txtCedula.getText().trim())
                    .fechaNacimiento(dpFechaNacimiento.getValue())
                    .telefono(txtTelefono.getText().trim())
                    .email(txtEmail.getText().trim())
                    .direccion(txtDireccion.getText().trim())
                    .build();

            usuarioService.crearUsuarioConPaciente(usuarioNuevo, pacienteDTO);
            cerrarModal();

        } catch (Exception e) {
            mostrarError("Error al guardar los datos del paciente.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarModal();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank()) {
            mostrarError("El nombre es obligatorio.");
            return false;
        }
        if (txtCedula.getText().isBlank()) {
            mostrarError("La cédula es obligatoria.");
            return false;
        }
        if (txtTelefono.getText().isBlank()) {
            mostrarError("El teléfono es obligatorio.");
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void cerrarModal() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}