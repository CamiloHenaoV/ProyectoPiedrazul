package com.piedrazul.frontend.app;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación Piedrazul Frontend.
 *
 * Sin @SpringBootApplication. JavaFX puro.
 * StageInitializer gestiona la carga de FXMLs y la navegación.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializamos el contenedor manual de dependencias (reemplaza el contexto Spring)
        AppContext context = AppContext.getInstance();
        context.initialize();

        StageInitializer stageInitializer = context.getStageInitializer();
        stageInitializer.setPrimaryStage(primaryStage);

        // Primera vista: Login
        stageInitializer.cambiarVista(
                "/view/fxml/auth/login.fxml",
                "Piedrazul - Iniciar Sesión",
                400, 300
        );

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
