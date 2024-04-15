package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.Cursor;

/**
 * JavaFX App
 */
public class App extends Application {

    private Scene scene;
    private CircuitCanvas circuitCanvas;

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        initializeSidebar(sidebar);

        circuitCanvas = new CircuitCanvas(600, 400);

        borderPane.setLeft(sidebar);
        borderPane.setCenter(circuitCanvas);

        scene = new Scene(borderPane, 1000, 600);
        stage.setTitle("Logic Gates Simulator");
        stage.setScene(scene);
        stage.show();
    }

    private void initializeSidebar(VBox sidebar) {
        String[] gateTypes = { "AND", "OR", "NOT", "NAND", "NOR", "XOR", "XNOR" };
        for (String type : gateTypes) {
            ImageView imageView = new ImageView(SvgUtil.loadSvgImage("/com/example/" + type + "_ANSI_Labelled.svg"));
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setPickOnBounds(true);

            imageView.setOnMouseEntered(event -> imageView.setCursor(Cursor.HAND));
            imageView.setOnMouseExited(event -> imageView.setCursor(Cursor.DEFAULT));

            imageView.setOnDragDetected(event -> {
                Dragboard db = imageView.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putImage(imageView.getImage());
                db.setContent(content);
                db.setDragView(imageView.getImage(), event.getX(), event.getY());
                event.consume();
            });

            HBox hbox = new HBox(imageView);
            hbox.setPadding(new Insets(5));
            sidebar.getChildren().add(hbox);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}