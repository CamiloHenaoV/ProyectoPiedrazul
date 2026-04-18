package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.AuthClient;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.model.dto.UsuarioRegistroDTO;
import com.piedrazul.frontend.model.enums.RolUsuario;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Formulario de creación y edición de usuarios.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, IRegistroService, IUsuarioService (locales)
 * - Añadido:   UsuarioClient (HTTP), EventBus para publicar eventos
 * - Al crear paciente/profesional se abre el modal correspondiente vía StageInitializer
 * - Tras guardar exitosamente se publica el evento para que ListaUsuariosController
 *   y DashboardAdminController recarguen automáticamente sus datos
 */
public class FormUsuarioController {

    @FXML private Label                lblTitulo;
    @FXML private TextField            txtNombre;
    @FXML private TextField            txtLogin;
    @FXML private PasswordField        txtPassword;
    @FXML private TextField            txtPasswordVisible;
    @FXML private CheckBox             chkMostrarPassword;
    @FXML private ComboBox<RolUsuario> cbRol;
    @FXML private Label                lblError;

    private final UsuarioClient    usuarioClient;
    private final AuthClient authClient;
    private final StageInitializer stageInitializer;
    private final EventBus         eventBus;

    private UsuarioDTO usuarioEditar;
    private boolean    passwordVisible = false;

    public FormUsuarioController(UsuarioClient usuarioClient,
                                 AuthClient authClient,
                                 StageInitializer stageInitializer,
                                 EventBus eventBus) {
        this.usuarioClient   = usuarioClient;
        this.authClient      = authClient;
        this.stageInitializer = stageInitializer;
        this.eventBus         = eventBus;
    }

    @FXML
    public void initialize() {
        cbRol.setItems(FXCollections.observableArrayList(RolUsuario.values()));
        lblError.setVisible(false);
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuarioEditar = usuario;
        if (usuario != null) {
            lblTitulo.setText("Editar Usuario");
            txtNombre.setText(usuario.getNombreCompleto());
            txtLogin.setText(usuario.getLogin());
            txtLogin.setDisable(true);
            txtPassword.setDisable(true);
            cbRol.setValue(usuario.getRol());
        } else {
            lblTitulo.setText("Nuevo Usuario");
        }
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) return;

        try {
            if (usuarioEditar == null) {
                crearNuevoUsuario();
            } else {
                editarUsuarioExistente();
            }
        } catch (HttpException ex) {
            if (ex.isConflict())
                mostrarError("El login ya está en uso.");
            else
                mostrarError("Error del servidor (" + ex.getStatusCode() + ").");
        } catch (Exception e) {
            mostrarError("Error inesperado al guardar.");
        }
    }

    private void crearNuevoUsuario() throws Exception {
        UsuarioRegistroDTO nuevo = UsuarioRegistroDTO.builder()
                .nombreCompleto(txtNombre.getText().trim())
                .login(txtLogin.getText().trim())
                .password(passwordVisible ? txtPasswordVisible.getText() : txtPassword.getText())
                .rol(cbRol.getValue())
                .build();

        RolUsuario rol = cbRol.getValue();

        if (rol == RolUsuario.paciente) {
            // Cerrar este modal y abrir el de datos de paciente
            stageInitializer.abrirModal(
                    "/view/fxml/usuarios/form-paciente.fxml",
                    "Datos del Paciente", 480, 420,
                    loader -> {
                        FormPacienteController ctrl = loader.getController();
                        ctrl.setUsuarioNuevo(nuevo);
                    });
            cerrarModal();

        } else if (rol == RolUsuario.profesional) {
            stageInitializer.abrirModal(
                    "/view/fxml/usuarios/form-profesional.fxml",
                    "Datos del Profesional", 420, 340,
                    loader -> {
                        FormProfesionalController ctrl = loader.getController();
                        ctrl.setUsuarioNuevo(nuevo);
                    });
            cerrarModal();
        } else {
            // Administrador: POST /usuarios directo
            UsuarioDTO usuarioDTO = UsuarioDTO.builder()
                    .nombreCompleto(nuevo.getNombreCompleto())
                    .login(nuevo.getLogin())
                    .rol(nuevo.getRol())
                    .activo(true)
                    .build();
            UsuarioDTO creado = usuarioClient.crearUsuario(usuarioDTO);
            authClient.registrarCredencial(creado.getId(), nuevo.getLogin(), nuevo.getPassword());
            // Publicar evento para que ListaUsuarios y Dashboard se actualicen
            eventBus.publish(AppEvent.USUARIO_CREADO, creado);
            cerrarModal();
        }
    }

    private void editarUsuarioExistente() throws Exception {
        UsuarioDTO actualizado = UsuarioDTO.builder()
                .id(usuarioEditar.getId())
                .nombreCompleto(txtNombre.getText().trim())
                .login(usuarioEditar.getLogin())
                .rol(cbRol.getValue())
                .activo(usuarioEditar.getActivo())
                .build();

        usuarioClient.actualizarUsuario(usuarioEditar.getId(), actualizado);
        eventBus.publish(AppEvent.USUARIO_ACTUALIZADO, actualizado);
        cerrarModal();
    }

    @FXML
    private void handleCancelar() {
        cerrarModal();
    }

    @FXML
    private void handleMostrarPassword() {
        passwordVisible = chkMostrarPassword.isSelected();
        if (passwordVisible) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank()) {
            mostrarError("El nombre es obligatorio."); return false;
        }
        if (usuarioEditar == null && txtLogin.getText().isBlank()) {
            mostrarError("El login es obligatorio."); return false;
        }
        if (usuarioEditar == null
                && txtPassword.getText().isBlank()
                && txtPasswordVisible.getText().isBlank()) {
            mostrarError("La contraseña es obligatoria."); return false;
        }
        if (usuarioEditar == null
                && txtPassword.getText().length() < 8
                && txtPasswordVisible.getText().length() < 8) {
            mostrarError("La contraseña debe tener al menos 8 caracteres."); return false;
        }
        if (cbRol.getValue() == null) {
            mostrarError("Selecciona un rol."); return false;
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
