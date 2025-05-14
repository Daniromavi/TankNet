package es.studium.tanknet.core;

import es.studium.tanknet.model.Dispositivo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
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
        String mac = "Desconocida";

        while ((linea = reader.readLine()) != null) {
            if (linea.contains("Nmap scan report for")) {
                ip = linea.substring(linea.lastIndexOf(" ") + 1);
            }
            else if (linea.contains("Host is up") && ip != null) {
                String macDetectada = obtenerMacDesdeARP(ip); // Usas tu método confiable
                Dispositivo d = new Dispositivo(ip, macDetectada, "Pendiente de escaneo...");
                callback.accept(d);
                ip = null;
            }
        }

        process.waitFor();
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
}

    public static String obtenerSubred() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // Solo interfaces activas, no loopback ni virtuales
                if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
                    for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                        InetAddress addr = ia.getAddress();
                        if (addr instanceof Inet4Address) {
                            // Filtramos subredes "extrañas", por ejemplo VirtualBox o similares
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

    private static String obtenerMacDesdeARP(String ip) {
        try {
            ProcessBuilder pb = System.getProperty("os.name").toLowerCase().contains("win")
                    ? new ProcessBuilder("arp", "-a", ip)
                    : new ProcessBuilder("arp", ip);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.contains(ip)) {
                    String[] partes = linea.trim().split("\\s+");
                    for (String parte : partes) {
                        if (parte.matches("..:..:..:..:..:..") || parte.matches("..-..-..-..-..-..")) {
                            return parte.replace("-", ":");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "00:00:00:00:00:00";
    }
}
