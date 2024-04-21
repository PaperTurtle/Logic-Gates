package com.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;

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

        MenuBar menuBar = new MenuBar();

        // Create menus
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save...");

        // Add menu items to the File menu
        fileMenu.getItems().addAll(openItem, saveItem);

        // Add File menu to the menu bar
        menuBar.getMenus().add(fileMenu);

        // Set action for openItem
        openItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Circuit File");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    List<GateData> gatesData = new CircuitFileManager().loadCircuit(file.getPath());
                    circuitCanvas.loadGates(gatesData);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        // Set action for saveItem
        saveItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Circuit File");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    String filePath = file.getPath();
                    if (!filePath.toLowerCase().endsWith(".json")) {
                        filePath += ".json";
                    }
                    List<GateData> gateData = circuitCanvas.getAllGateData();
                    new CircuitFileManager().saveCircuit(file.getPath(), gateData);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        borderPane.setLeft(scrollableSidebar);
        borderPane.setCenter(circuitCanvas);
        borderPane.setTop(menuBar);

        scene = new Scene(borderPane, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/com/example/styles.css").toExternalForm());
        scene.setOnMouseMoved(event -> {
            if (floatingImageView != null) {
                floatingImageView.setX(event.getX() - floatingImageView.getBoundsInLocal().getWidth() / 2);
                floatingImageView.setY(event.getY() - floatingImageView.getBoundsInLocal().getHeight() / 2);
            }
        });

        scrollableSidebar.setOnMouseClicked(event -> {
            if (floatingImageView != null && !(event.getTarget() instanceof ImageView)) {
                borderPane.getChildren().remove(floatingImageView);
                floatingImageView = null;
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
        VBox inputsSection = new VBox(5);
        VBox outputsSection = new VBox(5);
        VBox gatesSection = new VBox(5);
        inputsSection.getStyleClass().add("section");
        outputsSection.getStyleClass().add("section");
        gatesSection.getStyleClass().add("section");

        sidebar.getChildren().addAll(createSectionLabel("Inputs"), inputsSection, createSectionLabel("Outputs"),
                outputsSection, createSectionLabel("Logic Gates"), gatesSection);

        String[] inputTypes = { "SWITCH" };
        String[] outputTypes = { "LIGHTBULB" };
        String[] gateTypes = { "AND", "OR", "NOT", "BUFFER", "NAND", "NOR", "XOR", "XNOR" };

        addItemsToSection(inputsSection, inputTypes);
        addItemsToSection(outputsSection, outputTypes);
        addItemsToSection(gatesSection, gateTypes);
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-style");
        return label;
    }

    private void addItemsToSection(VBox section, String[] types) {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5, 0, 5, 0));
        int count = 0;

        for (String type : types) {
            ImageView imageView = new ImageView(SvgUtil.loadSvgImage("/com/example/" + type + "_ANSI_Labelled.svg"));
            imageView.setId(type);
            imageView.setFitWidth(90);
            imageView.setFitHeight(40);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setPickOnBounds(true);

            String tooltipText = getTooltipText(type);
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.getStyleClass().add("tooltip-style");
            tooltip.setShowDelay(Duration.millis(100));

            Tooltip.install(imageView, tooltip);

            imageView.setOnMouseEntered(event -> imageView.setCursor(Cursor.HAND));
            imageView.setOnMouseExited(event -> imageView.setCursor(Cursor.DEFAULT));

            imageView.setOnMouseClicked(event -> {
                if (floatingImageView == null) {
                    createFloatingImage(imageView, event);
                }
                if (floatingImageView != null) {
                    borderPane.getChildren().remove(floatingImageView);
                    floatingImageView = null;
                    createFloatingImage(imageView, event);
                }
            });

            hbox.getChildren().add(imageView);
            count++;
            if (count % 2 == 0) {
                section.getChildren().add(hbox);
                hbox = new HBox(10);
                hbox.setPadding(new Insets(5, 0, 5, 0));
            }
        }
        if (count % 2 != 0) {
            section.getChildren().add(hbox);
        }
    }

    private String getTooltipText(String type) {
        switch (type) {
            case "AND":
                return "AND Gate";
            case "OR":
                return "OR Gate";
            case "NOT":
                return "NOT Gate";
            case "BUFFER":
                return "BUFFER Gate";
            case "NAND":
                return "NAND Gate";
            case "NOR":
                return "NOR Gate";
            case "XOR":
                return "XOR Gate";
            case "XNOR":
                return "XNOR Gate";
            case "SWITCH":
                return "Switch";
            case "LIGHTBULB":
                return "Lightbulb";
            default:
                return type + " Gate";
        }
    }

    private void createFloatingImage(ImageView sourceImageView, MouseEvent event) {
        floatingImageView = new ImageView(sourceImageView.getImage());
        floatingImageView.setId(sourceImageView.getId());
        floatingImageView.setFitHeight(50);
        floatingImageView.setPreserveRatio(true);
        floatingImageView.setOpacity(0.5);
        floatingImageView.setX(
                event.getScreenX() - scene.getWindow().getX() - floatingImageView.getBoundsInLocal().getWidth() / 2);
        floatingImageView.setY(event.getScreenY() - scene.getWindow().getY()
                - floatingImageView.getBoundsInLocal().getHeight() / 2 - 28);
        borderPane.getChildren().add(floatingImageView);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
