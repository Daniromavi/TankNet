package es.studium.tanknet.core;

import es.studium.tanknet.model.Dispositivo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class NetworkScanner {

    public static void escanearRed(Consumer<Dispositivo> callback) {
        Set<String> subredes = obtenerSubredesValidas();

        if (subredes.isEmpty()) {
            System.out.println("No se detectaron subredes válidas.");
            return;
        }

        for (String subred : subredes) {
            System.out.println("Escaneando subred: " + subred + "0/24");

            try {
                ProcessBuilder builder = new ProcessBuilder("nmap", "-sn", subred + "0/24");
                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String linea;
                String ip = null;

                while ((linea = reader.readLine()) != null) {
                    if (linea.contains("Nmap scan report for")) {
                        ip = linea.replaceAll(".*\\(([^)]+)\\)", "$1").replaceAll(".*for ", "");
                    } else if (linea.contains("Host is up") && ip != null) {
                        forzarDescubrimientoARP(ip); // socket + ping + espera
                        String mac = obtenerMacDesdeARP(ip);
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

    private static void forzarDescubrimientoARP(String ip) {
        try {
            // Intentar conexión por sockets a puertos comunes
            int[] puertos = {80, 443, 22, 135, 445};
            for (int puerto : puertos) {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(ip, puerto), 200);
                    break; // si conecta, salimos
                } catch (IOException ignored) {}
            }

            // Hacer ping
            Process p = System.getProperty("os.name").toLowerCase().contains("win")
                    ? new ProcessBuilder("ping", "-n", "1", ip).start()
                    : new ProcessBuilder("ping", "-c", "1", ip).start();
            p.waitFor();

            // Esperar para que ARP se actualice
            Thread.sleep(200);
        } catch (Exception ignored) {}
    }

    private static String obtenerMacDesdeARP(String ip) {
        try {
            ProcessBuilder pb = new ProcessBuilder("arp", "-a");
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(ip)) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        return parts[1].replace("-", ":").toLowerCase();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "00:00:00:00:00:00";
    }


    public static Set<String> obtenerSubredesValidas() {
        Set<String> subredes = new HashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress addr = ia.getAddress();
                        if (addr instanceof Inet4Address) {
                            String ip = addr.getHostAddress();

                            if (!ip.startsWith("169.") && !ip.startsWith("192.168.52.") &&
                                    !ip.startsWith("192.168.80.") && !ip.startsWith("192.168.174.")) {

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
}
