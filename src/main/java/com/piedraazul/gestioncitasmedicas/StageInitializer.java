package com.piedraazul.gestioncitasmedicas;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URL;
import com.piedraazul.gestioncitasmedicas.JavaFxApplication.StageReadyEvent;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final ApplicationContext context;

    public StageInitializer(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            URL url = getClass().getResource("/views/main.fxml");
            if (url == null) {
                throw new RuntimeException("No se encontró el archivo FXML en /views/main.fxml");
            }
            FXMLLoader loader = new FXMLLoader(url);

            loader.setControllerFactory(context::getBean);

            Parent root = loader.load();
            Stage stage = event.getStage();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("MediGest - Sistema de Citas");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fatal al cargar la interfaz gráfica", e);
        }
    }
}