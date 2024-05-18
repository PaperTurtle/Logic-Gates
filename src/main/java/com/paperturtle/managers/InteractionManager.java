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
     * Constructs an InteractionManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public InteractionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
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
        imageView.setPickOnBounds(true);

        imageView.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && imageView.getStyleClass().contains("selected")) {
                Object[] userData = (Object[]) imageView.getUserData();
                double baseX = (double) userData[0];
                double baseY = (double) userData[1];

                double deltaX = event.getSceneX() - baseX;
                double deltaY = event.getSceneY() - baseY;

                for (Map.Entry<ImageView, LogicGate> entry : canvas.getGateImageViews().entrySet()) {
                    ImageView otherImageView = entry.getKey();
                    if (otherImageView.getStyleClass().contains("selected")) {
                        LogicGate otherGate = entry.getValue();

                        double newX = otherGate.getImageView().getX() + deltaX;
                        double newY = otherGate.getImageView().getY() + deltaY;

                        double clampedX = Math.max(0,
                                Math.min(newX, canvas.getWidth() - otherImageView.getBoundsInLocal().getWidth()));
                        double clampedY = Math.max(0,
                                Math.min(newY, canvas.getHeight() - otherImageView.getBoundsInLocal().getHeight()));

                        otherGate.setPosition(clampedX, clampedY);
                        otherImageView.relocate(clampedX, clampedY);
                    }
                }

                for (TextLabel textLabel : canvas.getTextLabels()) {
                    if (textLabel.getStyleClass().contains("selected")) {
                        double newX = textLabel.getLayoutX() + deltaX;
                        double newY = textLabel.getLayoutY() + deltaY;

                        double clampedX = Math.max(0, Math.min(newX, canvas.getWidth() - textLabel.getWidth()));
                        double clampedY = Math.max(0, Math.min(newY, canvas.getHeight() - textLabel.getHeight()));

                        textLabel.setLayoutX(clampedX);
                        textLabel.setLayoutY(clampedY);
                    }
                }

                imageView.setUserData(new Object[] { event.getSceneX(), event.getSceneY(), gate });
                event.consume();
            }
        });

        imageView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double offsetX = event.getSceneX() - imageView.getLayoutX();
                double offsetY = event.getSceneY() - imageView.getLayoutY();
                if (!event.isControlDown() && !imageView.getStyleClass().contains("selected")) {
                    canvas.getSelectionManager().deselectAllGatesExcept(imageView);
                    canvas.getSelectionManager().deselectAllLabels();
                }
                imageView.getStyleClass().add("selected");
                imageView.setUserData(new Object[] { offsetX, offsetY, gate });
            }
            handleMousePressedForGate(imageView, gate, event);
        });

        imageView.setOnMouseReleased(event -> {
            if (imageView.getStyleClass().contains("selected")) {
                event.consume();
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getTarget() instanceof Pane) {
                canvas.getGateManager().deselectAllGates();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof ImageView)) {
                if (highlightedGate != null) {
                    highlightedGate.getImageView().getStyleClass().remove("selected");
                    highlightedGate = null;
                }
                if (canvas.getOpenContextMenu() != null) {
                    canvas.getOpenContextMenu().hide();
                    canvas.setOpenContextMenu(null);
                }
                canvas.requestFocus();
            }
        });
    }

    /**
     * Sets up drag handlers for a text label.
     * 
     * @param textLabel the text label
     */
    public void setupDragHandlersForLabel(TextLabel textLabel) {
        textLabel.setOnMousePressed(event -> {
            double offsetX = event.getSceneX() - textLabel.getLayoutX();
            double offsetY = event.getSceneY() - textLabel.getLayoutY();
            textLabel.setUserData(new double[] { offsetX, offsetY });

            if (!event.isControlDown() && !textLabel.getStyleClass().contains("selected")) {
                canvas.getSelectionManager().deselectAllLabelsExcept(textLabel);
            }
            if (!textLabel.getStyleClass().contains("selected")) {
                textLabel.getStyleClass().add("selected");
            }

        });

        textLabel.setOnMouseDragged(dragEvent -> {
            if (dragEvent.getButton() == MouseButton.PRIMARY && textLabel.getStyleClass().contains("selected")) {
                double[] userData = (double[]) textLabel.getUserData();
                double offsetX = userData[0];
                double offsetY = userData[1];

                double newX = dragEvent.getSceneX() - offsetX;
                double newY = dragEvent.getSceneY() - offsetY;

                double deltaX = newX - textLabel.getLayoutX();
                double deltaY = newY - textLabel.getLayoutY();

                newX = Math.max(0, Math.min(newX, canvas.getWidth() - textLabel.getWidth()));
                newY = Math.max(0, Math.min(newY, canvas.getHeight() - textLabel.getHeight()));

                textLabel.setLayoutX(newX);
                textLabel.setLayoutY(newY);

                // Move all selected gates
                for (Map.Entry<ImageView, LogicGate> entry : canvas.getGateImageViews().entrySet()) {
                    ImageView otherImageView = entry.getKey();
                    if (otherImageView.getStyleClass().contains("selected")) {
                        LogicGate otherGate = entry.getValue();

                        newX = otherGate.getImageView().getX() + deltaX;
                        newY = otherGate.getImageView().getY() + deltaY;

                        double clampedX = Math.max(0, Math.min(
                                newX,
                                canvas.getWidth() - otherImageView.getBoundsInLocal().getWidth()));
                        double clampedY = Math.max(0, Math.min(
                                newY,
                                canvas.getHeight() - otherImageView.getBoundsInLocal().getHeight()));

                        otherGate.setPosition(clampedX, clampedY);
                        otherImageView.relocate(clampedX, clampedY);
                    }
                }

                // Move all selected text labels (except the current one)
                for (TextLabel otherLabel : canvas.getTextLabels()) {
                    if (otherLabel != textLabel && otherLabel.getStyleClass().contains("selected")) {
                        double newLabelX = otherLabel.getLayoutX() + deltaX;
                        double newLabelY = otherLabel.getLayoutY() + deltaY;

                        double clampedX = Math.max(0,
                                Math.min(newLabelX, canvas.getWidth() - otherLabel.getWidth()));
                        double clampedY = Math.max(0,
                                Math.min(newLabelY, canvas.getHeight() - otherLabel.getHeight()));

                        otherLabel.setLayoutX(clampedX);
                        otherLabel.setLayoutY(clampedY);
                    }
                }

                dragEvent.consume();
            }
        });

        textLabel.setOnMouseReleased(event -> {
            if (textLabel.getStyleClass().contains("selected")) {
                event.consume();
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getTarget() instanceof Pane) {
                canvas.getSelectionManager().deselectAllLabels();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof TextLabel)) {
                if (canvas.getOpenContextMenu() != null) {
                    canvas.getOpenContextMenu().hide();
                    canvas.setOpenContextMenu(null);
                }
                canvas.requestFocus();
            }
        });
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
            return;
        } else if (event.getTarget() instanceof ImageView) {
            ImageView clickedImageView = (ImageView) event.getTarget();
            if (highlightedGate != null
                    && !highlightedGate.getImageView().equals(clickedImageView)) {
                canvas.getGateManager().deselectAllGates();
                LogicGate clickedGate = canvas.getGateImageViews().get(clickedImageView);
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
                canvas.getInteractionManager().setupConnectionHandlers();
                event.consume();
            }
        });
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
