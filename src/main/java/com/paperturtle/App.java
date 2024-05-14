package com.paperturtle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ButtonBar.ButtonData;

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
        scrollableSidebar.setMinWidth(200);
        scrollableSidebar.setMaxWidth(200);
        scrollableSidebar.setPannable(false);

        initializeSidebar(sidebar);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: white;");

        circuitCanvas = new CircuitCanvas(2000, 2000, scrollPane);
        scrollPane.setContent(circuitCanvas);

        MenuBar menuBar = new MenuBar();

        // Create menus
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save...");

        Menu cursorMenu = new Menu("Cursor");
        MenuItem pointerItem = new MenuItem("Use Pointer");
        // MenuItem grabbyItem = new MenuItem("Use Grabby Hand");

        Menu optionsMenu = new Menu("Options");
        MenuItem tableItem = new MenuItem("Generate Truth Table");

        pointerItem.setOnAction(e -> {
            scene.setCursor(Cursor.DEFAULT);
            circuitCanvas.setCurrentCursorMode(CircuitCanvas.CursorMode.POINTER);
            circuitCanvas.setCursor(Cursor.DEFAULT);
        });

        tableItem.setOnAction(e -> {
            circuitCanvas.getInteractionManager().generateAndDisplayCompleteTruthTable();
        });

        // grabbyItem.setOnAction(e -> {
        // scene.setCursor(Cursor.OPEN_HAND);
        // circuitCanvas.setCurrentCursorMode(CircuitCanvas.CursorMode.GRABBY);
        // circuitCanvas.setCursor(Cursor.OPEN_HAND);
        // });

        fileMenu.getItems().addAll(openItem, saveItem);
        // cursorMenu.getItems().addAll(pointerItem, grabbyItem);
        cursorMenu.getItems().addAll(pointerItem);
        optionsMenu.getItems().addAll(tableItem);

        menuBar.getMenus().addAll(fileMenu, cursorMenu, optionsMenu);

        openItem.setOnAction(e -> {
            if (!circuitCanvas.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Open File");
                alert.setHeaderText("Canvas is not empty");
                alert.setContentText("Would you like to save your current work before opening a new file?");

                ButtonType buttonSave = new ButtonType("Save");
                ButtonType buttonContinue = new ButtonType("Continue without saving");
                ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(buttonSave, buttonContinue, buttonCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == buttonSave) {
                    saveCurrentWork(stage);
                } else if (result.isPresent() && result.get() == buttonCancel) {
                    return;
                }
            }

            openNewFile(stage);
        });

        // Set action for saveItem
        saveItem.setOnAction(e -> {
            List<GateData> gateData = circuitCanvas.getAllGateData();
            List<TextLabel> textLabels = circuitCanvas.getAllTextLabels();
            List<CircuitComponent> components = new ArrayList<>();
            components.addAll(gateData);
            components.addAll(textLabels);

            if (gateData.isEmpty() && textLabels.isEmpty()) {
                showAlert("Warning", "The canvas is empty. Nothing to save.", Alert.AlertType.WARNING);
                return;
            }

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
                    new CircuitFileManager().saveCircuit(filePath, components);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        borderPane.setCenter(circuitCanvas);
        borderPane.setLeft(scrollableSidebar);
        borderPane.setTop(menuBar);

        scene = new Scene(borderPane, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/com/paperturtle/styles.css").toExternalForm());
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
                if ("TextLabel".equals(floatingImageView.getId())) {
                    TextLabel gateLabel = new TextLabel("Label", 90, 40);
                    circuitCanvas.drawTextLabel(gateLabel, x, y);
                } else {
                    LogicGate gate = GateFactory.createGate(floatingImageView.getId());
                    if (gate != null) {
                        circuitCanvas.drawGate(gate, x, y);
                    }
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
        VBox flipflopsSection = new VBox(5);
        VBox utilitiesSection = new VBox(5);
        inputsSection.getStyleClass().add("section");
        outputsSection.getStyleClass().add("section");
        gatesSection.getStyleClass().add("section");
        flipflopsSection.getStyleClass().add("section");
        utilitiesSection.getStyleClass().add("section");

        sidebar.getChildren().addAll(createSectionLabel("Inputs"), inputsSection, createSectionLabel("Outputs"),
                outputsSection, createSectionLabel("Logic Gates"), gatesSection, createSectionLabel("Utilities"),
                utilitiesSection);

        String[] inputTypes = { "SWITCH", "CLOCK", "HIGHCONSTANT", "LOWCONSTANT" };
        String[] outputTypes = { "LIGHTBULB", "FOURBITDIGIT" };
        String[] gateTypes = { "AND", "OR", "NOT", "BUFFER", "NAND", "NOR", "XOR", "XNOR", "TRISTATE" };
        // String[] flipflopTypes = { "ASYNC_RS_FLIPFLOP", "SYNC_RS_FLIPFLOP",
        // "EDGE_JK_FLIPFLOP", "SYNC_T_FLIPFLOP",
        // "EDGE_D_FLIPFLOP", "LEVEL_D_FLIPFLOP" };
        String[] utilityTypes = { "TextLabel" };

        addItemsToSection(inputsSection, inputTypes);
        addItemsToSection(outputsSection, outputTypes);
        addItemsToSection(gatesSection, gateTypes);
        // addItemsToSection(flipflopsSection, flipflopTypes);
        addItemsToSection(utilitiesSection, utilityTypes);
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-style");
        return label;
    }

    private ImageView createImageViewFromTextLabel(TextLabel textLabel) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage writableImage = textLabel.snapshot(parameters, null);

        ImageView imageView = new ImageView(writableImage);
        imageView.setFitWidth(90);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        return imageView;
    }

    private void addItemsToSection(VBox section, String[] types) {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5, 0, 5, 0));
        int count = 0;

        for (String type : types) {
            ImageView imageView;
            if (type.equals("TextLabel")) {
                TextLabel textLabel = new TextLabel("Label", 90, 40);
                imageView = createImageViewFromTextLabel(textLabel);
            } else {
                imageView = new ImageView(
                        SvgUtil.loadSvgImage("/com/paperturtle/" + type + "_ANSI_Labelled.svg"));
            }
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
        floatingImageView.setMouseTransparent(true);
        borderPane.getChildren().add(floatingImageView);
    }

    private void saveCurrentWork(Stage stage) {
        List<GateData> gateData = circuitCanvas.getAllGateData();
        List<TextLabel> textLabels = circuitCanvas.getAllTextLabels();
        List<CircuitComponent> components = new ArrayList<>();
        System.out.println(textLabels);
        System.out.println("Triggered");
        components.addAll(gateData);
        components.addAll(textLabels);

        if (!components.isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Circuit File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            File desktop = new File(System.getProperty("user.home"), "Desktop");
            fileChooser.setInitialDirectory(desktop);
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    String filePath = file.getPath();
                    if (!filePath.toLowerCase().endsWith(".json")) {
                        filePath += ".json";
                    }
                    new CircuitFileManager().saveCircuit(filePath, components);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        } else {
            showAlert("Warning", "The canvas is empty. Nothing to save.", Alert.AlertType.WARNING);
        }
    }

    private void openNewFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Circuit File");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        fileChooser.setInitialDirectory(desktop);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                List<CircuitComponent> gatesData = new CircuitFileManager().loadCircuit(file.getPath());
                circuitCanvas.loadComponents(gatesData);
            } catch (IOException | IllegalArgumentException e) {
                showAlert("Error", "Failed to load the file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
