module com.piedrazul.proyectopiedrazul {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.piedrazul.proyectopiedrazul to javafx.fxml;
    exports com.piedrazul.proyectopiedrazul;
}