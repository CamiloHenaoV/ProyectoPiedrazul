package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.exceptions.LoginDuplicadoException;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class FormUsuarioController {

    @FXML private Label                lblTitulo;
    @FXML private TextField            txtNombre;
    @FXML private TextField            txtLogin;
    @FXML private PasswordField        txtPassword;
    @FXML private ComboBox<RolUsuario> cbRol;
    @FXML private Label                lblError;

    private final IUsuarioService usuarioService;
    private final EventBus        eventBus;

    private UsuarioDTO usuarioEditar;

    public FormUsuarioController(IUsuarioService usuarioService,
                                 EventBus eventBus) {
        this.usuarioService = usuarioService;
        this.eventBus       = eventBus;
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
                UsuarioDTO nuevo = UsuarioDTO.builder()
                        .nombreCompleto(txtNombre.getText().trim())
                        .login(txtLogin.getText().trim())
                        .password(txtPassword.getText())
                        .rol(cbRol.getValue())
                        .activo(true)
                        .build();
                usuarioService.crearUsuario(nuevo);
            } else {
                UsuarioDTO actualizado = UsuarioDTO.builder()
                        .id(usuarioEditar.getId())
                        .nombreCompleto(txtNombre.getText().trim())
                        .login(usuarioEditar.getLogin())
                        .rol(cbRol.getValue())
                        .activo(usuarioEditar.getActivo())
                        .build();
                usuarioService.actualizarUsuario(usuarioEditar.getId(), actualizado);
            }
            cerrarModal();

        } catch (LoginDuplicadoException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error inesperado al guardar.");
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
        if (usuarioEditar == null && txtLogin.getText().isBlank()) {
            mostrarError("El login es obligatorio.");
            return false;
        }
        if (usuarioEditar == null && txtPassword.getText().isBlank()) {
            mostrarError("La contraseña es obligatoria.");
            return false;
        }
        if (cbRol.getValue() == null) {
            mostrarError("Selecciona un rol.");
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