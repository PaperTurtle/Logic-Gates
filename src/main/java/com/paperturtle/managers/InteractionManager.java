package com.paperturtle.managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.paperturtle.components.LogicGate;
import com.paperturtle.components.inputs.HighConstantGate;
import com.paperturtle.components.inputs.LowConstantGate;
import com.paperturtle.components.inputs.SwitchGate;
import com.paperturtle.components.outputs.FourBitDigitGate;
import com.paperturtle.components.outputs.Lightbulb;
import com.paperturtle.components.utilities.TextLabel;
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
        List<Lightbulb> lightbulbs = new ArrayList<>();
        List<FourBitDigitGate> fourBitDigitGates = new ArrayList<>();
        List<Boolean> constantInputs = new ArrayList<>();

        for (LogicGate gate : selectedGates) {
            if (gate instanceof SwitchGate) {
                switchGates.add((SwitchGate) gate);
            } else if (gate instanceof Lightbulb) {
                lightbulbs.add((Lightbulb) gate);
            } else if (gate instanceof FourBitDigitGate) {
                fourBitDigitGates.add((FourBitDigitGate) gate);
            } else if (gate instanceof HighConstantGate) {
                constantInputs.add(true);
            } else if (gate instanceof LowConstantGate) {
                constantInputs.add(false);
            }
        }

        if ((constantInputs.isEmpty() && switchGates.isEmpty())
                || (lightbulbs.isEmpty() && fourBitDigitGates.isEmpty())) {
            System.out.println("input or output gates not found in the selected gates.");
            return;
        }

        int numInputs = switchGates.size();
        int numConstants = constantInputs.size();
        int numLightbulbs = lightbulbs.size();
        int numFourBitDigitGates = fourBitDigitGates.size();
        int totalCombinations = 1 << numInputs;

        Boolean[][] truthTableInputs = new Boolean[totalCombinations][numInputs + numConstants];
        Object[][] truthTableOutputs = new Object[totalCombinations][numLightbulbs + numFourBitDigitGates];

        boolean[] initialStates = new boolean[numInputs];
        for (int i = 0; i < numInputs; i++) {
            initialStates[i] = switchGates.get(i).getState();
        }

        for (int i = 0; i < totalCombinations; i++) {
            for (int j = 0; j < numInputs; j++) {
                truthTableInputs[i][j] = (i & (1 << j)) != 0;
            }

            for (int j = 0; j < numConstants; j++) {
                truthTableInputs[i][numInputs + j] = constantInputs.get(j);
            }
        }

        for (int i = 0; i < totalCombinations; i++) {
            for (int j = 0; j < numInputs; j++) {
                switchGates.get(j).setState(truthTableInputs[i][j]);
            }

            for (int k = 0; k < numLightbulbs; k++) {
                truthTableOutputs[i][k] = lightbulbs.get(k).evaluate();
            }

            for (int k = 0; k < numFourBitDigitGates; k++) {
                truthTableOutputs[i][numLightbulbs + k] = fourBitDigitGates.get(k).getOutputValue();
            }
        }

        for (int i = 0; i < numInputs; i++) {
            switchGates.get(i).setState(initialStates[i]);
        }
        lightbulbs.forEach(Lightbulb::evaluate);
        fourBitDigitGates.forEach(FourBitDigitGate::evaluate);

        displaySimplifiedTruthTable(truthTableInputs, truthTableOutputs);
    }

    /**
     * Displays the simplified truth table.
     * 
     * @param inputs  the input values of the truth table
     * @param outputs the output values of the truth table
     */
    private void displaySimplifiedTruthTable(Boolean[][] inputs, Object[][] outputs) {
        TableView<List<String>> table = new TableView<>();
        ObservableList<List<String>> data = FXCollections.observableArrayList();

        for (int i = 0; i < inputs[0].length; i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> inputColumn = new TableColumn<>("I" + (i + 1));
            inputColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
            table.getColumns().add(inputColumn);
        }

        for (int i = 0; i < outputs[0].length; i++) {
            final int colIndex = inputs[0].length + i;
            TableColumn<List<String>, String> outputColumn = new TableColumn<>("O" + (i + 1));
            outputColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
            table.getColumns().add(outputColumn);
        }

        for (int i = 0; i < inputs.length; i++) {
            List<String> row = new ArrayList<>();
            boolean rowIsEmpty = true;

            for (Boolean input : inputs[i]) {
                row.add(input ? "true" : "false");
                if (input != null) {
                    rowIsEmpty = false;
                }
            }

            for (Object output : outputs[i]) {
                if (output instanceof Boolean) {
                    row.add((Boolean) output ? "true" : "false");
                } else if (output instanceof Integer) {
                    row.add(output.toString());
                }
                System.out.println("Output for row " + i + ": " + output);
            }

            if (!rowIsEmpty) {
                data.add(row);
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
    private void exportTruthTableToCsv(Boolean[][] inputs, Object[][] outputs) {
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

                for (int i = 0; i < outputs[0].length; i++) {
                    sb.append("O").append(i + 1);
                    if (i < outputs[0].length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("\n");

                for (int i = 0; i < inputs.length; i++) {
                    for (Boolean input : inputs[i]) {
                        sb.append(input ? "true" : "false").append(",");
                    }
                    for (int j = 0; j < outputs[i].length; j++) {
                        if (outputs[i][j] instanceof Boolean) {
                            sb.append((Boolean) outputs[i][j] ? "true" : "false");
                        } else if (outputs[i][j] instanceof Integer) {
                            sb.append(outputs[i][j].toString());
                        }
                        if (j < outputs[i].length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("\n");
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
            if (event.getButton() == MouseButton.PRIMARY && canvas.getCurrentLine() == null) {
                if (gate.getOutputConnections().size() < gate.getMaxOutputConnections()) {
                    Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(),
                            outputMarker.getCenterY());
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
