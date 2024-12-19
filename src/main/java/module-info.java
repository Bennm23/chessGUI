module chess.chessgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
//    requires eu.hansolo.tilesfx;
//    requires com.almasb.fxgl.all;
    requires com.google.protobuf;


    opens chess.chessgui to javafx.fxml;
    exports chess.chessgui;
    exports protocols;
    exports pieces;
}