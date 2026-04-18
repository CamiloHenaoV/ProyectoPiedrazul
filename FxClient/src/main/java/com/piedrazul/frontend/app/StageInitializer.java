package com.piedrazul.frontend.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Gestiona la navegación entre vistas JavaFX.
 *
 * Equivalente al StageInitializer del monolito, pero sin Spring.
 * Usa AppContext para inyectar dependencias en los controladores
 * a través de ControllerFactory.
 */
public class StageInitializer {

    private Stage      primaryStage;
    private final AppContext context;

    public StageInitializer(AppContext context) {
        this.context = context;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // ── Navegación principal ─────────────────────────────────────

    /** Cambia la vista principal sin devolver el loader. */
    public void cambiarVista(String fxmlPath, String titulo, double ancho, double alto) {
        cambiarVistaConLoader(fxmlPath, titulo, ancho, alto);
    }

    /**
     * Cambia la vista principal y devuelve el FXMLLoader para que
     * el caller pueda pasar datos al controlador destino
     * (p.ej. setUsuarioActual).
     */
    public FXMLLoader cambiarVistaConLoader(String fxmlPath, String titulo,
                                            double ancho, double alto) {
        try {
            FXMLLoader loader = crearLoader(fxmlPath);
            Parent root = loader.load();
            primaryStage.setTitle(titulo);
            primaryStage.setScene(new Scene(root, ancho, alto));
            return loader;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista: " + fxmlPath, e);
        }
    }

    // ── Modales ──────────────────────────────────────────────────

    /**
     * Abre una ventana modal y ejecuta el callback para configurar
     * el controlador una vez que el FXML está cargado.
     */
    public <T> void abrirModal(String fxmlPath, String titulo,
                                double ancho, double alto,
                                Consumer<FXMLLoader> configurar) {
        try {
            FXMLLoader loader = crearLoader(fxmlPath);
            Parent root = loader.load();
            configurar.accept(loader);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);
            modal.setTitle(titulo);
            modal.setScene(new Scene(root, ancho, alto));
            modal.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir el modal: " + fxmlPath, e);
        }
    }

    // ── Interno ──────────────────────────────────────────────────

    /**
     * Crea un FXMLLoader con una ControllerFactory que inyecta
     * dependencias desde AppContext (reemplaza context::getBean de Spring).
     */
    private FXMLLoader crearLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(new ControllerFactory(context));
        return loader;
    }
}
