package es.studium.tanknet.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Servicio {
    private final StringProperty puerto = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty version = new SimpleStringProperty();

    public Servicio(String puerto, String servicio, String version) {
        this.puerto.set(puerto);
        this.nombre.set(servicio);
        this.version.set(version);
    }

    public String getPuerto() {
        return puerto.get();
    }

    public String getNombre() {
        return nombre.get();
    }

    public String getVersion() {
        return version.get();
    }

    public StringProperty puertoProperty() {
        return puerto;
    }

    public StringProperty nombreProperty() {
        return nombre;
    }

    public StringProperty versionProperty() {
        return version;
    }

}
