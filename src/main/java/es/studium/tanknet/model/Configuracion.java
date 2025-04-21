package es.studium.tanknet.model;

import java.util.Locale;

public class Configuracion {
    private boolean darkMode;
    private String idioma = "es"; // o "en"

    public Configuracion() {}

    public Configuracion(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getIdioma(){
        return idioma;
    }

    public void setIdioma(String idioma){
        this.idioma = idioma;
    }
}
