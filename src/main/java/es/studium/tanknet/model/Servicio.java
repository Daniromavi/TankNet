package es.studium.tanknet.model;

public class Servicio {
    private final String puerto;
    private final String nombre;
    private final String version;

    public Servicio(String puerto, String nombre, String version) {
        this.puerto = puerto;
        this.nombre = nombre;
        this.version = version;
    }

    public String getPuerto() { return puerto; }
    public String getNombre() { return nombre; }
    public String getVersion() { return version; }
}
