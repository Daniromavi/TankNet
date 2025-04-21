package es.studium.tanknet.core;

import java.util.Locale;
import java.util.ResourceBundle;

public class IdiomaManager {
    private static ResourceBundle bundle;

    public static void cargarIdioma(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static Locale getLocale() {
        return bundle.getLocale();
    }
}
