package com.paperturtle.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paperturtle.components.GateFactory;
import com.paperturtle.components.LogicGate;
import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.data.ClipboardData;
import com.paperturtle.gui.CircuitCanvas;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Command to paste logic gates and their connections as well as text labels
 * from the clipboard onto
 * the circuit canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class PasteComponentsCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The labels data from the clipboard to be pasted.
     */
    private List<TextLabel> clipboardLabelsData;

    /**
     * The gates data from the clipboard to be pasted.
     */
    private List<ClipboardData> clipboardGatesData;

    /**
     * The list of logic gates that have been pasted.
     */
    private List<TextLabel> pastedLabels = new ArrayList<>();

    /**
     * The list of logic gates that have been pasted.
     */
    private List<LogicGate> pastedGates = new ArrayList<>();

    /**
     * The list of connections that have been pasted.
     */
    private List<Line> pastedConnections = new ArrayList<>();

    /**
     * The x-coordinate offset for pasting.
     */
    private double offsetX;

    /**
     * The y-coordinate offset for pasting.
     */
    private double offsetY;

    /**
     * The global x-coordinate offset for pasting.
     */
    private static double globalOffsetX = 0;

    /**
     * The global y-coordinate offset for pasting.
     */
    private static double globalOffsetY = 0;

    /**
     * Constructs an PasteComponentsCommand with the specified parameters.
     * 
     * @param canvas the circuit canvas
     */
    public PasteComponentsCommand(CircuitCanvas canvas, List<ClipboardData> clipboardGatesData,
            List<TextLabel> clipboardLabelsData, double offsetX, double offsetY) {
        this.canvas = canvas;
        this.clipboardLabelsData = clipboardLabelsData;
        this.clipboardGatesData = clipboardGatesData;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

    }

    /**
     * Executes the command to add logic gates and text labels to the canvas at
     * the specified coordinates.
     */
    @Override
    public void execute() {
        canvas.getGateManager().deselectAllGates();
        Map<String, LogicGate> createdGates = new HashMap<>();
        Map<String, String> oldToNewIdMap = new HashMap<>();

        clipboardGatesData.forEach(data -> {
            LogicGate gate = GateFactory.createGate(canvas.normalizeType(data.getType()));
            if (gate != null) {
                double newX = data.getPosition().getX() + offsetX + globalOffsetX;
                double newY = data.getPosition().getY() + offsetY + globalOffsetY;

                String uniqueId = data.getId() + "_" + System.nanoTime();
                oldToNewIdMap.put(data.getId(), uniqueId);

                gate.setPosition(newX, newY);
                gate.setId(uniqueId);
                gate.setMaxOutputConnections(data.getMaxOutputConnections());
                createdGates.put(uniqueId, gate);
                canvas.drawGate(gate, newX, newY);
                gate.getImageView().getStyleClass().add("selected");
                pastedGates.add(gate);

            } else {
                System.out.println("Unable to create gate of type: " + data.getType());
            }
        });

        clipboardGatesData.forEach(data -> {
            String newSourceId = oldToNewIdMap.get(data.getId());
            LogicGate sourceGate = createdGates.get(newSourceId);
            if (sourceGate != null) {
                data.getOutputs().forEach(output -> {
                    String oldTargetId = output.getGateId();
                    String newTargetId = oldToNewIdMap.get(oldTargetId);
                    if (newTargetId != null) {
                        ClipboardData.ConnectionData newOutput = new ClipboardData.ConnectionData(newTargetId,
                                output.getPointIndex());
                        createConnection(createdGates, sourceGate, newOutput);
                    } else {
                        System.out.println("Target gate not found for old ID: " + oldTargetId);
                    }
                });
            }
        });

        canvas.getSelectionManager().deselectAllLabels();
        createLabels();
        incrementGlobalOffset();
    }

    /**
     * Undoes the command by removing logic gates and text labels from the canvas.
     */
    @Override
    public void undo() {
        removeConnections();
        removeGates();
        removeLabels();
        decrementGlobalOffset();
    }

    /**
     * Creates logic gates from the clipboard data and adds them to the canvas.
     */
    private void createLabels() {
        clipboardLabelsData.forEach(originalLabel -> {
            // Create a new TextLabel instance with the same properties as the original
            TextLabel newLabel = new TextLabel(originalLabel.getLabel(), originalLabel.getWidth(),
                    originalLabel.getHeight());
            double newX = originalLabel.getLayoutX() + offsetX + globalOffsetX;
            double newY = originalLabel.getLayoutY() + offsetY + globalOffsetY;

            newLabel.setLayoutX(newX);
            newLabel.setLayoutY(newY);
            newLabel.setFillColor((Color) originalLabel.getFillColor());
            newLabel.setBackgroundColor((Color) originalLabel.getBackgroundColor());
            newLabel.setFontFamily(originalLabel.getFontFamily());
            newLabel.setFontSize(originalLabel.getFontSize());
            newLabel.setUnderline(originalLabel.isUnderline());
            newLabel.setStrikethrough(originalLabel.isStrikethrough());
            newLabel.setAutoSize(originalLabel.isAutoSize());
            newLabel.setFont(Font.font(originalLabel.getFontFamily(),
                    originalLabel.getFontWeight() == FontWeight.BOLD ? FontWeight.BOLD : FontWeight.NORMAL,
                    originalLabel.getFontPosture() == FontPosture.ITALIC ? FontPosture.ITALIC : FontPosture.REGULAR,
                    originalLabel.getFontSize()));

            canvas.drawTextLabel(newLabel, newX, newY);
            newLabel.getStyleClass().add("selected");
            pastedLabels.add(newLabel);
        });
    }

    /**
     * Creates logic gates from the clipboard data and adds them to the canvas.
     * 
     * @return a map of the created gates, with the gate ID as the key
     */
    private Map<String, LogicGate> createGates() {
        Map<String, LogicGate> createdGates = new HashMap<>();
        clipboardGatesData.forEach(data -> {
            LogicGate gate = GateFactory.createGate(canvas.normalizeType(data.getType()));
            if (gate != null) {
                double newX = data.getPosition().getX() + offsetX + globalOffsetX;
                double newY = data.getPosition().getY() + offsetY + globalOffsetY;

                gate.setPosition(newX, newY);
                gate.setId(data.getId());
                gate.setMaxOutputConnections(data.getMaxOutputConnections());
                createdGates.put(data.getId(), gate);
                canvas.drawGate(gate, newX, newY);
                gate.getImageView().getStyleClass().add("selected");
                pastedGates.add(gate);

            } else {
                System.out.println("Unable to create gate of type: " + data.getType());
            }
        });
        return createdGates;
    }

    /**
     * Creates a connection between two logic gates based on the provided output
     * 
     * @param createdGates the map of created gates
     * @param sourceGate   the source gate of the connection
     * @param output       the output data for the connection
     */
    private void createConnection(Map<String, LogicGate> createdGates, LogicGate sourceGate,
            ClipboardData.ConnectionData output) {
        LogicGate targetGate = createdGates.get(output.getGateId());
        if (targetGate == null) {
            System.out.println("Output gate not found for ID: " + output.getGateId());
            return;
        }

        Point2D sourcePos = sourceGate.getOutputMarker().localToParent(sourceGate.getOutputMarker().getCenterX(),
                sourceGate.getOutputMarker().getCenterY());
        Point2D targetPos = targetGate.getInputMarkers().get(output.getPointIndex()).localToParent(
                targetGate.getInputMarkers().get(output.getPointIndex()).getCenterX(),
                targetGate.getInputMarkers().get(output.getPointIndex()).getCenterY());

        Line connectionLine = new Line(sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
        connectionLine.setStrokeWidth(3.5);
        connectionLine.setStroke(Color.BLACK);

        if (!canvas.getChildren().contains(connectionLine)) {
            canvas.getChildren().add(connectionLine);
        }
        sourceGate.addOutputConnection(connectionLine);
        targetGate.addInputConnection(connectionLine, output.getPointIndex());
        if (!pastedConnections.contains(connectionLine)) {
            pastedConnections.add(connectionLine);
        }
        sourceGate.addOutputGate(targetGate);
        targetGate.addInput(sourceGate);
        canvas.getLineToStartGateMap().put(connectionLine, sourceGate);

        System.out.println("Created connection from " + sourceGate.getId() + " to " + targetGate.getId()
                + " at points (" + sourcePos.getX() + ", " + sourcePos.getY() + ") to (" + targetPos.getX() + ", "
                + targetPos.getY() + ")");
    }

    /**
     * Removes the pasted connections from the canvas.
     */
    private void removeConnections() {
        pastedConnections.forEach(connection -> {
            canvas.getChildren().remove(connection);
            LogicGate sourceGate = canvas.getLineToStartGateMap().get(connection);
            LogicGate targetGate = canvas.getGateManager().findTargetGate(connection);
            if (sourceGate != null && targetGate != null) {
                int index = targetGate.findInputConnectionIndex(connection);
                if (index != -1) {
                    sourceGate.removeOutputConnection(connection);
                    targetGate.removeInputConnection(connection, index);
                    targetGate.removeInput(sourceGate);
                }
            }
            canvas.getLineToStartGateMap().remove(connection);
        });
        pastedConnections.clear();
    }

    /**
     * Removes the pasted gates from the canvas.
     */
    private void removeGates() {
        pastedGates.forEach(gate -> canvas.getGateManager().removeGate(gate.getImageView()));
        pastedGates.clear();
    }

    /**
     * Removes the pasted labels from the canvas.
     */
    private void removeLabels() {
        pastedLabels.forEach(label -> canvas.removeTextLabel(label));
        pastedLabels.clear();
    }

    /**
     * Increments the global offset for pasting gates and connections.
     */
    private void incrementGlobalOffset() {
        globalOffsetX += 30;
        globalOffsetY += 30;
    }

    /**
     * Decrements the global offset for pasting gates and connections.
     */
    private void decrementGlobalOffset() {
        globalOffsetX -= 30;
        globalOffsetY -= 30;
    }

    /**
     * Set global offset to 0.
     */
    public static void resetGlobalOffset() {
        globalOffsetX = 0;
        globalOffsetY = 0;
    }
}
