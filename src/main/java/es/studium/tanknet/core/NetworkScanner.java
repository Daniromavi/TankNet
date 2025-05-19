package es.studium.tanknet.core;

import es.studium.tanknet.model.Dispositivo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class NetworkScanner {

    public static void escanearRed(Consumer<Dispositivo> callback) {
        String subred = obtenerSubred();
        if (subred == null) {
            System.out.println("No se pudo obtener la subred.");
            return;
        }

        try {
            ProcessBuilder builder = new ProcessBuilder("nmap", "-sn", subred + "0/24");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String linea;
            String ip = null;
            List<String> ipsDetectadas = new ArrayList<>();

            while ((linea = reader.readLine()) != null) {
                if (linea.contains("Nmap scan report for")) {
                    ip = linea.substring(linea.lastIndexOf(" ") + 1);
                } else if (linea.contains("Host is up") && ip != null) {
                    // Hacemos ping para forzar entrada en tabla ARP
                    try {
                        InetAddress inet = InetAddress.getByName(ip);
                        inet.isReachable(100);
                    } catch (Exception ignored) {}

                    ipsDetectadas.add(ip);
                    ip = null;
                }
            }

            process.waitFor();

            // Espera breve para asegurar que la tabla ARP se actualice
            Thread.sleep(300);
            Map<String, String> arpTable = cacheARP();

            for (String ipActiva : ipsDetectadas) {
                String mac = arpTable.getOrDefault(ipActiva, "00:00:00:00:00:00");
                System.out.println("IP detectada: " + ipActiva + " - MAC: " + mac);

                Dispositivo d = new Dispositivo(ipActiva, mac, "Pendiente de escaneo...");
                callback.accept(d);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String obtenerSubred() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress addr = ia.getAddress();
                        if (addr instanceof Inet4Address) {
                            String ip = addr.getHostAddress();
                            if (!ip.startsWith("192.168.52.") && !ip.startsWith("192.168.56.")) {
                                System.out.println("Usando interfaz: " + ni.getDisplayName() + " - IP: " + ip);
                                return ip.substring(0, ip.lastIndexOf('.') + 1);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

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
