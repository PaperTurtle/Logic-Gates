package com.paperturtle.managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.paperturtle.components.HighConstantGate;
import com.paperturtle.components.Lightbulb;
import com.paperturtle.components.LogicGate;
import com.paperturtle.components.LowConstantGate;
import com.paperturtle.components.SwitchGate;
import com.paperturtle.components.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * The InteractionManager class is responsible for handling user interactions
 * with the circuit canvas, including dragging, clicking, and creating
 * connections.
 * 
 * @see CircuitCanvas
 * 
 * @author Seweryn Czabanowski
 */
public class InteractionManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * A flag indicating whether a logic gate was just selected.
     */
    private boolean justSelected = false;

    /**
     * The logic gate that is currently highlighted.
     */
    private LogicGate highlightedGate = null;

    /**
     * The drag manager for the circuit canvas.
     */
    private DragManager dragManager;

    /**
     * Constructs an InteractionManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public InteractionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
        this.dragManager = new DragManager(canvas);
    }

    /**
     * Sets up drag handlers for a logic gate.
     * 
     * @param imageView the ImageView of the gate
     * @param gate      the logic gate
     */
    public void setupDragHandlers(ImageView imageView, LogicGate gate) {
        dragManager.setupDragHandlers(imageView, gate);
    }

    /**
     * Sets up drag handlers for a text label.
     * 
     * @param textLabel the text label
     */
    public void setupDragHandlersForLabel(TextLabel textLabel) {
        dragManager.setupDragHandlersForLabel(textLabel);
    }

    /**
     * Handles canvas click events.
     * 
     * @param event the mouse event
     */
    public void handleCanvasClick(MouseEvent event) {
        if (!(event.getTarget() instanceof ImageView)) {
            if (justSelected) {
                justSelected = false;
                return;
            }
            canvas.requestFocus();
        } else {
            ImageView clickedImageView = (ImageView) event.getTarget();
            LogicGate clickedGate = canvas.getGateImageViews().get(clickedImageView);
            if (highlightedGate != null && highlightedGate != clickedGate) {
                canvas.getGateManager().deselectAllGates();
                if (clickedGate != null) {
                    highlightedGate = clickedGate;
                    if (!clickedImageView.getStyleClass().contains("selected")) {
                        clickedImageView.getStyleClass().add("selected");
                    }
                    if (clickedGate instanceof SwitchGate) {
                        ((SwitchGate) clickedGate).setSelected(true);
                    }
                }
            }
        }
    }

    /**
     * Generates and displays the complete truth table for the selected gates.
     */
    public void generateAndDisplayCompleteTruthTable() {
        List<LogicGate> selectedGates = canvas.getSelectedGates();

        List<SwitchGate> switchGates = new ArrayList<>();
        Lightbulb lightbulb = null;
        List<Boolean> constantInputs = new ArrayList<>();

        for (LogicGate gate : selectedGates) {
            if (gate instanceof SwitchGate) {
                switchGates.add((SwitchGate) gate);
            } else if (gate instanceof Lightbulb) {
                lightbulb = (Lightbulb) gate;
            } else if (gate instanceof HighConstantGate) {
                constantInputs.add(true);
            } else if (gate instanceof LowConstantGate) {
                constantInputs.add(false);
            }
        }

        if ((constantInputs.isEmpty() && switchGates.isEmpty()) || lightbulb == null) {
            System.out.println("SwitchGates or Lightbulb not found in the selected gates.");
            return;
        }

        int numInputs = switchGates.size();
        int numConstants = constantInputs.size();
        int totalCombinations = 1 << numInputs;

        Boolean[][] truthTableInputs = new Boolean[totalCombinations][numInputs + numConstants];
        Boolean[] truthTableOutputs = new Boolean[totalCombinations];

        boolean[] initialStates = new boolean[numInputs];
        for (int i = 0; i < numInputs; i++) {
            initialStates[i] = switchGates.get(i).getState();
        }

        for (int i = 0; i < totalCombinations; i++) {
            for (int j = 0; j < numInputs; j++) {
                truthTableInputs[i][j] = (i & (1 << j)) != 0;
            }
        }

        for (int i = 0; i < totalCombinations; i++) {
            for (int j = 0; j < numInputs; j++) {
                switchGates.get(j).setState(truthTableInputs[i][j]);
            }

            for (int j = 0; j < numConstants; j++) {
                truthTableInputs[i][numInputs + j] = constantInputs.get(j);
            }

            truthTableOutputs[i] = lightbulb.evaluate();
        }

        for (int i = 0; i < numInputs; i++) {
            switchGates.get(i).setState(initialStates[i]);
        }
        lightbulb.evaluate();

        displaySimplifiedTruthTable(truthTableInputs, truthTableOutputs);
    }

    /**
     * Displays the simplified truth table.
     * 
     * @param inputs  the input values of the truth table
     * @param outputs the output values of the truth table
     */
    private void displaySimplifiedTruthTable(Boolean[][] inputs, Boolean[] outputs) {
        TableView<List<String>> table = new TableView<>();
        ObservableList<List<String>> data = FXCollections.observableArrayList();

        for (int i = 0; i < inputs[0].length; i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> inputColumn = new TableColumn<>("I" + (i + 1));
            inputColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
            table.getColumns().add(inputColumn);
        }

        TableColumn<List<String>, String> outputColumn = new TableColumn<>("O1");
        outputColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(inputs[0].length)));
        table.getColumns().add(outputColumn);

        for (int i = 0; i < inputs.length; i++) {
            List<String> row = new ArrayList<>();
            boolean rowIsEmpty = true;

            System.out.println("Processing row " + i);

            for (Boolean input : inputs[i]) {
                row.add(input ? "true" : "false");
                if (input) {
                    rowIsEmpty = false;
                }
            }

            System.out.println("Row before adding output: " + row);
            System.out.println("Row is empty: " + rowIsEmpty);
            System.out.println("Output value: " + outputs[i]);

            if (!rowIsEmpty || outputs[i]) {
                row.add(outputs[i] ? "true" : "false");
                data.add(row);
                System.out.println("Added row: " + row);
            } else {
                System.out.println("Skipped row: " + row);
            }
        }

        table.setItems(data);

        Button exportButton = new Button("Export to CSV");
        exportButton.setOnAction(e -> exportTruthTableToCsv(inputs, outputs));

        VBox vbox = new VBox(exportButton, table);

        Stage stage = new Stage();
        stage.setTitle("Truth Table");
        Scene scene = new Scene(vbox);
        stage.setScene(scene);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        double rowHeight = 25;
        double tableHeight = rowHeight * (data.size() + 1);
        table.setPrefHeight(tableHeight);

        vbox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        vbox.setPrefHeight(Region.USE_COMPUTED_SIZE);

        stage.sizeToScene();

        ContextMenu contextMenu = new ContextMenu();
        MenuItem exportCsv = new MenuItem("Export to CSV");
        exportCsv.setOnAction(e -> exportTruthTableToCsv(inputs, outputs));
        contextMenu.getItems().add(exportCsv);

        table.setContextMenu(contextMenu);
        table.getStylesheets().add(getClass().getResource("/com/paperturtle/styles.css").toExternalForm());

        stage.show();
    }

    /**
     * Exports the truth table to a CSV file.
     * 
     * @param inputs  the input values of the truth table
     * @param outputs the output values of the truth table
     */
    private void exportTruthTableToCsv(Boolean[][] inputs, Boolean[] outputs) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("truth_table.csv");
        Stage stage = new Stage();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < inputs[0].length; i++) {
                    sb.append("I").append(i + 1).append(",");
                }
                sb.append("O1\n");

                for (int i = 0; i < inputs.length; i++) {
                    for (Boolean input : inputs[i]) {
                        sb.append(input ? "true" : "false").append(",");
                    }
                    sb.append(outputs[i] ? "true" : "false").append("\n");
                }

                writer.write(sb.toString());
                System.out.println("Truth table exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets up connection handlers for creating connections between logic gates.
     */
    public void setupConnectionHandlers() {
        canvas.setOnMouseMoved(mouseMoveEvent -> {
            if (canvas.getCurrentLine() != null) {
                canvas.getCurrentLine().setEndX(mouseMoveEvent.getX());
                canvas.getCurrentLine().setEndY(mouseMoveEvent.getY());
            }
        });

        canvas.setOnMouseClicked(mouseClickEvent -> {
            if (canvas.getCurrentLine() != null && mouseClickEvent.getClickCount() == 1) {
                if (!canvas.getConnectionManager().finalizeConnection(mouseClickEvent.getX(), mouseClickEvent.getY(),
                        null)) {
                    canvas.getChildren().remove(canvas.getCurrentLine());
                }
                canvas.setCurrentLine(null);
                resetInteractionHandlers();
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY && event.getTarget() instanceof Line) {
                Line targetLine = (Line) event.getTarget();
                if (canvas.getOpenContextMenu() != null) {
                    canvas.getOpenContextMenu().hide();
                }
                ContextMenu lineContextMenu = new ContextMenu();
                MenuItem deleteLine = new MenuItem("Remove");
                deleteLine.setOnAction(e -> {
                    canvas.getConnectionManager().removeConnection(targetLine);
                    e.consume();
                });
                lineContextMenu.getItems().add(deleteLine);
                lineContextMenu.show(targetLine, event.getScreenX(), event.getScreenY());
                canvas.setOpenContextMenu(lineContextMenu);
            }
        });
    }

    /**
     * Resets the interaction handlers.
     */
    private void resetInteractionHandlers() {
        canvas.setOnMouseMoved(null);
        canvas.setOnMouseClicked(null);
    }

    /**
     * Sets up output interaction for a logic gate.
     * 
     * @param outputMarker the output marker of the gate
     * @param gate         the logic gate
     */
    public void setupOutputInteraction(Circle outputMarker, LogicGate gate) {
        outputMarker.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && canvas.getCurrentLine() == null
                    && gate.canAddOutputConnection()) {
                Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(), outputMarker.getCenterY());
                canvas.setCurrentLine(new Line(outputPos.getX(), outputPos.getY(), event.getX(), event.getY()));
                Color lineColor = gate.evaluate() ? Color.RED : Color.BLACK;
                canvas.getCurrentLine().setStroke(lineColor);
                canvas.getCurrentLine().setStrokeWidth(3.5);
                canvas.getChildren().add(canvas.getCurrentLine());
                canvas.getLineToStartGateMap().put(canvas.getCurrentLine(), gate);
                canvas.getConnectionManager().setStartGate(gate);
                gate.addOutputConnection(canvas.getCurrentLine());
                setupConnectionHandlers();
                event.consume();
            }
        });
    }

    /**
     * Get the highlighted gate.
     * 
     * @return the highlighted gate
     */
    public LogicGate getHighlightedGate() {
        return highlightedGate;
    }

    /**
     * Sets the highlighted gate.
     * 
     * @param highlightedGate the highlighted gate to set
     */
    public void setHighlightedGate(LogicGate highlightedGate) {
        this.highlightedGate = highlightedGate;
    }

    /**
     * Checks if the gate was just selected.
     * 
     * @return true if the gate was just selected, false otherwise
     */
    public boolean isJustSelected() {
        return justSelected;
    }

    /**
     * Sets the justSelected flag.
     * 
     * @param justSelected the flag to set
     */
    public void setJustSelected(boolean justSelected) {
        this.justSelected = justSelected;
    }
}
