package es.studium.tanknet.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Dispositivo {
    private final StringProperty ip;
    private final StringProperty mac;
    private final StringProperty nombre;

    public Dispositivo(String ip, String mac, String nombre) {
        this.ip = new SimpleStringProperty(ip);
        this.mac = new SimpleStringProperty(mac);
        this.nombre = new SimpleStringProperty(nombre);
    }

    public StringProperty ipProperty() { return ip; }
    public StringProperty macProperty() { return mac; }
    public StringProperty nombreProperty() { return nombre; }

    public String getIp() { return ip.get(); }
    public String getMac() { return mac.get(); }
    public String getNombre() { return nombre.get(); }
}
