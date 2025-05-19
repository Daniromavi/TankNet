package es.studium.tanknet.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public class Servicio {
    private final StringProperty puerto = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty version = new SimpleStringProperty();
    private List<Vulnerabilidad> vulnerabilidades;

    public void setVulnerabilidades(List<Vulnerabilidad> vulnerabilidades) {
        this.vulnerabilidades = vulnerabilidades;
    }

    public List<Vulnerabilidad> getVulnerabilidades() {
        return vulnerabilidades;
    }


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
