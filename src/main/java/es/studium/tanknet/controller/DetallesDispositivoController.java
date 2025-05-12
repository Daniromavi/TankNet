package es.studium.tanknet.controller;

import javafx.fxml.FXML;
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
    private TableView<Servicio> tablaServicios;

    @FXML
    private TableColumn<Servicio, String> colPuerto;

    @FXML
    private TableColumn<Servicio, String> colServicio;

    @FXML
    private TableColumn<Servicio, String> colVersion;

    public void initialize() {

    colPuerto.setCellValueFactory(new PropertyValueFactory<>("puerto"));
    colServicio.setCellValueFactory(new PropertyValueFactory<>("nombre"));
    colVersion.setCellValueFactory(new PropertyValueFactory<>("version"));

}

    public void setDispositivo(Dispositivo dispositivo) {
        ipLabel.setText("IP: " + dispositivo.getIp());
        macLabel.setText("MAC: " + dispositivo.getMac());

        if (dispositivo.getServicios() != null && !dispositivo.getServicios().isEmpty()) {
            tablaServicios.getItems().setAll(dispositivo.getServicios());
        } else {
            tablaServicios.getItems().clear(); // Opcional: limpia la tabla si no hay nada
            tablaServicios.setPlaceholder(new Label("No se han escaneado servicios."));
            System.out.println("Este dispositivo no tiene servicios escaneados.");
        }

        tablaServicios.refresh();
    }

}
