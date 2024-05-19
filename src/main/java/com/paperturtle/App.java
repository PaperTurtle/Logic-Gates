package com.paperturtle;

import com.paperturtle.gui.AppGUI;
import com.paperturtle.gui.CircuitCanvas;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.ImageView;

public class App extends Application {
    /**
     * The scene containing the user interface of the application.
     */
    private Scene scene;

    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas circuitCanvas;

    /**
     * The image view used for displaying a floating image (e.g., when dragging a
     * logic gate).
     */
    private ImageView floatingImageView;

    /**
     * The main layout container for the application's user interface.
     */
    private BorderPane borderPane = new BorderPane();

    @Override
    public void start(Stage stage) {
        AppGUI appGUI = new AppGUI(this, stage);
        appGUI.initialize();
    }

    /**
     * Returns the main layout container for the application's user interface.
     * 
     * @return the main layout container
     */
    public BorderPane getBorderPane() {
        return borderPane;
    }

    /**
     * Returns the scene containing the user interface of the application.
     * 
     * @return the scene containing the user interface
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Sets the scene containing the user interface of the application.
     * 
     * @param scene the scene containing the user interface
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Returns the canvas on which the circuit is drawn.
     * 
     * @return the circuit canvas
     */
    public CircuitCanvas getCircuitCanvas() {
        return circuitCanvas;
    }

    /**
     * Sets the canvas on which the circuit is drawn.
     * 
     * @param circuitCanvas the circuit canvas
     */
    public void setCircuitCanvas(CircuitCanvas circuitCanvas) {
        this.circuitCanvas = circuitCanvas;
    }

    /**
     * Returns the image view used for displaying a floating image (e.g., when
     * 
     * @return the floating image view
     */
    public ImageView getFloatingImageView() {
        return floatingImageView;
    }

    /**
     * Sets the image view used for displaying a floating image (e.g., when
     * 
     * @param floatingImageView the floating image view
     */
    public void setFloatingImageView(ImageView floatingImageView) {
        this.floatingImageView = floatingImageView;
    }

    /**
     * Launches the application.
     * 
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
