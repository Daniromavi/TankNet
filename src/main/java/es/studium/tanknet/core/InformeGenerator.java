package es.studium.tanknet.core;

import es.studium.tanknet.model.Informe;
import es.studium.tanknet.model.Servicio;
import es.studium.tanknet.model.Vulnerabilidad;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class InformeGenerator {

    public static void generarPDF(Informe informe, File directorioDestino) throws IOException, InterruptedException {
        String contenidoLaTeX = generarContenidoLaTeX(informe);
        File texFile = new File(directorioDestino, "informe.tex");

        Files.writeString(texFile.toPath(), contenidoLaTeX);

        ProcessBuilder pb = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", texFile.getName());
        pb.directory(directorioDestino);
        pb.inheritIO().start().waitFor();

        // Opcional: limpiar archivos auxiliares
        Files.deleteIfExists(Path.of(directorioDestino.getAbsolutePath(), "informe.aux"));
        Files.deleteIfExists(Path.of(directorioDestino.getAbsolutePath(), "informe.log"));
        Files.deleteIfExists(texFile.toPath());
    }

    private static String generarContenidoLaTeX(Informe informe) {
        StringBuilder contenido = new StringBuilder();

        for (Servicio s : informe.getServicios()) {
            contenido.append("\\textbf{Service:} ").append(s.getNombre()).append(" (")
                    .append(s.getPuerto()).append(" - ").append(s.getVersion()).append(")\\\\\n");

            if (s.getVulnerabilidades() != null && !s.getVulnerabilidades().isEmpty()) {
                for (Vulnerabilidad v : s.getVulnerabilidades()) {
                    contenido.append("\\texttt{")
                            .append(v.getCve()).append("}: ")
                            .append(v.getDescripcion().replaceAll("[%#&_{}]", ""))
                            .append("\\\\[0.1cm]\n"); // Espacio pequeño entre CVEs
                }
            } else {
                contenido.append("No known vulnerabilities\\\\[0.1cm]\n");
            }

            // 👇 Aquí el espacio grande entre servicios (¡con doble barra!)
            contenido.append("\\\\[0.6cm]\n");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String fechaFormateada = informe.getFecha().format(formatter);

        return String.format(PLANTILLA_LATEX,
                informe.getTitulo(),
                informe.getIp(),
                informe.getMac(),
                fechaFormateada,
                contenido.toString()
        );
    }

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
        \\normalsize IP: %s \\\\
        MAC: %s \\\\
        Fecha: %s
        \\end{center}

        \\vspace{1cm}
       \s
        \\section*{Services and Vulnerabilities}

        %s

        \\textbf{Nota:} Las descripciones de vulnerabilidades se muestran en el idioma original (inglés).
           \s
        \\end{document}
       \s""";

}
