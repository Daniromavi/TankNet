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

        public class DetallesDispositivoController {

            @FXML
            private Label tituloLabel;

            @FXML
            private Label ipLabel;

            @FXML
            private Label macLabel;

            @FXML
            private Button btnEscanear;

            @FXML
            private Button btnInforme;

            @FXML
            private TableView<Servicio> tablaServicios;

            @FXML
            private TableColumn<Servicio, String> colPuerto;

            @FXML
            private TableColumn<Servicio, String> colServicio;

            @FXML
            private TableColumn<Servicio, String> colVersion;

            private Dispositivo dispositivoActual;

            private Thread animacionThread;
            private boolean animacionActiva = false;

            public void initialize() {
                colPuerto.setCellValueFactory(new PropertyValueFactory<>("puerto"));
                colServicio.setCellValueFactory(new PropertyValueFactory<>("nombre"));
                colVersion.setCellValueFactory(new PropertyValueFactory<>("version"));

                btnEscanear.setOnAction(event -> escanearServicios());
                btnInforme.setOnAction(event -> generarInforme());
            }


            public void setDispositivo(Dispositivo dispositivo) {
                this.dispositivoActual = dispositivo; //soy autista, retrasado y maricon
                ipLabel.setText("IP: " + dispositivo.getIp());
                macLabel.setText("MAC: " + dispositivo.getMac());

                if (dispositivo.getServicios() != null && !dispositivo.getServicios().isEmpty()) {
                    tablaServicios.getItems().setAll(dispositivo.getServicios());
                } else {
                    tablaServicios.getItems().clear();
                    tablaServicios.setPlaceholder(new Label("No se han escaneado servicios."));
                }

                tablaServicios.refresh();
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
                        // Limpiar versión para evitar cosas como ((Debian)), etc.
                        String versionLimpia = servicio.getVersion().replaceAll("[^0-9\\.]", "").trim();

                        System.out.println("Buscando CVEs para: " + servicio.getNombre() + " " + versionLimpia);

                        List<Vulnerabilidad> vulns = CveLookup.buscarCves(servicio.getNombre(), versionLimpia);
                        System.out.println(vulns);
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
                Dispositivo dispositivo = dispositivoActual; // El dispositivo seleccionado
                TextInputDialog dialogo = new TextInputDialog("Informe de " + dispositivo.getIp());
                dialogo.setTitle("Título del informe");
                dialogo.setHeaderText("Introduce un título para el informe:");
                dialogo.setContentText("Título:");

                String titulo = dialogo.showAndWait().orElse("Informe de " + dispositivo.getIp());
                Informe informe = new Informe(titulo, dispositivo.getIp(), dispositivo.getMac(), dispositivo.getServicios());

                FileChooser fileChooser = new FileChooser();

                fileChooser.setTitle("Guardar informe PDF");
                fileChooser.setInitialFileName(titulo + ".pdf");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

                File archivoDestino = fileChooser.showSaveDialog(tablaServicios.getScene().getWindow());

                if (archivoDestino != null) {
                    try {
                        File carpetaTemporal = new File("informes_temp");
                        carpetaTemporal.mkdirs();

                        InformeGenerator.generarPDF(informe, carpetaTemporal);

                        // Mover el PDF generado a la ruta elegida por el usuario
                        File generado = new File(carpetaTemporal, "informe.pdf");
                        if (generado.exists()) {
                            Files.move(generado.toPath(), archivoDestino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }

                        // Eliminar carpeta temporal
                        for (File f : carpetaTemporal.listFiles()) f.delete();
                        carpetaTemporal.delete();

                        Alert alerta = new Alert(Alert.AlertType.INFORMATION, "Informe generado correctamente.");
                        alerta.showAndWait();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Alert alerta = new Alert(Alert.AlertType.ERROR, "Error al generar el informe.");
                        alerta.showAndWait();
                    }
                }
            }

        }
