package es.studium.tanknet.core;

import java.io.BufferedReader;
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
}
