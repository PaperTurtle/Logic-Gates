/**
 * This package contains the main classes and interfaces for the Logic Gate
 * Simulator application.
 * 
 * <p>
 * It includes the main application class, core logic, and essential components
 * that form the backbone of the application.
 * </p>
 */
module com.paperturtle {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires transitive com.google.gson;

    requires batik.transcoder;
    requires batik.svggen;
    requires batik.util;

    exports com.paperturtle;
    exports com.paperturtle.components;
    exports com.paperturtle.components.gates;
    exports com.paperturtle.components.inputs;
    exports com.paperturtle.components.outputs;
    exports com.paperturtle.components.utilities;
    exports com.paperturtle.data;
    exports com.paperturtle.managers;
    exports com.paperturtle.commands;
    exports com.paperturtle.serializers;
    exports com.paperturtle.utils;
    exports com.paperturtle.gui;

    opens com.paperturtle to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.data to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.managers to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.commands to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.components to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.components.gates to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.components.inputs to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.components.outputs to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.components.utilities to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.serializers to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.utils to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;
    opens com.paperturtle.gui to javafx.fxml, batik.transcoder, batik.svggen, javafx.swing;

}