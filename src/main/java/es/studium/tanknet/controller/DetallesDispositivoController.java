package es.studium.tanknet.controller;

import es.studium.tanknet.core.CveLookup;
import es.studium.tanknet.core.InformeGenerator;
import es.studium.tanknet.core.NmapScanner;
import es.studium.tanknet.model.Informe;
import es.studium.tanknet.model.Vulnerabilidad;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import es.studium.tanknet.model.Servicio;
import es.studium.tanknet.model.Dispositivo;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class DetallesDispositivoController {

    @FXML private Label ipLabel;
    @FXML private Label macLabel;
    @FXML private Button btnEscanear;
    @FXML private Button btnInforme;

    @FXML private TableView<Servicio> tablaServicios;
    @FXML private TableColumn<Servicio, String> colPuerto;
    @FXML private TableColumn<Servicio, String> colServicio;
    @FXML private TableColumn<Servicio, String> colVersion;

    private Dispositivo dispositivoActual;
    private Thread animacionThread;
    private boolean animacionActiva = false;

    public void setDispositivo(Dispositivo dispositivo) {
        this.dispositivoActual = dispositivo;

        ipLabel.setText("IP: " + dispositivo.getIp());
        macLabel.setText("MAC: " + dispositivo.getMac());

        tablaServicios.refresh();
    }

    public void initialize() {
        // Asociar columnas de la tabla a propiedades del modelo Servicio
        colPuerto.setCellValueFactory(new PropertyValueFactory<>("puerto"));
        colServicio.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colVersion.setCellValueFactory(new PropertyValueFactory<>("version"));

        btnEscanear.setOnAction(event -> escanearServicios());
        btnInforme.setOnAction(event -> generarInforme());
    }

    @FXML
    private void escanearServicios() {
        if (dispositivoActual == null) return;

        btnEscanear.setDisable(true);
        startAnimacionBoton();

        Task<List<Servicio>> escaneoTask = new Task<>() {
            @Override
            protected List<Servicio> call() {
                return NmapScanner.obtenerServicios(dispositivoActual.getIp());
            }
        };

        escaneoTask.setOnSucceeded(e -> {
            stopAnimacionBoton();
            List<Servicio> servicios = escaneoTask.getValue();

            for (Servicio servicio : servicios) {
                // Limpiar la versión para que la búsqueda de CVEs sea más precisa
                String versionLimpia = servicio.getVersion().replaceAll("[^0-9\\.]", "").trim();

                // Buscar vulnerabilidades en la NVD para ese servicio y versión
                List<Vulnerabilidad> vulns = CveLookup.buscarCves(servicio.getNombre(), versionLimpia);
                servicio.setVulnerabilidades(vulns);
            }

            dispositivoActual.setServicios(servicios);
            tablaServicios.getItems().setAll(servicios);
            btnEscanear.setDisable(false);
            btnEscanear.setText("Escanear Servicios");
        });

        escaneoTask.setOnFailed(e -> {
            stopAnimacionBoton();
            btnEscanear.setDisable(false);
            btnEscanear.setText("Escanear Servicios");
        });

        new Thread(escaneoTask).start();
    }

    // Muestra animación en el botón mientras se escanean servicios
    private void startAnimacionBoton() {
        animacionActiva = true;
        animacionThread = new Thread(() -> {
            String[] estados = {"Escaneando", "Escaneando.", "Escaneando..", "Escaneando..."};
            int i = 0;
            while (animacionActiva) {
                final String texto = estados[i % estados.length];
                Platform.runLater(() -> btnEscanear.setText(texto));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                i++;
            }
        });
        animacionThread.setDaemon(true);
        animacionThread.start();
    }

    private void stopAnimacionBoton() {
        animacionActiva = false;
        if (animacionThread != null && animacionThread.isAlive()) {
            animacionThread.interrupt();
        }
    }

    @FXML
    private void generarInforme() {

        Dispositivo dispositivo = dispositivoActual;

        if (dispositivoActual == null || dispositivoActual.getServicios() == null || dispositivoActual.getServicios().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No hay servicios escaneados para generar un informe.").showAndWait();
            return;
        }

        // Solicitar título del informe al usuario
        TextInputDialog dialogo = new TextInputDialog("Informe de " + dispositivo.getIp());
        dialogo.setTitle("Título del informe");
        dialogo.setHeaderText("Introduce un título para el informe:");
        dialogo.setContentText("Título:");

        // Preguntar por el idioma del informe
        List<String> idiomas = List.of("Español", "Inglés");
        ChoiceDialog<String> idiomaDialog = new ChoiceDialog<>("Español", idiomas);
        idiomaDialog.setTitle("Idioma del informe");
        idiomaDialog.setHeaderText("Selecciona el idioma del informe:");
        idiomaDialog.setContentText("Idioma:");
        Optional<String> idiomaSeleccionado = idiomaDialog.showAndWait();

        if (idiomaSeleccionado.isEmpty()) {
            return; // Cancelado por el usuario
        }

        String idioma = idiomaSeleccionado.get().equals("Español") ? "es" : "en";


        String titulo = dialogo.showAndWait().orElse("Informe de " + dispositivo.getIp());

        Informe informe = new Informe(titulo, dispositivo.getIp(), dispositivo.getMac(), dispositivo.getServicios());

        // Selección de archivo destino
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar informe PDF");
        fileChooser.setInitialFileName(titulo + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File archivoDestino = fileChooser.showSaveDialog(tablaServicios.getScene().getWindow());

        if (archivoDestino != null) {
            try {
                // Crear carpeta temporal para generar el PDF
                File carpetaTemporal = new File("informes_temp");
                carpetaTemporal.mkdirs();

                // Generar el informe en LaTeX
                InformeGenerator.generarPDF(informe, carpetaTemporal, idioma);

                // Mover el informe final al destino elegido por el usuario
                File generado = new File(carpetaTemporal, "informe.pdf");
                if (generado.exists()) {
                    Files.move(generado.toPath(), archivoDestino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                // Limpiar archivos temporales
                for (File f : carpetaTemporal.listFiles()) f.delete();
                carpetaTemporal.delete();

                new Alert(Alert.AlertType.INFORMATION, "Informe generado correctamente.").showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error al generar el informe.").showAndWait();
            }
        }
    }

}
