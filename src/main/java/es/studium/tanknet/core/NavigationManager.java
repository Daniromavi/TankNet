package es.studium.tanknet.core;

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
}
