package es.studium.tanknet.core;

import es.studium.tanknet.model.Dispositivo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NetworkScanner {

    public static void escanearRed(Consumer<Dispositivo> callback) {
        String subred = obtenerSubred();

        if (subred == null) {
            System.out.println("No se pudo obtener la subred.");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(100);

        for (int i = 1; i <= 254; i++) {
            String ip = subred + i;
            executor.submit(() -> {
                if (pingHost(ip)) {
                    try {
                        // Forzar que la MAC aparezca en la tabla ARP
                        try (Socket socket = new Socket()) {
                            socket.connect(new InetSocketAddress(ip, 80), 100);
                        } catch (IOException ignored) {}

                        Thread.sleep(100); // Esperar un poco para que ARP se actualice
                        String nombre = "Pendiente de escaneo...";
                        String mac = obtenerMacDesdeARP(ip);

                        Dispositivo d = new Dispositivo(ip, mac, nombre);
                        System.out.println("Detectado: " + ip + " | MAC: " + mac);
                        callback.accept(d);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executor.shutdown();
    }

    private static boolean pingHost(String ip) {
        try {
            String cmd = System.getProperty("os.name").toLowerCase().contains("win")
                    ? "ping -n 1 -w 100 " + ip
                    : "ping -c 1 -W 1 " + ip;

            Process p = Runtime.getRuntime().exec(cmd);
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
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
