package com.paperturtle.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.paperturtle.App;
import com.paperturtle.commands.AddGateCommand;
import com.paperturtle.components.GateFactory;
import com.paperturtle.components.LogicGate;
import com.paperturtle.components.TextLabel;
import com.paperturtle.data.GateData;
import com.paperturtle.managers.CircuitFileManager;
import com.paperturtle.utils.CircuitComponent;
import com.paperturtle.utils.SvgUtil;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javafx.stage.Stage;

public class AppGUI {
    /**
     * The application instance.
     */
    private final App app;

    /**
     * The primary stage of the application.
     */
    private final Stage stage;

    /**
     * Constructs an AppGUI for the specified application and stage.
     */
    public AppGUI(App app, Stage stage) {
        this.app = app;
        this.stage = stage;
    }

    /**
     * Initializes the user interface of the application.
     */
    public void initialize() {
        VBox sidebar = new VBox(10);
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

        CircuitCanvas circuitCanvas = new CircuitCanvas(2000, 2000);
        app.setCircuitCanvas(circuitCanvas);

        MenuBar menuBar = new MenuBar();
        createMenus(menuBar);

        app.getBorderPane().setCenter(circuitCanvas);
        app.getBorderPane().setLeft(scrollableSidebar);
        app.getBorderPane().setTop(menuBar);

        Scene scene = new Scene(app.getBorderPane(), 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/com/paperturtle/styles.css").toExternalForm());
        app.setScene(scene);

        scene.setOnMouseMoved(event -> {
            ImageView floatingImageView = app.getFloatingImageView();
            if (floatingImageView != null) {
                floatingImageView.setX(event.getX() - floatingImageView.getBoundsInLocal().getWidth() / 2);
                floatingImageView.setY(event.getY() - floatingImageView.getBoundsInLocal().getHeight() / 2);
            }
        });

        scrollableSidebar.setOnMouseClicked(event -> {
            ImageView floatingImageView = app.getFloatingImageView();
            if (floatingImageView != null && !(event.getTarget() instanceof ImageView)) {
                app.getBorderPane().getChildren().remove(floatingImageView);
                app.setFloatingImageView(null);
            }
        });

        scene.setOnMouseClicked(event -> {
            ImageView floatingImageView = app.getFloatingImageView();
            if (floatingImageView != null && event.getTarget() == app.getCircuitCanvas()) {
                double x = event.getSceneX() - app.getCircuitCanvas().getLayoutX()
                        - floatingImageView.getBoundsInLocal().getWidth() / 2;
                double y = event.getSceneY() - app.getCircuitCanvas().getLayoutY()
                        - floatingImageView.getBoundsInLocal().getHeight() / 2;
                if ("TextLabel".equals(floatingImageView.getId())) {
                    TextLabel gateLabel = new TextLabel("Label", 90, 40);
                    app.getCircuitCanvas().drawTextLabel(gateLabel, x, y);
                } else {
                    LogicGate gate = GateFactory.createGate(floatingImageView.getId());
                    if (gate != null) {
                        app.getCircuitCanvas().getCommandManager()
                                .executeCommand(new AddGateCommand(app.getCircuitCanvas(), gate, x, y));
                    }
                }
                app.getBorderPane().getChildren().remove(floatingImageView);
                app.setFloatingImageView(null);
            }
        });

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                this::saveCurrentWork);

        stage.setTitle("Logic Gates Simulator");
        stage.setScene(scene);
        stage.show();
        app.getCircuitCanvas().requestFocus();
    }

    /**
     * Initializes the sidebar of the application.
     * 
     * @param sidebar the sidebar to initialize
     */
    private void initializeSidebar(VBox sidebar) {
        VBox inputsSection = new VBox(5);
        VBox outputsSection = new VBox(5);
        VBox gatesSection = new VBox(5);
        VBox utilitiesSection = new VBox(5);
        inputsSection.getStyleClass().add("section");
        outputsSection.getStyleClass().add("section");
        gatesSection.getStyleClass().add("section");
        utilitiesSection.getStyleClass().add("section");

        sidebar.getChildren().addAll(createSectionLabel("Inputs"), inputsSection, createSectionLabel("Outputs"),
                outputsSection, createSectionLabel("Logic Gates"), gatesSection, createSectionLabel("Utilities"),
                utilitiesSection);

        String[] inputTypes = { "SWITCH", "CLOCK", "HIGHCONSTANT", "LOWCONSTANT" };
        String[] outputTypes = { "LIGHTBULB", "FOURBITDIGIT" };
        String[] gateTypes = { "AND", "OR", "NOT", "BUFFER", "NAND", "NOR", "XOR", "XNOR", "TRISTATE" };
        String[] utilityTypes = { "TextLabel" };

        addItemsToSection(inputsSection, inputTypes);
        addItemsToSection(outputsSection, outputTypes);
        addItemsToSection(gatesSection, gateTypes);
        addItemsToSection(utilitiesSection, utilityTypes);
    }

    /**
     * Creates the menus for the application.
     * 
     * @param menuBar the menu bar to which the menus are added
     */
    private void createMenus(MenuBar menuBar) {
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem saveItem = new MenuItem("Save...");
        MenuItem exitItem = new MenuItem("Exit");

        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));

        exitItem.setOnAction(e -> stage.close());

        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        MenuItem redoItem = new MenuItem("Redo");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");
        MenuItem deleteItem = new MenuItem("Delete");

        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        deleteItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));

        undoItem.setOnAction(e -> app.getCircuitCanvas().getCommandManager().undo());
        redoItem.setOnAction(e -> app.getCircuitCanvas().getCommandManager().redo());
        copyItem.setOnAction(e -> app.getCircuitCanvas().getClipboardManager().copySelectedGatesToClipboard());
        pasteItem.setOnAction(e -> app.getCircuitCanvas().getClipboardManager().pasteGatesFromClipboard());
        deleteItem.setOnAction(e -> app.getCircuitCanvas().getGateManager().removeSelectedGates());

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem shortcutsItem = new MenuItem("Keyboard Shortcuts");

        aboutItem.setOnAction(e -> {
            showAlert("About",
                    "Logic Gates Simulator\nVersion 1.0\nCreated by Seweryn Czabanowski\nThis is a school project.",
                    Alert.AlertType.INFORMATION);
        });

        shortcutsItem.setOnAction(e -> {
            showAlert("Keyboard Shortcuts",
                    "Ctrl+O: Open file\n" +
                            "Ctrl+S: Save file\n" +
                            "Ctrl+C: Copy selected gates\n" +
                            "Ctrl+V: Paste gates from clipboard\n" +
                            "Ctrl+X: Cut selected gates\n" +
                            "Ctrl+A: Select all components\n" +
                            "Ctrl+Z: Undo\n" +
                            "Ctrl+Y: Redo\n" +
                            "Ctrl+D: Delete selected gates\n",
                    Alert.AlertType.INFORMATION);
        });

        Menu optionsMenu = new Menu("Options");
        MenuItem tableItem = new MenuItem("Generate Truth Table");
        MenuItem clearItem = new MenuItem("Clear the canvas");

        tableItem.setOnAction(e -> {
            app.getCircuitCanvas().getInteractionManager().generateAndDisplayCompleteTruthTable();
        });

        clearItem.setOnAction(e -> {
            app.getCircuitCanvas().clearCanvas();
        });

        fileMenu.getItems().addAll(openItem, saveItem, exitItem);
        optionsMenu.getItems().addAll(tableItem, clearItem);
        editMenu.getItems().addAll(undoItem, redoItem, copyItem, pasteItem, deleteItem);
        helpMenu.getItems().addAll(aboutItem, shortcutsItem);
        menuBar.getMenus().addAll(fileMenu, optionsMenu, editMenu, helpMenu);

        openItem.setOnAction(e -> {
            if (!app.getCircuitCanvas().isEmpty()) {
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
                    saveCurrentWork();
                } else if (result.isPresent() && result.get() == buttonCancel) {
                    return;
                }
            }

            openNewFile();
        });

        saveItem.setOnAction(e -> {
            List<GateData> gateData = app.getCircuitCanvas().getAllGateData();
            List<TextLabel> textLabels = app.getCircuitCanvas().getAllTextLabels();
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
    }

    /**
     * Creates a label for a section in the sidebar.
     * 
     * @param text the text of the label
     * @return the created label
     */
    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-style");
        return label;
    }

    /**
     * Creates an image view from a text label.
     * 
     * @param textLabel the text label to create an image view from
     * @return the created image view
     */
    private ImageView createImageViewFromTextLabel(TextLabel textLabel) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage writableImage = textLabel.snapshot(parameters, null);

        ImageView imageView = new ImageView(writableImage);
        imageView.setFitWidth(80);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setPickOnBounds(true);

        return imageView;
    }

    /**
     * Adds items to a section in the sidebar.
     * 
     * @param section the section to which the items are added
     * @param types   the types of the items to add
     */
    private void addItemsToSection(VBox section, String[] types) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 0, 5, 0));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(30);
        gridPane.getColumnConstraints().addAll(col1, col2);

        int count = 0;

        for (String type : types) {
            ImageView imageView;
            if (type.equals("TextLabel")) {
                TextLabel textLabel = new TextLabel("Label", 90, 40);
                imageView = createImageViewFromTextLabel(textLabel);
            } else {
                imageView = new ImageView(SvgUtil.loadSvgImage("/com/paperturtle/" + type + "_ANSI_Labelled.svg"));
            }
            imageView.setId(type);
            imageView.setFitWidth(80);
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
                ImageView floatingImageView = app.getFloatingImageView();
                if (floatingImageView == null) {
                    createFloatingImage(imageView, event);
                }
                if (floatingImageView != null) {
                    app.getBorderPane().getChildren().remove(floatingImageView);
                    app.setFloatingImageView(null);
                    createFloatingImage(imageView, event);
                }
            });

            int row = count / 2;
            int col = count % 2;
            gridPane.add(imageView, col, row);

            GridPane.setHalignment(imageView, HPos.CENTER);
            GridPane.setValignment(imageView, VPos.CENTER);

            count++;
        }

        section.getChildren().add(gridPane);
    }

    /**
     * Returns the tooltip text for the specified gate type.
     * 
     * @param type the type of the gate
     * @return the tooltip text for the specified gate type
     */
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

    /**
     * Creates a floating image for dragging.
     * 
     * @param sourceImageView the source image view
     * @param event           the mouse event
     */
    private void createFloatingImage(ImageView sourceImageView, MouseEvent event) {
        ImageView floatingImageView = new ImageView(sourceImageView.getImage());
        floatingImageView.setId(sourceImageView.getId());
        floatingImageView.setFitHeight(50);
        floatingImageView.setPreserveRatio(true);
        floatingImageView.setOpacity(0.5);
        floatingImageView.setX(event.getScreenX() - app.getScene().getWindow().getX()
                - floatingImageView.getBoundsInLocal().getWidth() / 2);
        floatingImageView.setY(event.getScreenY() - app.getScene().getWindow().getY()
                - floatingImageView.getBoundsInLocal().getHeight() / 2 - 28);
        floatingImageView.setMouseTransparent(true);
        app.getBorderPane().getChildren().add(floatingImageView);
        app.setFloatingImageView(floatingImageView);
    }

    /**
     * Saves the current work to a file.
     */
    private void saveCurrentWork() {
        List<GateData> gateData = app.getCircuitCanvas().getAllGateData();
        List<TextLabel> textLabels = app.getCircuitCanvas().getAllTextLabels();
        List<CircuitComponent> components = new ArrayList<>();
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

    /**
     * Opens a new file.
     */
    private void openNewFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Circuit File");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("JSON Files", "*.json"));
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        fileChooser.setInitialDirectory(desktop);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                List<CircuitComponent> gatesData = new CircuitFileManager().loadCircuit(file.getPath());
                app.getCircuitCanvas().loadComponents(gatesData);
            } catch (IOException | IllegalArgumentException e) {
                showAlert("Error", "Failed to load the file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Shows an alert with the specified title and content.
     * 
     * @param title     the title of the alert
     * @param content   the content of the alert
     * @param alertType the type of the alert
     */
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
