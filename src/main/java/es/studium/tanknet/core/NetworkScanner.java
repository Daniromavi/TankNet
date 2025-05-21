package es.studium.tanknet.core;

import es.studium.tanknet.model.Dispositivo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class NetworkScanner {

    // Escanea todas las subredes válidas detectadas en las interfaces activas
    public static void escanearRed(Consumer<Dispositivo> callback) {
        Set<String> subredes = obtenerSubredesValidas(); // Detecta las subredes IPv4 válidas

        if (subredes.isEmpty()) {
            System.out.println("No se detectaron subredes válidas.");
            return;
        }

        Map<String, String> tablaArp = cacheARP(); // Cacheamos la tabla ARP del sistema para resolver MACs

        for (String subred : subredes) {
            System.out.println("Escaneando subred: " + subred + "0/24");

            try {
                // Escaneo nmap tipo ping-scan para descubrir hosts activos
                ProcessBuilder builder = new ProcessBuilder("nmap", "-sn", subred + "0/24");
                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String linea;
                String ip = null;

                while ((linea = reader.readLine()) != null) {
                    if (linea.contains("Nmap scan report for")) {
                        // Extraemos la IP correctamente tanto si viene entre paréntesis como sin ellos
                        ip = linea.replaceAll(".*\\(([^)]+)\\)", "$1") // si viene como (192.168.x.x)
                                .replaceAll(".*for ", "");            // si viene como "for 192.168.x.x"
                    }
                    else if (linea.contains("Host is up") && ip != null) {
                        String mac = tablaArp.getOrDefault(ip, "00:00:00:00:00:00");
                        callback.accept(new Dispositivo(ip, mac, "Pendiente de escaneo..."));
                        ip = null;
                    }
                }

                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Devuelve un set de subredes locales válidas, filtrando interfaces virtuales y reservadas
    public static Set<String> obtenerSubredesValidas() {
        Set<String> subredes = new HashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                // Solo interfaces activas, no loopback ni virtuales
                if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress addr = ia.getAddress();
                        if (addr instanceof Inet4Address) {
                            String ip = addr.getHostAddress();

                            // Filtrado de subredes irrelevantes (Docker, VirtualBox, localhost, etc.)
                            if (!ip.startsWith("192.168.56.") && !ip.startsWith("192.168.52.") &&
                                    !ip.startsWith("169.254") && !ip.startsWith("127.")) {

                                String subred = ip.substring(0, ip.lastIndexOf('.') + 1);

                                if (subredes.add(subred)) {
                                    System.out.println("Detectada subred válida: " + subred + " (Interfaz: " + ni.getDisplayName() + ")");
                                }
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return subredes;
    }

    // Devuelve un mapa IP -> MAC consultando la tabla ARP del sistema
    private static Map<String, String> cacheARP() {
        Map<String, String> tabla = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("arp", "-a");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
                    String[] partes = linea.trim().split("\\s+");
                    if (partes.length >= 2) {
                        String ip = partes[0];
                        String mac = partes[1].replace("-", ":").toLowerCase();
                        tabla.put(ip, mac);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tabla;
    }
}
