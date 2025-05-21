package es.studium.tanknet.core;

import es.studium.tanknet.model.Servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NmapScanner {

    // Ejecuta un escaneo completo de puertos (-p-) y devuelve solo los puertos abiertos como strings
    public static List<String> escanearPuertos(String ip) {
        List<String> puertosAbiertos = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder("nmap", "-p-", ip);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                // Filtra líneas que indiquen puertos TCP abiertos (ej: "80/tcp open http")
                if (linea.matches("\\d+/tcp\\s+open.*")) {
                    String puerto = linea.split("/")[0]; // Extrae solo el número del puerto
                    puertosAbiertos.add(puerto);
                }
            }
            process.waitFor(); // Espera a que el proceso termine
        } catch (Exception e) {
            e.printStackTrace();
        }
        return puertosAbiertos;
    }

    // Escanea servicios y versiones (-sV) y construye objetos Servicio con puerto, nombre y versión
    public static List<Servicio> obtenerServicios(String ip) {
        List<Servicio> servicios = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder("nmap", "-sV", ip);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String linea;
            boolean captura = false; // Solo empezamos a capturar después de la cabecera "PORT"

            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("PORT")) {
                    captura = true; // Se encontró la cabecera de servicios
                    continue;
                }
                if (captura && linea.matches("^\\d+/tcp\\s+open.*")) {
                    // Ejemplo de línea válida: "22/tcp   open  ssh     OpenSSH 7.4"
                    String[] partes = linea.trim().split("\\s+", 4);

                    String puerto = partes[0]; // ej: "22/tcp"
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
