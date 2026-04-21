package com.piedrazul.frontend.controller;

import com.piedrazul.frontend.app.StageInitializer;
import com.piedrazul.frontend.client.UsuarioClient;
import com.piedrazul.frontend.http.HttpException;
import com.piedrazul.frontend.model.dto.UsuarioDTO;
import com.piedrazul.frontend.observer.AppEvent;
import com.piedrazul.frontend.observer.EventBus;
import com.piedrazul.frontend.observer.Observer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de gestión de usuarios.
 *
 * CAMBIOS RESPECTO AL MONOLITO:
 * - Eliminado: @Component, IUsuarioService (local), context::getBean para FormUsuarioController
 * - Añadido:   UsuarioClient (HTTP)
 * - cargarUsuarios() → GET /usuarios al API Gateway (async)
 * - handleToggleActivo() → PATCH /usuarios/{id}/activar|desactivar (async)
 * - abrirFormulario() usa StageInitializer con ControllerFactory (sin getBean de Spring)
 * - La tabla se recarga en hilo UI via Platform.runLater tras cada operación
 */
public class ListaUsuariosController implements Observer<UsuarioDTO> {

    @FXML private TableView<UsuarioDTO>            tablaUsuarios;
    @FXML private TableColumn<UsuarioDTO, String>  colNombre;
    @FXML private TableColumn<UsuarioDTO, String>  colRol;
    @FXML private TableColumn<UsuarioDTO, Boolean> colActivo;
    @FXML private TextField                        txtBuscar;
    @FXML private Button                           btnEditar;
    @FXML private Button                           btnToggleActivo;
    @FXML private Label                            lblEstado;

    private final UsuarioClient    usuarioClient;
    private final StageInitializer stageInitializer;
    private final EventBus         eventBus;

    private ObservableList<UsuarioDTO> todosLosUsuarios = FXCollections.observableArrayList();

    public ListaUsuariosController(UsuarioClient usuarioClient,
                                   StageInitializer stageInitializer,
                                   EventBus eventBus) {
        this.usuarioClient   = usuarioClient;
        this.stageInitializer = stageInitializer;
        this.eventBus         = eventBus;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSeleccion();

        eventBus.subscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.subscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.subscribe(AppEvent.USUARIO_DESACTIVADO, this);

        cargarUsuarios();
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
                .filter(u -> u.getNombreCompleto().toLowerCase().contains(filtro)
                          || u.getLogin().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
        tablaUsuarios.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    private void handleNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void handleEditar() {
        UsuarioDTO seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) abrirFormulario(seleccionado);
    }

    /**
     * CAMBIO: antes llamaba usuarioService.activarUsuario/desactivarUsuario (síncrono).
     * Ahora hace PATCH HTTP en hilo secundario y recarga la tabla al terminar.
     */
    @FXML
    private void handleToggleActivo() {
        UsuarioDTO sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        btnToggleActivo.setDisable(true);
        new Thread(() -> {
            try {
                if (sel.getActivo()) {
                    usuarioClient.desactivarUsuario(sel.getId());
                    Platform.runLater(() -> lblEstado.setText("Usuario desactivado correctamente."));
                } else {
                    usuarioClient.activarUsuario(sel.getId());
                    Platform.runLater(() -> lblEstado.setText("Usuario activado correctamente."));
                }
                Platform.runLater(this::cargarUsuarios);
            } catch (HttpException ex) {
                Platform.runLater(() -> lblEstado.setText(
                        "Error al cambiar estado: " + ex.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> lblEstado.setText("Error de conexión."));
            } finally {
                Platform.runLater(() -> btnToggleActivo.setDisable(false));
            }
        }).start();
    }

    @FXML
    private void volver() {
        eventBus.unsubscribe(AppEvent.USUARIO_CREADO,      this);
        eventBus.unsubscribe(AppEvent.USUARIO_ACTUALIZADO, this);
        eventBus.unsubscribe(AppEvent.USUARIO_DESACTIVADO, this);
        stageInitializer.cambiarVista(
                "/view/fxml/dashboard/dashboard-admin.fxml",
                "Piedrazul - Dashboard", 900, 600);
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNombreCompleto()));
        colRol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRol().name()));
        colActivo.setCellValueFactory(d ->
                new SimpleBooleanProperty(d.getValue().getActivo()));
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                setText(empty ? "" : (activo ? "Activo" : "Inactivo"));
            }
        });
    }

    private void configurarSeleccion() {
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> {
                    boolean hay = actual != null;
                    btnEditar.setDisable(!hay);
                    btnToggleActivo.setDisable(!hay);
                    if (hay) btnToggleActivo.setText(actual.getActivo() ? "Desactivar" : "Activar");
                });
    }

    /**
     * CAMBIO: antes era usuarioService.listarTodos() (llamada local síncrona).
     * Ahora es GET /usuarios en hilo secundario para no bloquear la UI.
     */
    private void cargarUsuarios() {
        new Thread(() -> {
            try {
                List<UsuarioDTO> lista = usuarioClient.listarTodos();
                Platform.runLater(() -> {
                    todosLosUsuarios = FXCollections.observableArrayList(lista);
                    tablaUsuarios.setItems(todosLosUsuarios);
                    lblEstado.setText("Total: " + todosLosUsuarios.size() + " usuarios");
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblEstado.setText("Error al cargar usuarios."));
            }
        }).start();
    }

    // CAMBIO: ya no usa context::getBean — StageInitializer usa ControllerFactory internamente
    private void abrirFormulario(UsuarioDTO usuario) {
        stageInitializer.abrirModal(
                "/view/fxml/usuarios/form-usuarios.fxml",
                usuario == null ? "Nuevo Usuario" : "Editar Usuario",
                500, 450,
                loader -> {
                    FormUsuarioController ctrl = loader.getController();
                    ctrl.setUsuario(usuario);
                });
    }
}
