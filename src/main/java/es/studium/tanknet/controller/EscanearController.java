package es.studium.tanknet.controller;

import es.studium.tanknet.model.Dispositivo;
import es.studium.tanknet.core.NetworkScanner;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class EscanearController {

    @FXML
    private TableView<Dispositivo> tablaDispositivos;

    @FXML
    private TableColumn<Dispositivo, String> colIP;

    @FXML
    private TableColumn<Dispositivo, String> colMac;

    @FXML
    private TableColumn<Dispositivo, String> colNombre;

    @FXML
    public void initialize() {
        colIP.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colMac.setCellValueFactory(new PropertyValueFactory<>("mac"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
    }
    @FXML
    public void escanearRed() {
        System.out.println("Escaneando");
        Task<Void> tareaEscaneo = new Task<>() {
            @Override
            protected Void call() {
                NetworkScanner.escanearRed(dispositivo -> {
                    Platform.runLater(() -> tablaDispositivos.getItems().add(dispositivo));
                });
                return null;
            }
        };
        new Thread(tareaEscaneo).start();
    }
}
