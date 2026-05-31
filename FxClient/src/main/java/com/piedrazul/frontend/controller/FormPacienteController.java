package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.client.AuthClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.PacienteDTO;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.dto.UsuarioRegistroDTO;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
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
    private final AuthClient authClient;
    private UsuarioRegistroDTO usuarioNuevo;
    private final EventBus eventBus;

    public FormPacienteController(UsuarioClient usuarioClient,
                                  AuthClient authClient,
                                  EventBus eventBus) {
        this.usuarioClient = usuarioClient;
        this.authClient      = authClient;
        this.eventBus = eventBus;
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
    }

    public void setUsuarioNuevo(UsuarioRegistroDTO usuario) {
        this.usuarioNuevo = usuario;
        txtNombre.setText(usuario.getNombreCompleto());
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) return;

        // ── Paso 1: crear perfil en MSUserManagement ──────────────────────
        UsuarioDTO creado = null;
        try {
            PacienteDTO pacienteDTO = PacienteDTO.builder()
                    .nombreCompleto(txtNombre.getText().trim())
                    .cedulaIdentidad(txtCedula.getText().trim())
                    .fechaNacimiento(dpFechaNacimiento.getValue())
                    .telefono(txtTelefono.getText().trim())
                    .email(txtEmail.getText().trim())
                    .direccion(txtDireccion.getText().trim())
                    .build();
            UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                    .nombreCompleto(usuarioNuevo.getNombreCompleto())
                    .login(usuarioNuevo.getLogin())
                    .rol(usuarioNuevo.getRol())
                    .activo(true)
                    .build();

            creado = usuarioClient.registrarPaciente(usuarioDTO, pacienteDTO);

        } catch (HttpException ex) {
            if (ex.isConflict())
                mostrarError("El login ya está en uso.");
            else
                mostrarError("Error al crear el perfil del paciente (paso 1): " + ex.getMessage());
            return;
        } catch (Exception e) {
            mostrarError("Error inesperado al crear el perfil del paciente.");
            return;
        }

        // ── Paso 2: registrar credenciales en MSAuth ───────────────────────
        // FIX ALTO: si este paso falla, el usuario queda en BD sin credenciales.
        // Mitigación: se intenta desactivar el usuario creado (transacción
        // compensatoria) para que no quede en un estado silencioso.
        // La solución definitiva es un endpoint atómico en MSUserManagement que
        // ejecute ambos pasos dentro de una única transacción coordinada.
        try {
            authClient.registrarCredencial(
                    creado.getId(),
                    usuarioNuevo.getLogin(),
                    usuarioNuevo.getPassword()
            );
        } catch (Exception credEx) {
            // Compensación: marcar el usuario como inactivo para evitar un registro
            // huérfano que nunca podrá iniciar sesión.
            try {
                usuarioClient.desactivarUsuario(creado.getId());
            } catch (Exception rollbackEx) {
                // Si la compensación también falla, al menos dejamos registro claro.
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
