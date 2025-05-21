package es.studium.tanknet.core;

import es.studium.tanknet.controller.DetallesDispositivoController;
import es.studium.tanknet.model.Dispositivo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class NavigationManager {

    // Layout principal donde se cargan las vistas (se establece desde el MainController)
    private static BorderPane mainLayout;

    // Se llama al iniciar la app para registrar el layout principal
    public static void setMainLayout(BorderPane layout) {
        mainLayout = layout;
    }

    // Carga una vista FXML y la coloca en el centro del layout
    public static void setView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(NavigationManager.class.getResource(fxmlPath));
            mainLayout.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo cargar la vista: " + fxmlPath);
        }
    }

    // Carga una vista FXML y le pasa datos (un Dispositivo) a su controlador antes de mostrarla
    public static void setViewWithData(String fxmlPath, Dispositivo dispositivo) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Obtener el controlador y pasarle el dispositivo escaneado
            DetallesDispositivoController controller = loader.getController();
            controller.setDispositivo(dispositivo);

            mainLayout.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
