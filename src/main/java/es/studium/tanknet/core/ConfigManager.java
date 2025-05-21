package es.studium.tanknet.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.studium.tanknet.model.Configuracion;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private static final String CONFIG_PATH = "config.json"; // Ruta del archivo de configuración
    private static final ObjectMapper mapper = new ObjectMapper(); // Jackson para manejar JSON

    // Guarda la configuración (modo oscuro, etc.) en un archivo JSON
    public static void guardarConfiguracion(Configuracion config) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_PATH), config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Carga la configuración desde disco (si existe), o crea una por defecto
    public static Configuracion cargarConfiguracion() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try {
                return mapper.readValue(file, Configuracion.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Configuracion(false); // Si no hay archivo, se devuelve modo claro por defecto
    }
}
