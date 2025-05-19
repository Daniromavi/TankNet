package es.studium.tanknet.model;

import java.time.LocalDateTime;
import java.util.List;

public class Informe {
    private String titulo;
    private String ip;
    private String mac;
    private LocalDateTime fecha;
    private List<Servicio> servicios;

    public Informe(String titulo, String ip, String mac, List<Servicio> servicios) {
        this.titulo = titulo;
        this.ip = ip;
        this.mac = mac;
        this.fecha = LocalDateTime.now();
        this.servicios = servicios;
    }

    public String getTitulo() { return titulo; }
    public String getIp() { return ip; }
    public String getMac() { return mac; }
    public LocalDateTime getFecha() { return fecha; }
    public List<Servicio> getServicios() { return servicios; }
}
