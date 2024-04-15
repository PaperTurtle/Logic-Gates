module com.example {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires com.google.gson;

    requires batik.transcoder;
    requires batik.svggen;
    requires batik.util;

    opens com.example to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;

    exports com.example;
}
