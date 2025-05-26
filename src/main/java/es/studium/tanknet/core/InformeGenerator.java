package es.studium.tanknet.core;

import es.studium.tanknet.model.Informe;
import es.studium.tanknet.model.Servicio;
import es.studium.tanknet.model.Vulnerabilidad;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class InformeGenerator {

    public static void generarPDF(Informe informe, File directorioDestino, String idiomaDestino) throws IOException, InterruptedException {
        String contenidoLaTeX = generarContenidoLaTeX(informe, idiomaDestino);
        File texFile = new File(directorioDestino, "informe.tex");

        Files.writeString(texFile.toPath(), contenidoLaTeX);

        ProcessBuilder pb = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", texFile.getName());
        pb.directory(directorioDestino);
        pb.inheritIO().start().waitFor();

        // Eliminar archivos temporales
        Files.deleteIfExists(Path.of(directorioDestino.getAbsolutePath(), "informe.aux"));
        Files.deleteIfExists(Path.of(directorioDestino.getAbsolutePath(), "informe.log"));
        Files.deleteIfExists(texFile.toPath());
    }

    private static String generarContenidoLaTeX(Informe informe, String idioma) {
        StringBuilder contenido = new StringBuilder();

        // Traducción de campos estáticos
        String titulo = traducir(informe.getTitulo(), idioma);
        String ipLabel = traducir("IP", idioma);
        String macLabel = traducir("MAC", idioma);
        String fechaLabel = traducir("Date", idioma);
        String seccionTitulo = traducir("Services and Vulnerabilities", idioma);
        String sinVulnerabilidades = traducir("No known vulnerabilities", idioma);
        String nota = traducir("Note: The vulnerability descriptions are shown in the selected language.", idioma);
        String servicioTexto = traducir("Service", idioma);

        // Formatear fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String fechaFormateada = informe.getFecha().format(formatter);

        // Traducir cuerpo del informe
        for (Servicio s : informe.getServicios()) {
            String nombre = traducir(s.getNombre(), idioma);
            String version = s.getVersion();
            String puerto = s.getPuerto();

            contenido.append(String.format("\\textbf{%s:} %s (%s - %s)\\\\\n", servicioTexto, nombre, puerto, version));

            if (s.getVulnerabilidades() != null && !s.getVulnerabilidades().isEmpty()) {
                for (Vulnerabilidad v : s.getVulnerabilidades()) {
                    String descripcion = v.getDescripcion().replaceAll("[%#&_{}]", "");
                    if (!idioma.equals("en")) {
                        descripcion = traducir(descripcion, idioma);
                    }
                    contenido.append("\\texttt{").append(v.getCve()).append("}: ")
                            .append(descripcion).append("\\\\[0.1cm]\n");
                }
            } else {
                contenido.append(sinVulnerabilidades).append("\\\\[0.1cm]\n");
            }

            contenido.append("\\\\[0.6cm]\n");
        }

        // Ensamblar plantilla LaTeX
        return String.format(PLANTILLA_LATEX,
                titulo, ipLabel, informe.getIp(), macLabel, informe.getMac(), fechaLabel, fechaFormateada,
                seccionTitulo, contenido.toString(), nota
        );
    }

    // Traduce con fallback
    private static String traducir(String texto, String idiomaDestino) {
        if (idiomaDestino.equals("en")) return texto;
        return Traductor.traducir(texto, "en", idiomaDestino);
    }

    // Plantilla LaTeX con variables insertadas
    private static final String PLANTILLA_LATEX = """
        \\documentclass{article}
        \\usepackage[utf8]{inputenc}
        \\usepackage[margin=1in]{geometry}
        \\usepackage{titlesec}
        \\usepackage{hyperref}
        \\titleformat{\\section}{\\normalfont\\Large\\bfseries}{}{0em}{}
        \\begin{document}

        \\begin{center}
        \\Huge\\textbf{%s} \\\\[1em]
        \\normalsize %s: %s \\\\
        %s: %s \\\\
        %s: %s
        \\end{center}

        \\vspace{1cm}

        \\section*{%s}

        %s

        \\textbf{%s}

        \\end{document}
        """;
}
