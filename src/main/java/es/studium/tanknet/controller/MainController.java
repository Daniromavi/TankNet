package es.studium.tanknet.controller;

import es.studium.tanknet.core.ConfigManager;
import es.studium.tanknet.core.IdiomaManager;
import es.studium.tanknet.core.NavigationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane InicioLayout; // esto se enlaza con fx:id="mainLayout"

    @FXML
    public void initialize() {
        IdiomaManager.cargarIdioma(ConfigManager.cargarConfiguracion().getIdioma());

        NavigationManager.setMainLayout(InicioLayout);
        NavigationManager.setView("/es/studium/tanknet/view/InicioCentro.fxml");
    }

    public void handleEscanearRed() {
        try {
            Node configuracionView = FXMLLoader.load(getClass().getResource("/es/studium/tanknet/view/Escanear.fxml"));
            InicioLayout.setCenter(configuracionView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleVerInformes() {
        // Abrir vista con informes guardados
    }

    @FXML
    public void handleConfiguracion() {
        try {
            Node configuracionView = FXMLLoader.load(getClass().getResource("/es/studium/tanknet/view/Configuracion.fxml"));
            InicioLayout.setCenter(configuracionView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

