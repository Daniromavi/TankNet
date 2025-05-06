package es.studium.tanknet.core;

import es.studium.tanknet.controller.DetallesDispositivoController;
import es.studium.tanknet.model.Dispositivo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class NavigationManager {

    private static BorderPane mainLayout;

    public static void setMainLayout(BorderPane layout) {
        mainLayout = layout;
    }

    public static void setView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(NavigationManager.class.getResource(fxmlPath));
            mainLayout.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo cargar la vista: " + fxmlPath);
        }
    }

    public static void setViewWithData(String fxmlPath, Dispositivo dispositivo) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Node vista = loader.load();

            // Accedemos al controlador y le pasamos el dispositivo
            DetallesDispositivoController controller = loader.getController();
            controller.setDispositivo(dispositivo);

            mainLayout.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
