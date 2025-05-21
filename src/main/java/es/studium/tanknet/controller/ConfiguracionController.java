package es.studium.tanknet.controller;

import es.studium.tanknet.core.ConfigManager;
import es.studium.tanknet.core.NavigationManager;
import es.studium.tanknet.core.ThemeManager;
import es.studium.tanknet.model.Configuracion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class ConfiguracionController {

    @FXML
    private CheckBox darkModeCheck;

    @FXML
    public void initialize() {
        // Cargar la configuración actual desde disco y reflejarla en el checkbox
        Configuracion config = ConfigManager.cargarConfiguracion();
        darkModeCheck.setSelected(config.isDarkMode());
    }

    @FXML
    public void volverAlInicio() {
        // Cambiar la vista al menú principal
        NavigationManager.setView("/es/studium/tanknet/view/InicioCentro.fxml");
    }

    @FXML
    public void guardarConfiguracion() {
        boolean darkMode = darkModeCheck.isSelected();

        // Guardar el valor actual del modo oscuro en la configuración
        Configuracion config = new Configuracion(darkMode);
        ConfigManager.guardarConfiguracion(config);

        // Aplicar el tema seleccionado (oscuro o claro) a la escena
        ThemeManager.aplicarTema(darkModeCheck.getScene(), darkMode);

        // Recargar la vista para que el cambio de tema se aplique visualmente
        NavigationManager.setView("/es/studium/tanknet/view/Configuracion.fxml");
    }
}
