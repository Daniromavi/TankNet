package es.studium.tanknet.core;

import es.studium.tanknet.model.Informe;
import es.studium.tanknet.model.Servicio;
import es.studium.tanknet.model.Vulnerabilidad;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class InformeGenerator {

    // Genera un informe PDF ejecutando pdflatex a partir del contenido LaTeX generado
    public static void generarPDF(Informe informe, File directorioDestino) throws IOException, InterruptedException {
        String contenidoLaTeX = generarContenidoLaTeX(informe);
        File texFile = new File(directorioDestino, "informe.tex");

        Files.writeString(texFile.toPath(), contenidoLaTeX);

        // Ejecutar pdflatex en modo silencioso
        ProcessBuilder pb = new ProcessBuilder("pdflatex", "-interaction=nonstopmode", texFile.getName());
        pb.directory(directorioDestino);
        pb.inheritIO().start().waitFor();

        // Eliminar archivos auxiliares (.aux, .log y .tex)
        Files.deleteIfExists(Path.of(directorioDestino.getAbsolutePath(), "informe.aux"));
        Files.deleteIfExists(Path.of(directorioDestino.getAbsolutePath(), "informe.log"));
        Files.deleteIfExists(texFile.toPath());
    }

    // Genera el contenido LaTeX que se va a compilar a PDF
    private static String generarContenidoLaTeX(Informe informe) {
        StringBuilder contenido = new StringBuilder();

        for (Servicio s : informe.getServicios()) {
            contenido.append("\\textbf{Service:} ").append(s.getNombre()).append(" (")
                    .append(s.getPuerto()).append(" - ").append(s.getVersion()).append(")\\\\\n");

            if (s.getVulnerabilidades() != null && !s.getVulnerabilidades().isEmpty()) {
                for (Vulnerabilidad v : s.getVulnerabilidades()) {
                    // Se escapan símbolos conflictivos para que no reviente el LaTeX
                    contenido.append("\\texttt{")
                            .append(v.getCve()).append("}: ")
                            .append(v.getDescripcion().replaceAll("[%#&_{}]", ""))
                            .append("\\\\[0.1cm]\n"); // Espacio pequeño entre vulnerabilidades
                }
            } else {
                contenido.append("No known vulnerabilities\\\\[0.1cm]\n");
            }

            contenido.append("\\\\[0.6cm]\n"); // Espacio grande entre servicios
        }

        // Formatear fecha para el encabezado
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

    // Plantilla base de LaTeX que se rellena con título, IP, fecha y contenido
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

        \\section*{Services and Vulnerabilities}

        %s

        \\textbf{Nota:} Las descripciones de vulnerabilidades se muestran en el idioma original (inglés).

        \\end{document}
        """;
}
