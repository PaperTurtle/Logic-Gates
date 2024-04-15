package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) {
        BorderPane borderPane = new BorderPane();

        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200); 
        Button exampleGateButton = new Button("AND Gate");
        sidebar.getChildren().add(exampleGateButton);

        Canvas canvas = new Canvas();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        borderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double canvasWidth = newVal.doubleValue() - sidebar.getPrefWidth();
            canvas.setWidth(canvasWidth);
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillRect(0, 0, canvasWidth, canvas.getHeight());
        });

        borderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double canvasHeight = newVal.doubleValue();
            canvas.setHeight(canvasHeight);
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillRect(0, 0, canvas.getWidth(), canvasHeight);
        });

        borderPane.setLeft(sidebar);
        borderPane.setCenter(canvas);

        Scene scene = new Scene(borderPane, 800, 600);
        stage.setTitle("Logic Gates Simulator");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }

}