package es.studium.tanknet.core;

import javafx.scene.Scene;

public class ThemeManager {

    private static final String DARK_THEME = "/styles/dark-theme.css";
    private static final String LIGHT_THEME = "/styles/light-theme.css";
    private static final String BOOTSTRAP = "/styles/bootstrapfx.css";

    public static void aplicarTema(Scene scene, boolean darkMode) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeManager.class.getResource(BOOTSTRAP).toExternalForm());
        scene.getStylesheets().add(ThemeManager.class.getResource(
                darkMode ? DARK_THEME : LIGHT_THEME).toExternalForm());
    }
}
