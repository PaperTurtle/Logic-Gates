module com.paperturtle {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires transitive com.google.gson;

    requires batik.transcoder;
    requires batik.svggen;
    requires batik.util;

    opens com.paperturtle to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;

    exports com.paperturtle;
}
