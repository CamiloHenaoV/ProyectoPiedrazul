package com.piedrazul.gestioncitasmedicas.controller;

import com.piedrazul.gestioncitasmedicas.app.StageInitializer;
import com.piedrazul.gestioncitasmedicas.model.dto.UsuarioDTO;
import com.piedrazul.gestioncitasmedicas.model.entities.enums.RolUsuario;
import com.piedrazul.gestioncitasmedicas.model.exceptions.LoginDuplicadoException;
import com.piedrazul.gestioncitasmedicas.model.services.interfaces.IUsuarioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Controlador JavaFX del formulario de creación y edición de usuarios
 * ({@code /view/fxml/usuarios/form-usuario.fxml}).
 *
 * <p>Se abre como ventana modal ({@link javafx.stage.Modality#APPLICATION_MODAL}).
 * Opera en dos modos
 * según el valor recibido en {@link #setUsuario(UsuarioDTO)}:
 * <ul>
 *   <li><strong>Creación</strong> ({@code usuario == null}): todos los campos están
 *       habilitados; al guardar se invoca {@link IUsuarioService#crearUsuario(UsuarioDTO)}.</li>
 *   <li><strong>Edición</strong> ({@code usuario != null}): los campos {@code txtLogin} y
 *       {@code txtPassword} se deshabilitan porque el login es inmutable y el cambio de
 *       contraseña no está soportado en este formulario; al guardar se invoca
 *       {@link IUsuarioService#actualizarUsuario(UUID, UsuarioDTO)}.</li>
 * </ul>
 * </p>
 *
 * <p>Tras una operación exitosa el modal se cierra automáticamente mediante
 * {@link #cerrarModal()} y la tabla de {@link ListaUsuariosController} se actualiza.</p>
 *
 * <p>Las dependencias se inyectan por constructor.</p>
 *
 * @see IUsuarioService#crearUsuario(UsuarioDTO)
 * @see IUsuarioService#actualizarUsuario(UUID, UsuarioDTO)
 * @see ListaUsuariosController
 */
@Component
public class FormUsuarioController {

    @FXML private Label                lblTitulo;
    @FXML private TextField            txtNombre;
    @FXML private TextField            txtLogin;
    @FXML private PasswordField        txtPassword;
    @FXML private ComboBox<RolUsuario> cbRol;
    @FXML private Label                lblError;
    private final StageInitializer stageInitializer;
    private final IUsuarioService usuarioService;
    private UsuarioDTO usuarioEditar;

    /**
     * Construye el controlador con las dependencias requeridas.
     *
     * @param usuarioService servicio para crear y actualizar usuarios
     */
    public FormUsuarioController(IUsuarioService usuarioService,
                                 StageInitializer stageInitializer) {

        this.usuarioService = usuarioService;
        this.stageInitializer=stageInitializer;
    }

    /**
     * Inicializa la vista una vez que el FXML ha sido cargado.
     *
     * <p>Popula el {@code cbRol} con todos los valores del enum {@link RolUsuario}
     * y oculta {@code lblError}. La precarga de datos del usuario (modo edición)
     * se realiza de forma diferida en {@link #setUsuario(UsuarioDTO)}.</p>
     */
    @FXML
    public void initialize() {
        cbRol.setItems(FXCollections.observableArrayList(RolUsuario.values()));
        lblError.setVisible(false);
    }

    /**
     * Configura el formulario según el usuario recibido.
     *
     * <p>Si {@code usuario} es {@code null}, el formulario queda en modo creación
     * con el título "Nuevo Usuario" y todos los campos vacíos y habilitados.</p>
     *
     * <p>Si {@code usuario} no es {@code null}, el formulario entra en modo edición:
     * <ul>
     *   <li>El título cambia a "Editar Usuario".</li>
     *   <li>{@code txtNombre} y {@code cbRol} se precargan con los datos actuales.</li>
     *   <li>{@code txtLogin} se precarga y se deshabilita (el login es inmutable).</li>
     *   <li>{@code txtPassword} se deshabilita (el cambio de contraseña no está
     *       soportado en este flujo).</li>
     * </ul>
     * </p>
     *
     * <p>Debe invocarse después de que JavaFX haya ejecutado {@link #initialize()}}.</p>
     *
     * @param usuario DTO del usuario a editar, o {@code null} para crear uno nuevo
     */
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

    /**
     * Valida los campos del formulario y persiste el usuario.
     *
     * <p>Flujo de ejecución:
     * <ol>
     *   <li>Llama a {@link #validarCampos()}; si falla, muestra el error y aborta.</li>
     *   <li>Si {@code usuarioEditar} es {@code null}, construye un nuevo {@link UsuarioDTO}
     *       con {@code activo = true} y llama a
     *       {@link IUsuarioService#crearUsuario(UsuarioDTO)}.</li>
     *   <li>Si {@code usuarioEditar} no es {@code null}, construye el DTO de actualización
     *       preservando el {@code id}, el {@code login} original y el estado {@code activo},
     *       y llama a {@link IUsuarioService#actualizarUsuario(UUID, UsuarioDTO)}.</li>
     *   <li>En caso de éxito, cierra el modal mediante {@link #cerrarModal()}.</li>
     *   <li>Si el servicio lanza {@link LoginDuplicadoException}, muestra su mensaje.
     *       Cualquier otra excepción muestra un error genérico.</li>
     * </ol>
     * </p>
     */
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

                RolUsuario rol = cbRol.getValue();

                if (rol == RolUsuario.paciente) {
                    cerrarModal();
                    stageInitializer.abrirModal(
                            "/view/fxml/usuarios/form-paciente.fxml",
                            "Datos del Paciente",
                            480, 420,
                            loader -> {
                                FormPacienteController ctrl = loader.getController();
                                ctrl.setUsuarioNuevo(nuevo);
                            }
                    );
                } else if (rol == RolUsuario.profesional) {
                    cerrarModal();
                    stageInitializer.abrirModal(
                            "/view/fxml/usuarios/form-profesional.fxml",
                            "Datos del Profesional",
                            420, 340,
                            loader -> {
                                FormProfesionalController ctrl = loader.getController();
                                ctrl.setUsuarioNuevo(nuevo);
                            }
                    );
                } else {
                    usuarioService.crearUsuario(nuevo);
                    cerrarModal();
                }

            } else {
                UsuarioDTO actualizado = UsuarioDTO.builder()
                        .id(usuarioEditar.getId())
                        .nombreCompleto(txtNombre.getText().trim())
                        .login(usuarioEditar.getLogin())
                        .rol(cbRol.getValue())
                        .activo(usuarioEditar.getActivo())
                        .build();
                usuarioService.actualizarUsuario(usuarioEditar.getId(), actualizado);
                cerrarModal();
            }

        } catch (LoginDuplicadoException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error inesperado al guardar.");
            e.printStackTrace();
        }
    }

    /**
     * Cierra el modal sin persistir cambios.
     *
     * <p>Invocado por el botón "Cancelar" del formulario.</p>
     */
    @FXML
    private void handleCancelar() {
        cerrarModal();
    }

    /**
     * Valida que los campos obligatorios del formulario no estén vacíos.
     *
     * <p>Reglas de validación:
     * <ul>
     *   <li>{@code txtNombre} nunca puede estar en blanco.</li>
     *   <li>{@code txtLogin} es obligatorio solo en modo creación ({@code usuarioEditar == null}).</li>
     *   <li>{@code txtPassword} es obligatoria solo en modo creación.</li>
     *   <li>{@code cbRol} debe tener un valor seleccionado en ambos modos.</li>
     * </ul>
     * Ante el primer campo inválido, muestra el mensaje de error y devuelve {@code false}.</p>
     *
     * @return {@code true} si todos los campos obligatorios son válidos; {@code false} en caso contrario
     */
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

    /**
     * Muestra un mensaje de error en {@code lblError} y lo hace visible.
     *
     * @param mensaje texto descriptivo del error a mostrar al usuario
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
    /**
     * Cierra la ventana modal obteniendo el {@link Stage} a través de cualquier
     * nodo de la escena actual ({@code txtNombre}).
     *
     * <p>Llamado en {@link #handleGuardar()} tras una persistencia exitosa y en
     * {@link #handleCancelar()} cuando el usuario decide no guardar cambios.</p>
     */
    private void cerrarModal() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
}