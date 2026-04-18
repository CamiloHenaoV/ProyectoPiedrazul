package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.PacienteDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Formulario de datos adicionales del paciente.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, IRegistroService (local)
 * - Añadido:   UsuarioClient.registrarPaciente() → POST /usuarios/pacientes
 * - La operación es síncrona aquí porque ocurre en un modal bloqueante;
 *   si el backend tarda considera hacerla async con Task.
 */
public class FormPacienteController {

    @FXML private TextField  txtNombre;
    @FXML private TextField  txtCedula;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField  txtTelefono;
    @FXML private TextField  txtEmail;
    @FXML private TextField  txtDireccion;
    @FXML private Label      lblError;

    private final UsuarioClient usuarioClient;
    private UsuarioDTO usuarioNuevo;

    public FormPacienteController(UsuarioClient usuarioClient) {
        this.usuarioClient = usuarioClient;
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

            // CAMBIO: antes registroService.registrarPaciente(usuarioNuevo, pacienteDTO)
            // Ahora: POST /usuarios/pacientes al API Gateway
            usuarioClient.registrarPaciente(usuarioNuevo, pacienteDTO);
            cerrarModal();

        } catch (HttpException ex) {
            if (ex.isConflict())
                mostrarError("El login ya está en uso.");
            else
                mostrarError("Error al guardar: " + ex.getMessage());
        } catch (Exception e) {
            mostrarError("Error al guardar los datos del paciente.");
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarModal();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank()) {
            mostrarError("El nombre es obligatorio."); return false;
        }
        if (txtCedula.getText().isBlank()) {
            mostrarError("La cédula es obligatoria."); return false;
        }
        if (txtTelefono.getText().isBlank()) {
            mostrarError("El teléfono es obligatorio."); return false;
        }
        return true;
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void cerrarModal() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}
