package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * JavaFX App
 */
public class App extends Application {

    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        initializeSidebar(sidebar);

        canvas = new Canvas(600, 400);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        borderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double canvasWidth = newVal.doubleValue() - sidebar.getPrefWidth();
            canvas.setWidth(canvasWidth);
            gc.fillRect(0, 0, canvasWidth, canvas.getHeight());
        });

        borderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double canvasHeight = newVal.doubleValue();
            canvas.setHeight(canvasHeight);
            gc.fillRect(0, 0, canvas.getWidth(), canvasHeight);
        });

        borderPane.setLeft(sidebar);
        borderPane.setCenter(canvas);

        scene = new Scene(borderPane, 800, 600);
        stage.setTitle("Logic Gates Simulator");
        stage.setScene(scene);
        stage.show();
    }

    private void initializeSidebar(VBox sidebar) {
        String[] gateTypes = { "AND", "OR", "NOT", "NAND", "NOR", "XOR", "XNOR" };
        for (String type : gateTypes) {
            Button gateButton = new Button(type);
            gateButton.setOnAction(event -> addGateToCanvas(type));
            sidebar.getChildren().add(gateButton);
        }
    }

    private void addGateToCanvas(String type) {
        System.out.println(type + " gate added");
    }

    public static void main(String[] args) {
        launch(args);
    }

}