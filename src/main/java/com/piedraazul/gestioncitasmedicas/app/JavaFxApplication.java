package com.piedraazul.gestioncitasmedicas.app;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.piedraazul.gestioncitasmedicas")
@EnableJpaRepositories(basePackages = "com.piedraazul.gestioncitasmedicas.model.repositories")
@EntityScan(basePackages = "com.piedraazul.gestioncitasmedicas.model.entities")
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        this.context = new SpringApplicationBuilder()
                .sources(GestionCitasMedicasApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) {
        this.context.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() {
        this.context.close();
        Platform.exit();
    }

    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
        public Stage getStage() {
            return (Stage) getSource();
        }
    }
}