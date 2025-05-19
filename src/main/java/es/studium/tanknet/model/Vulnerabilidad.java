package es.studium.tanknet.model;

public class Vulnerabilidad {
    private final String cve;
    private final String descripcion;

    public Vulnerabilidad(String cve, String descripcion) {
        this.cve = cve;
        this.descripcion = descripcion;
    }

    public String getCve() {
        return cve;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
