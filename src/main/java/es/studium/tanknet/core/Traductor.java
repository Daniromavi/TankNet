package es.studium.tanknet.core;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Traductor {

    public static String traducir(String texto, String from, String to) {
        try {
            if (texto == null || texto.trim().isEmpty()) return texto;

            String apiKey = "";
            URL url = new URL("https://api-free.deepl.com/v2/translate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "auth_key=" + apiKey +
                    "&text=" + URLEncoder.encode(texto, "UTF-8") +
                    "&target_lang=" + to;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json = reader.lines().collect(Collectors.joining());

            JSONObject res = new JSONObject(json);
            return res.getJSONArray("translations").getJSONObject(0).getString("text");

        } catch (Exception e) {
            System.err.println("Error al traducir con DeepL: " + e.getMessage());
            return texto;
        }
    }


}
