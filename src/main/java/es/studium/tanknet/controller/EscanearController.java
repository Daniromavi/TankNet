package es.studium.tanknet.controller;

import es.studium.tanknet.core.CveLookup;
import es.studium.tanknet.core.NavigationManager;
import es.studium.tanknet.core.NmapScanner;
import es.studium.tanknet.model.Dispositivo;
import es.studium.tanknet.core.NetworkScanner;
import es.studium.tanknet.model.Servicio;
import es.studium.tanknet.model.Vulnerabilidad;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class EscanearController {

    @FXML private Label errWifiLabel;
    @FXML private Button btnEscanear;
    @FXML private TableView<Dispositivo> tablaDispositivos;
    @FXML private TableColumn<Dispositivo, String> colIP;
    @FXML private TableColumn<Dispositivo, String> colMac;
    @FXML private TableColumn<Dispositivo, String> colPuertos;
    @FXML private TableColumn<Dispositivo, Void> colAcciones;

    @FXML
    public void initialize() {
        errWifiLabel.setVisible(false);

        tablaDispositivos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDispositivos.setPlaceholder(new Label("No se han escaneado dispositivos."));

        // Asignar proporciones a las columnas
        colIP.setMaxWidth(1f * Integer.MAX_VALUE * 11);
        colMac.setMaxWidth(1f * Integer.MAX_VALUE * 14);
        colPuertos.setMaxWidth(1f * Integer.MAX_VALUE * 48);
        colAcciones.setMaxWidth(1f * Integer.MAX_VALUE * 27);

        colIP.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colMac.setCellValueFactory(new PropertyValueFactory<>("mac"));
        colPuertos.setCellValueFactory(new PropertyValueFactory<>("puertos"));

        btnEscanear.setOnAction(event -> escanearRed());

        // Columna con botones "Escanear puertos" y "Más info"
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnPuertos = new Button("Escanear puertos");
            private final Button btnInfo = new Button("Más info");
            private final HBox hbox = new HBox(5, btnPuertos, btnInfo);

            {
                hbox.setAlignment(Pos.CENTER);

                // Estilo compacto para los botones
                btnPuertos.setPrefHeight(16);
                btnPuertos.setPrefWidth(110);
                btnPuertos.setStyle("-fx-font-size: 12px; -fx-padding: 2 5 2 5;");

                btnInfo.setPrefHeight(16);
                btnInfo.setPrefWidth(70);
                btnInfo.setStyle("-fx-font-size: 12px; -fx-padding: 2 5 2 5;");

                // Acción al pulsar "Escanear puertos"
                btnPuertos.setOnAction(event -> {
                    Dispositivo dispositivo = getTableView().getItems().get(getIndex());
                    System.out.println("Escaneando puertos de: " + dispositivo.getIp());

                    // Tarea completa: animación + escaneo + vulnerabilidades
                    Task<Void> escaneoCompleto = new Task<>() {
                        @Override
                        protected Void call() {
                            String[] estados = {"Cargando", "Cargando.", "Cargando..", "Cargando..."};
                            int[] i = {0};

                            // Hilo animación de estado
                            Thread animacion = new Thread(() -> {
                                try {
                                    while (!isCancelled()) {
                                        final String estado = estados[i[0] % estados.length];
                                        Platform.runLater(() -> {
                                            dispositivo.setPuertos(estado);
                                            tablaDispositivos.refresh();
                                        });
                                        Thread.sleep(500);
                                        i[0]++;
                                    }
                                } catch (InterruptedException ignored) {}
                            });
                            animacion.setDaemon(true);
                            animacion.start();

                            // Escaneo real
                            List<String> puertos = NmapScanner.escanearPuertos(dispositivo.getIp());
                            List<Servicio> servicios = NmapScanner.obtenerServicios(dispositivo.getIp());

                            for (Servicio servicio : servicios) {
                                // Limpiar la versión para evitar texto innecesario en la query de CVEs
                                String versionLimpia = servicio.getVersion().replaceAll("[^0-9\\.]", "").trim();

                                List<Vulnerabilidad> vulns = CveLookup.buscarCves(servicio.getNombre(), versionLimpia);
                                servicio.setVulnerabilidades(vulns);
                            }

                            // Actualizar resultados en la interfaz
                            Platform.runLater(() -> {
                                String textoPuertos = String.join(", ", puertos);
                                dispositivo.setPuertos(textoPuertos.isEmpty() ? "Sin puertos abiertos" : textoPuertos);
                                dispositivo.setServicios(servicios);
                                tablaDispositivos.refresh();
                            });

                            this.cancel(); // termina animación
                            return null;
                        }
                    };

                    new Thread(escaneoCompleto).start();
                });

                // Botón "Más info": abre vista de detalle
                btnInfo.setOnAction(event -> {
                    Dispositivo dispositivo = getTableView().getItems().get(getIndex());
                    NavigationManager.setViewWithData("/es/studium/tanknet/view/DetallesDispositivo.fxml", dispositivo);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        // Necesario para que la tabla renderice la columna sin datos
        colAcciones.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
    }

    @FXML
    public void escanearRed() {
        tablaDispositivos.getItems().clear();
        tablaDispositivos.getItems().add(new Dispositivo("Cargando...", "Cargando...", "Cargando...")); // fila temporal
        btnEscanear.setDisable(true);

        Task<Void> tareaEscaneo = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    errWifiLabel.setVisible(false);
                    startPuntosAnimacion();
                    tablaDispositivos.getItems().clear();
                });

                List<Dispositivo> detectados = new ArrayList<>();

                // Escanea todas las subredes activas válidas
                NetworkScanner.escanearRed(dispositivo -> {
                    detectados.add(dispositivo);
                    Platform.runLater(() -> tablaDispositivos.getItems().add(dispositivo));
                });

                // Simulación de espera visual (se puede quitar)
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}

                Platform.runLater(() -> {
                    stopPuntosAnimacion();
                    btnEscanear.setText("Escanear");
                    btnEscanear.setDisable(false);

                    if (detectados.isEmpty()) {
                        errWifiLabel.setText("No se encontraron dispositivos.");
                        errWifiLabel.setVisible(true);
                    }
                });

                return null;
            }
        };

        new Thread(tareaEscaneo).start();
    }

    private Thread puntosThread;
    private boolean animacionActiva = false;

    // Animación de "Escaneando..." en el botón
    private void startPuntosAnimacion() {
        animacionActiva = true;
        puntosThread = new Thread(() -> {
            String[] estados = {"Escaneando.", "Escaneando..", "Escaneando..."};
            int i = 0;
            while (animacionActiva) {
                final String texto = estados[i % estados.length];
                Platform.runLater(() -> btnEscanear.setText(texto));
                i++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        puntosThread.setDaemon(true);
        puntosThread.start();
    }

    private void stopPuntosAnimacion() {
        animacionActiva = false;
        if (puntosThread != null && puntosThread.isAlive()) {
            puntosThread.interrupt();
        }
    }
}

