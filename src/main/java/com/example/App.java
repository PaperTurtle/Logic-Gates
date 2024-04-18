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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;

/**
 * JavaFX App
 */
public class App extends Application {

    private Scene scene;
    private CircuitCanvas circuitCanvas;
    private ImageView floatingImageView;
    private BorderPane borderPane = new BorderPane();
    private VBox sidebar;

    @Override
    public void start(Stage stage) {

        sidebar = new VBox(10);
        sidebar.setPrefWidth(200);

        ScrollPane scrollableSidebar = new ScrollPane();
        scrollableSidebar.setContent(sidebar);
        scrollableSidebar.setFitToWidth(true);
        scrollableSidebar.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollableSidebar.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        initializeSidebar(sidebar);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        circuitCanvas = new CircuitCanvas(600, 400, scrollPane);
        scrollPane.setContent(circuitCanvas);

        borderPane.setLeft(scrollableSidebar);
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
                LogicGate gate = GateFactory.createGate(floatingImageView.getId());
                if (gate != null) {
                    circuitCanvas.drawGate(gate, x, y);
                }
                borderPane.getChildren().remove(floatingImageView);
                floatingImageView = null;
            }
        });

        stage.setTitle("Logic Gates Simulator");
        stage.setScene(scene);
        stage.show();

        circuitCanvas.requestFocus();
    }

    private void initializeSidebar(VBox sidebar) {
        // ! TODO ADD LIGHTBULB TO SIDEBAR OR ADD SCROLLABLE SECTIONS
        String[] gateTypes = { "AND", "OR", "NOT", "BUFFER", "NAND", "NOR", "XOR", "XNOR", "SWITCH", "LIGHTBULB" };
        for (String type : gateTypes) {
            ImageView imageView = new ImageView(SvgUtil.loadSvgImage("/com/example/" + type + "_ANSI_Labelled.svg"));

            imageView.setId(type);
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
                    floatingImageView.setId(type);
                    floatingImageView.setFitHeight(50);
                    floatingImageView.setPreserveRatio(true);
                    floatingImageView.setOpacity(0.5);
                    floatingImageView.setX(event.getScreenX() - scene.getWindow().getX()
                            - floatingImageView.getBoundsInLocal().getWidth() / 2 - 28);
                    floatingImageView.setY(event.getScreenY() - scene.getWindow().getY()
                            - floatingImageView.getBoundsInLocal().getHeight() / 2);
                    borderPane.getChildren().add(floatingImageView);
                }
            });

            HBox hbox = new HBox(imageView);
            hbox.setPadding(new Insets(5));
            sidebar.getChildren().add(hbox);
        }
    }

    public VBox getSidebar() {
        return sidebar;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
