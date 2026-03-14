package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import com.piedrazul.gestioncitasmedicas.observer.AppEvent;
import com.piedrazul.gestioncitasmedicas.observer.EventBus;
import com.piedrazul.gestioncitasmedicas.observer.Observer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListaUsuariosController implements Observer<UsuarioDTO> {

    @FXML private TableView<UsuarioDTO>            tablaUsuarios;
    @FXML private TableColumn<UsuarioDTO, String>  colNombre;
    @FXML private TableColumn<UsuarioDTO, String>  colRol;
    @FXML private TableColumn<UsuarioDTO, Boolean> colActivo;
    @FXML private TextField                        txtBuscar;
    @FXML private Button                           btnEditar;
    @FXML private Button                           btnToggleActivo;
    @FXML private Label                            lblEstado;

    @Autowired private IUsuarioService  usuarioService;
    @Autowired private StageInitializer stageInitializer;
    @Autowired private EventBus         eventBus;

    private ObservableList<UsuarioDTO> todosLosUsuarios = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSeleccion();

        eventBus.subscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.subscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.subscribe(AppEvent.USUARIO_DESACTIVADO, this);

        cargarUsuarios();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNombreCompleto()
                )
        );
        colRol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getRol().name()
                )
        );
        colActivo.setCellValueFactory(data ->
                new javafx.beans.property.SimpleBooleanProperty(
                        data.getValue().getActivo()
                )
        );
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                setText(empty || activo == null ? "" : activo ? "Activo" : "Inactivo");
            }
        });
    }

    private void configurarSeleccion() {
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> {
                    boolean haySeleccion = actual != null;
                    btnEditar.setDisable(!haySeleccion);
                    btnToggleActivo.setDisable(!haySeleccion);
                    if (haySeleccion) {
                        btnToggleActivo.setText(actual.getActivo() ? "Desactivar" : "Activar");
                    }
                }
        );
    }

    @Override
    public void onEvent(AppEvent event, UsuarioDTO data) {
        Platform.runLater(this::cargarUsuarios);
    }

    @FXML
    private void handleBuscar() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        if (filtro.isEmpty()) {
            tablaUsuarios.setItems(todosLosUsuarios);
            return;
        }
        List<UsuarioDTO> filtrados = todosLosUsuarios.stream()
                .filter(u ->
                        u.getNombreCompleto().toLowerCase().contains(filtro) ||
                                u.getLogin().toLowerCase().contains(filtro)
                )
                .collect(Collectors.toList());
        tablaUsuarios.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void handleNuevo() {
        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/usuarios/form-usuario.fxml",
                "Piedrazul - Nuevo Usuario",
                500, 450
        );
        FormUsuarioController controller = loader.getController();
        controller.setUsuario(null);
    }

    @FXML
    private void handleEditar() {
        UsuarioDTO seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        FXMLLoader loader = stageInitializer.cambiarVistaConLoader(
                "/view/fxml/usuarios/form-usuario.fxml",
                "Piedrazul - Editar Usuario",
                500, 450
        );
        FormUsuarioController controller = loader.getController();
        controller.setUsuario(seleccionado);
    }

    @FXML
    private void handleToggleActivo() {
        UsuarioDTO seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        if (seleccionado.getActivo()) {
            usuarioService.desactivarUsuario(seleccionado.getId());
            lblEstado.setText("Usuario desactivado correctamente.");
        } else {
            usuarioService.activarUsuario(seleccionado.getId());
            lblEstado.setText("Usuario activado correctamente.");
        }
        cargarUsuarios();
    }

    @FXML
    private void volver() {
        eventBus.unsubscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.unsubscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.unsubscribe(AppEvent.USUARIO_DESACTIVADO, this);
        stageInitializer.cambiarVista(
                "/view/fxml/dashboard/dashboard-admin.fxml",
                "Piedrazul - Dashboard",
                900, 600
        );
    }

    private void cargarUsuarios() {
        todosLosUsuarios = FXCollections.observableArrayList(
                usuarioService.listarTodos()
        );
        tablaUsuarios.setItems(todosLosUsuarios);
        lblEstado.setText("Total: " + todosLosUsuarios.size() + " usuarios");
    }


}