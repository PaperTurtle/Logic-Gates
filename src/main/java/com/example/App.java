package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;

/**
 * JavaFX App
 */
public class App extends Application {

    private Scene scene;
    private CircuitCanvas circuitCanvas;
    private ImageView floatingImageView;
    private BorderPane borderPane = new BorderPane();

    @Override
    public void start(Stage stage) {

        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(200);
        initializeSidebar(sidebar);

        circuitCanvas = new CircuitCanvas(600, 400);

        borderPane.setLeft(sidebar);
        borderPane.setCenter(circuitCanvas);

        scene = new Scene(borderPane, 1000, 600);
        scene.setOnMouseMoved(event -> {
            if (floatingImageView != null) {
                floatingImageView.setX(event.getX() - floatingImageView.getBoundsInLocal().getWidth() / 2);
                floatingImageView.setY(event.getY() - floatingImageView.getBoundsInLocal().getHeight() / 2);
            }
        });
        scene.setOnMouseClicked(event -> {
            if (floatingImageView != null && event.getTarget() == circuitCanvas) {
                double sidebarWidth = sidebar.getWidth();
                double x = event.getX() - floatingImageView.getBoundsInLocal().getWidth() / 2 - sidebarWidth;
                double y = event.getY() - floatingImageView.getBoundsInLocal().getHeight() / 2;
                ImageView toPlace = new ImageView(floatingImageView.getImage());
                toPlace.setX(x);
                toPlace.setY(y);
                circuitCanvas.getChildren().add(toPlace);
                borderPane.getChildren().remove(floatingImageView);
                floatingImageView = null;
            }
        });

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

            Tooltip tooltip = new Tooltip(type + " Gate");
            Tooltip.install(imageView, tooltip);

            imageView.setOnMouseEntered(event -> imageView.setCursor(Cursor.HAND));
            imageView.setOnMouseExited(event -> imageView.setCursor(Cursor.DEFAULT));

            imageView.setOnMouseClicked(event -> {
                if (floatingImageView == null) {
                    floatingImageView = new ImageView(imageView.getImage());
                    floatingImageView.setFitHeight(50);
                    floatingImageView.setPreserveRatio(true);
                    floatingImageView.setOpacity(0.5);
                    floatingImageView.setX(event.getScreenX() - scene.getWindow().getX()
                            - floatingImageView.getBoundsInLocal().getWidth() / 2 - 7);
                    floatingImageView.setY(event.getScreenY() - scene.getWindow().getY()
                            - floatingImageView.getBoundsInLocal().getHeight() / 2 - 27);
                    borderPane.getChildren().add(floatingImageView);
                }
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