package es.studium.tanknet.controller;

import es.studium.tanknet.core.NavigationManager;
import es.studium.tanknet.core.NmapScanner;
import es.studium.tanknet.model.Dispositivo;
import es.studium.tanknet.core.NetworkScanner;
import es.studium.tanknet.model.Servicio;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class EscanearController {

    @FXML
    private Label errWifiLabel;

    @FXML
    private Button btnEscanear;

    @FXML
    private TableView<Dispositivo> tablaDispositivos;

    @FXML
    private TableColumn<Dispositivo, String> colIP;

    @FXML
    private TableColumn<Dispositivo, String> colMac;

    @FXML
    private TableColumn<Dispositivo, String> colPuertos;

    @FXML
    private TableColumn<Dispositivo, Void> colAcciones;

    @FXML
    public void initialize() {
        errWifiLabel.setVisible(false);

        tablaDispositivos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colIP.setMaxWidth(1f * Integer.MAX_VALUE * 11); // 15% aprox
        colMac.setMaxWidth(1f * Integer.MAX_VALUE * 14);
        colPuertos.setMaxWidth(1f * Integer.MAX_VALUE * 48);
        colAcciones.setMaxWidth(1f * Integer.MAX_VALUE * 27); // 55% (el resto)

        colIP.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colMac.setCellValueFactory(new PropertyValueFactory<>("mac"));
        colPuertos.setCellValueFactory(new PropertyValueFactory<>("puertos"));

        btnEscanear.setOnAction(event -> { escanearRed();});


        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnPuertos = new Button("Escanear puertos");
            private final Button btnInfo = new Button("Más info");
            private final HBox hbox = new HBox(5, btnPuertos, btnInfo);

            {
                hbox.setAlignment(Pos.CENTER);
                hbox.setPrefHeight(20); // altura del HBox más baja

                btnPuertos.setPrefHeight(16); // altura de botón más pequeña
                btnPuertos.setPrefWidth(110);
                btnPuertos.setStyle("-fx-font-size: 12px; -fx-padding: 2 5 2 5;"); // padding más pequeño

                btnInfo.setPrefHeight(16);
                btnInfo.setPrefWidth(70);
                btnInfo.setStyle("-fx-font-size: 12px; -fx-padding: 2 5 2 5;");

                btnPuertos.setOnAction(event -> {
                    Dispositivo dispositivo = getTableView().getItems().get(getIndex());
                    System.out.println("Escaneando puertos de: " + dispositivo.getIp());

                    Task<Void> animacionTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            String[] estados = {"Cargando", "Cargando.", "Cargando..", "Cargando..."};
                            int i = 0;
                            while (!isCancelled()) {
                                final String estadoActual = estados[i % estados.length];
                                Platform.runLater(() -> {
                                    dispositivo.setPuertos(estadoActual);
                                    tablaDispositivos.refresh();
                                });
                                Thread.sleep(500);
                                i++;
                            }
                            return null;
                        }
                    };

                    Thread hiloAnimacion = new Thread(animacionTask);
                    hiloAnimacion.setDaemon(true);
                    hiloAnimacion.start();

                    Task<Void> escaneoTask = new Task<>() {
                        @Override
                        protected Void call() {
                            List<String> puertos = NmapScanner.escanearPuertos(dispositivo.getIp());
                            List<Servicio> servicios = NmapScanner.obtenerServicios(dispositivo.getIp());

                            Platform.runLater(() -> {
                                animacionTask.cancel();
                                String textoPuertos = String.join(", ", puertos);
                                dispositivo.setPuertos(textoPuertos.isEmpty() ? "Sin puertos abiertos" : textoPuertos);
                                tablaDispositivos.refresh();
                                dispositivo.setServicios(servicios);
                            });

                            return null;
                        }
                    };

                    new Thread(escaneoTask).start();
                });


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

        colAcciones.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
    }

    @FXML
    public void escanearRed() {
        tablaDispositivos.getItems().clear(); // Limpiamos tabla
        tablaDispositivos.getItems().add(new Dispositivo("Cargando...", "Cargando...", "Cargando...")); // Fila temporal

        btnEscanear.setDisable(true); // Desactivar el botón
        Task<Void> tareaEscaneo = new Task<>() {
            @Override
            protected Void call() {
                String subred = NetworkScanner.obtenerSubred();
                if (subred == null) {
                    Platform.runLater(() -> errWifiLabel.setVisible(true));
                    return null;
                } else {
                    Platform.runLater(() -> errWifiLabel.setVisible(false));
                }

                // Iniciar animación de puntos
                Platform.runLater(() -> startPuntosAnimacion());

                Platform.runLater(() -> tablaDispositivos.getItems().clear());

                NetworkScanner.escanearRed(dispositivo -> {
                    Platform.runLater(() -> tablaDispositivos.getItems().add(dispositivo));
                });

                // Cuando termine (ponemos una espera para shutdown del executor si quieres)
                try {
                    Thread.sleep(5000); // Simula duración de escaneo si es rápido (luego ajustamos)
                } catch (InterruptedException ignored) {}

                Platform.runLater(() -> {
                    stopPuntosAnimacion();
                    btnEscanear.setText("Escanear"); // Restaurar botón
                    btnEscanear.setDisable(false);  // Activar otra vez
                });
                return null;
            }
        };
        new Thread(tareaEscaneo).start();
    }

    private Thread puntosThread;
    private boolean animacionActiva = false;

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
                    Thread.sleep(500); // Cambia cada medio segundo
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
