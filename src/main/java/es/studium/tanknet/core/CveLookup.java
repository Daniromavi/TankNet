package es.studium.tanknet.core;

import es.studium.tanknet.model.Vulnerabilidad;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CveLookup {

    // URL base de la API REST de la NVD (National Vulnerability Database)
    private static final String API_URL = "https://services.nvd.nist.gov/rest/json/cves/2.0?keywordSearch=";
    private static final String USER_AGENT = "TankNet/1.0";

    // Busca vulnerabilidades (CVEs) en la NVD para un servicio y versión determinados
    public static List<Vulnerabilidad> buscarCves(String nombre, String version) {
        List<Vulnerabilidad> resultado = new ArrayList<>();

        try {
            // Normalización básica de nombres comunes para mejorar los resultados de búsqueda
            nombre = nombre.toLowerCase();
            if (nombre.contains("http")) nombre = "apache";
            if (nombre.contains("ssh")) nombre = "openssh";

            // Codificar la query para URL (espacios, caracteres especiales, etc.)
            String query = URLEncoder.encode(nombre + " " + version, StandardCharsets.UTF_8);
            URL url = new URL(API_URL + query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = conn.getResponseCode();
            System.out.println("Buscando CVEs para: \"" + nombre + " " + version + "\"");
            System.out.println("Código HTTP NVD: " + responseCode);

            if (responseCode == 200) {
                // Leer y juntar la respuesta JSON
                String json = new BufferedReader(new InputStreamReader(conn.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));

                JSONObject jsonObject = new JSONObject(json);
                JSONArray cves = jsonObject.optJSONArray("vulnerabilities");

                if (cves != null) {
                    for (int i = 0; i < cves.length(); i++) {
                        JSONObject cveItem = cves.getJSONObject(i).getJSONObject("cve");
                        String id = cveItem.optString("id", "CVE desconocido");

                        // Obtener descripción en inglés
                        JSONArray descs = cveItem.optJSONArray("descriptions");
                        String descripcion = "Sin descripción disponible.";

                        if (descs != null && descs.length() > 0) {
                            for (int j = 0; j < descs.length(); j++) {
                                JSONObject d = descs.getJSONObject(j);
                                if (d.getString("lang").equalsIgnoreCase("en")) {
                                    descripcion = d.getString("value");
                                    break;
                                }
                            }
                        }

                        resultado.add(new Vulnerabilidad(id, descripcion));
                    }
                }
            } else {
                System.err.println("Error al acceder a la API de la NVD. Código: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Resultado CVEs encontrados: " + resultado);
        return resultado;
    }
}
