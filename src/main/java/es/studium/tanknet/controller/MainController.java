package es.studium.tanknet.controller;

import es.studium.tanknet.core.NavigationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane InicioLayout; // Vista principal (fx:id="mainLayout" en el FXML)

    @FXML
    public void initialize() {
        // Registrar el layout principal para navegación global
        NavigationManager.setMainLayout(InicioLayout);

        // Cargar la vista inicial (pantalla de bienvenida o escaneo)
        NavigationManager.setView("/es/studium/tanknet/view/InicioCentro.fxml");
    }

    // Vista en caché para no volver a cargarla cada vez que se abre
    private Node vistaEscanear = null;

    public void handleEscanearRed() {
        try {
            // Cargar la vista solo la primera vez (se reutiliza)
            if (vistaEscanear == null) {
                vistaEscanear = FXMLLoader.load(getClass().getResource("/es/studium/tanknet/view/Escanear.fxml"));
            }
            // Mostrarla en el centro del BorderPane
            InicioLayout.setCenter(vistaEscanear);
        } catch (IOException e) {
            e.printStackTrace(); // Error de carga del FXML
        }
    }

    public void handleVerInformes() {
        // Por implementar: cambiar a vista de informes generados
    }

    @FXML
    public void handleConfiguracion() {
        try {
            // Cargar la vista de configuración (no se cachea)
            Node configuracionView = FXMLLoader.load(getClass().getResource("/es/studium/tanknet/view/Configuracion.fxml"));
            InicioLayout.setCenter(configuracionView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
