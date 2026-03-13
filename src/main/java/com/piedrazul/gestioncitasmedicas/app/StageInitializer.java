package com.piedrazul.gestioncitasmedicas.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

import com.piedrazul.gestioncitasmedicas.app.JavaFxApplication.StageReadyEvent;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final ApplicationContext context;
    private Stage primaryStage;

    public StageInitializer(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        this.primaryStage = event.getStage();
        cambiarVista("/view/fxml/auth/login.fxml", "Gestión Citas medicas - Iniciar Sesión", 400, 300);
    }

    public void cambiarVista(String rutaFxml, String titulo, double ancho, double alto) {
        try {
            // Con "/" busca desde la raíz de resources
            URL url = getClass().getResource(rutaFxml);

            if (url == null) {
                throw new RuntimeException("No se encontró el FXML: " + rutaFxml);
            }

            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(context::getBean);

            Parent root = loader.load();
            primaryStage.setScene(new Scene(root, ancho, alto));
            primaryStage.setTitle(titulo);
            primaryStage.show();

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la vista: " + rutaFxml, e);
        }
    }

    public void cambiarVista(String rutaFxml, String titulo) {
        cambiarVista(rutaFxml, titulo,
                primaryStage.getScene().getWidth(),
                primaryStage.getScene().getHeight()
        );
    }
}