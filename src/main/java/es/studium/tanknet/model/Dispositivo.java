package es.studium.tanknet.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class Dispositivo {
    private final StringProperty ip;
    private final StringProperty mac;
    private final StringProperty puertos;

    public Dispositivo(String ip, String mac, String puertos) {
        this.ip = new SimpleStringProperty(ip);
        this.mac = new SimpleStringProperty(mac);
        this.puertos = new SimpleStringProperty(puertos);
    }

    public StringProperty ipProperty() { return ip; }
    public StringProperty macProperty() { return mac; }
    public StringProperty puertosProperty() { return puertos; }

    public String getIp() { return ip.get(); }
    public String getMac() { return mac.get(); }
    public String getPuertos() { return puertos.get(); }
    public List<Servicio> getServicios() {return servicios;  }

    public void setPuertos(String puertos) {
        this.puertos.set(puertos);
    }
    public void setServicios(List<Servicio> servicios) {
        this.servicios = servicios != null ? servicios : new ArrayList<>();
    }

    private List<Servicio> servicios; //Almacena puerto, nombre y version del servicio

}
