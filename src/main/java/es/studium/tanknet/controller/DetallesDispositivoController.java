package es.studium.tanknet.controller;

import es.studium.tanknet.core.NmapScanner;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import es.studium.tanknet.model.Servicio;
import es.studium.tanknet.model.Dispositivo;

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
}
