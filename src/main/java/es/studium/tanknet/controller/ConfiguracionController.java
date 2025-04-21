package es.studium.tanknet.controller;

import es.studium.tanknet.core.ConfigManager;
import es.studium.tanknet.core.IdiomaManager;
import es.studium.tanknet.core.NavigationManager;
import es.studium.tanknet.core.ThemeManager;
import es.studium.tanknet.model.Configuracion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import java.io.IOException;

public class ConfiguracionController {

    @FXML
    private CheckBox darkModeCheck;

    @FXML
    private ChoiceBox<String> languageSelector;

    @FXML
    private Label titleLabel;
    @FXML
    private Label darkModeLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Button guardarButton;
    @FXML
    private Button volverButton;

    @FXML
    public void initialize() {
        Configuracion config = ConfigManager.cargarConfiguracion();
        darkModeCheck.setSelected(config.isDarkMode());

        // Idioma actual desde el bundle
        String lang = IdiomaManager.getLocale().getLanguage();
        languageSelector.setValue(lang.equals("en") ? "Inglés" : "Español");

        // Traducciones
        titleLabel.setText(IdiomaManager.get("config.title"));
        darkModeLabel.setText(IdiomaManager.get("config.darkmode"));
        languageLabel.setText(IdiomaManager.get("config.language"));
        guardarButton.setText(IdiomaManager.get("config.save"));
        volverButton.setText(IdiomaManager.get("config.return"));
    }


    @FXML
    public void volverAlInicio() {
        NavigationManager.setView("/es/studium/tanknet/view/InicioCentro.fxml");
    }

    @FXML
    public void guardarConfiguracion() {
        boolean darkMode = darkModeCheck.isSelected();
        String lang = languageSelector.getValue().equals("Inglés") ? "en" : "es";

        Configuracion config = new Configuracion(darkMode);
        config.setIdioma(lang);
        ConfigManager.guardarConfiguracion(config);

        ThemeManager.aplicarTema(darkModeCheck.getScene(), darkMode);
        IdiomaManager.cargarIdioma(lang);

        // Recargar la vista actual con el nuevo idioma
        NavigationManager.setView("/es/studium/tanknet/view/Configuracion.fxml");
    }
}