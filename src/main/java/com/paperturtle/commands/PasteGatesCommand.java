package com.paperturtle.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paperturtle.components.GateFactory;
import com.paperturtle.components.LogicGate;
import com.paperturtle.data.ClipboardData;
import com.paperturtle.gui.CircuitCanvas;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Command to paste logic gates and their connections from the clipboard onto
 * the circuit canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class PasteGatesCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The data from the clipboard to be pasted.
     */
    private List<ClipboardData> clipboardData;

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
     * Constructs a PasteGatesCommand with the specified parameters.
     * 
     * @param canvas        the circuit canvas to which gates are pasted
     * @param clipboardData the clipboard data representing gates and connections to
     *                      be pasted
     * @param offsetX       the x offset for pasting the gates
     * @param offsetY       the y offset for pasting the gates
     */
    public PasteGatesCommand(CircuitCanvas canvas, List<ClipboardData> clipboardData, double offsetX, double offsetY) {
        this.canvas = canvas;
        this.clipboardData = clipboardData;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Executes the command to paste logic gates and their connections onto the
     * canvas.
     * The gates and connections are positioned according to the provided offsets.
     */
    @Override
    public void execute() {
        canvas.getGateManager().deselectAllGates();
        Map<String, LogicGate> createdGates = createGates();

        for (ClipboardData data : clipboardData) {
            LogicGate sourceGate = createdGates.get(data.getId());
            if (sourceGate != null) {
                for (ClipboardData.ConnectionData output : data.getOutputs()) {
                    createConnection(createdGates, sourceGate, output);
                }
            }
        }

        incrementGlobalOffset();
    }

    /**
     * Undoes the command by removing the pasted gates and connections from the
     * canvas.
     */
    @Override
    public void undo() {
        removeConnections();
        removeGates();
        decrementGlobalOffset();
    }

    /**
     * Creates logic gates from the clipboard data and adds them to the canvas.
     * 
     * @return a map of the created gates, with the gate ID as the key
     */
    private Map<String, LogicGate> createGates() {
        Map<String, LogicGate> createdGates = new HashMap<>();
        clipboardData.forEach(data -> {
            LogicGate gate = GateFactory.createGate(canvas.normalizeType(data.getType()));
            if (gate != null) {
                double newX = data.getPosition().getX() + offsetX + globalOffsetX;
                double newY = data.getPosition().getY() + offsetY + globalOffsetY;

                gate.setPosition(newX, newY);
                gate.setId(data.getId());
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
        LogicGate targetGate = createdGates.get(output.gateId);
        if (targetGate == null) {
            System.out.println("Output gate not found for ID: " + output.gateId);
            return;
        }

        Point2D sourcePos = sourceGate.getOutputMarker().localToParent(sourceGate.getOutputMarker().getCenterX(),
                sourceGate.getOutputMarker().getCenterY());
        Point2D targetPos = targetGate.getInputMarkers().get(output.pointIndex).localToParent(
                targetGate.getInputMarkers().get(output.pointIndex).getCenterX(),
                targetGate.getInputMarkers().get(output.pointIndex).getCenterY());

        Line connectionLine = new Line(sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
        connectionLine.setStrokeWidth(3.5);
        connectionLine.setStroke(Color.BLACK);

        if (!canvas.getChildren().contains(connectionLine)) {
            canvas.getChildren().add(connectionLine);
        }
        sourceGate.addOutputConnection(connectionLine);
        targetGate.addInputConnection(connectionLine, output.pointIndex);
        if (!pastedConnections.contains(connectionLine)) {
            pastedConnections.add(connectionLine);
        }
        canvas.getLineToStartGateMap().put(connectionLine, sourceGate);
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
}
