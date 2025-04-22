module es.studium.tfg {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;


    opens es.studium.tanknet.controller to javafx.fxml;
    exports es.studium.tanknet.main;
    opens es.studium.tanknet.model to javafx.base;
    exports es.studium.tanknet.model to com.fasterxml.jackson.databind;
    opens es.studium.tanknet.main to javafx.fxml;
}