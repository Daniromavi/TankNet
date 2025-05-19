package es.studium.tanknet.core;

import es.studium.tanknet.model.Servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NmapScanner {

    public static List<String> escanearPuertos(String ip) {
        List<String> puertosAbiertos = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder("nmap", "-p-", ip);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.matches("\\d+/tcp\\s+open.*")) { // Solo líneas tipo "80/tcp open ..."
                    String puerto = linea.split("/")[0]; // Parte antes de /tcp
                    puertosAbiertos.add(puerto);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return puertosAbiertos;
    }

    public static List<Servicio> obtenerServicios(String ip) {
        List<Servicio> servicios = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder("nmap", "-sV", ip);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String linea;
            boolean captura = false;

            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("PORT")) {
                    captura = true;
                    continue;
                }
                if (captura && linea.matches("^\\d+/tcp\\s+open.*")) {
                    String[] partes = linea.trim().split("\\s+", 4);
                    String puerto = partes[0];
                    String servicio = partes.length > 2 ? partes[2] : "Desconocido";
                    String version = partes.length > 3 ? partes[3] : "N/A";
                    servicios.add(new Servicio(puerto, servicio, version));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return servicios;
    }

}
