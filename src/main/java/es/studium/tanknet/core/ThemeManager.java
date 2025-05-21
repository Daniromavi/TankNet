package es.studium.tanknet.core;

import javafx.scene.Scene;

public class ThemeManager {

    // Rutas a los estilos CSS del tema oscuro, claro y base (BootstrapFX)
    private static final String DARK_THEME = "/styles/dark-theme.css";
    private static final String LIGHT_THEME = "/styles/light-theme.css";
    private static final String BOOTSTRAP = "/styles/bootstrapfx.css";

    // Aplica el tema (oscuro o claro) a una escena JavaFX
    public static void aplicarTema(Scene scene, boolean darkMode) {
        scene.getStylesheets().clear(); // Limpiamos estilos previos
        scene.getStylesheets().add(ThemeManager.class.getResource(BOOTSTRAP).toExternalForm()); // Estilo base
        scene.getStylesheets().add(ThemeManager.class.getResource(
                darkMode ? DARK_THEME : LIGHT_THEME).toExternalForm()); // Estilo personalizado según modo
    }
}
