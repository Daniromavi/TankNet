package es.studium.tanknet.main;

import es.studium.tanknet.core.ConfigManager;
import es.studium.tanknet.core.ThemeManager;
import es.studium.tanknet.model.Configuracion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/es/studium/tanknet/view/InicioLayout.fxml"));
        BorderPane rootLayout = fxmlLoader.load(); // BorderPane = la raíz del FXML

        Scene scene = new Scene(rootLayout, 1000, 700);
        Configuracion config = ConfigManager.cargarConfiguracion();
        ThemeManager.aplicarTema(scene, config.isDarkMode());
        URL cssUrl = getClass().getResource("/styles/bootstrapfx.css");
        System.out.println("CSS URL: " + cssUrl);
        scene.getStylesheets().add(Objects.requireNonNull(cssUrl).toExternalForm());
        stage.setTitle("redacted");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}