package com.paperturtle.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.components.Lightbulb;
import com.paperturtle.components.LogicGate;
import com.paperturtle.components.SwitchGate;
import com.paperturtle.components.TextLabel;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
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
     * Handles mouse pressed events for a logic gate.
     * 
     * @param imageView the ImageView of the gate
     * @param gate      the logic gate
     * @param event     the mouse event
     */
    private void handleMousePressedForGate(ImageView imageView, LogicGate gate, MouseEvent event) {
        if (highlightedGate != null && highlightedGate != gate) {
            highlightedGate.getImageView().getStyleClass().remove("selected");
        }
        highlightedGate = gate;

        if (!imageView.getStyleClass().contains("selected")) {
            imageView.getStyleClass().add("selected");
        }

        double offsetX = event.getSceneX() - imageView.getLayoutX();
        double offsetY = event.getSceneY() - imageView.getLayoutY();
        imageView.setUserData(new Object[] { offsetX, offsetY, gate });

        if (event.getButton() == MouseButton.SECONDARY) {
            canvas.getContextMenuManager().showContextMenu(imageView, gate, event);
        }
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

        for (LogicGate gate : selectedGates) {
            if (gate instanceof SwitchGate) {
                switchGates.add((SwitchGate) gate);
            } else if (gate instanceof Lightbulb) {
                lightbulb = (Lightbulb) gate;
            }
        }

        if (switchGates.isEmpty() || lightbulb == null) {
            System.out.println("SwitchGates or Lightbulb not found in the selected gates.");
            return;
        }

        int numInputs = switchGates.size();
        int totalCombinations = 1 << numInputs;

        Boolean[][] truthTableInputs = new Boolean[totalCombinations][numInputs];
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
            for (Boolean input : inputs[i]) {
                row.add(input ? "true" : "false");
            }
            row.add(outputs[i] ? "true" : "false");
            data.add(row);
        }

        table.setItems(data);

        Stage stage = new Stage();
        stage.setTitle("Simplified Truth Table");
        Scene scene = new Scene(table, 400, 300);
        stage.setScene(scene);
        stage.show();
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
