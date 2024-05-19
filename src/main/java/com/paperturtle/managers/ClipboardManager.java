package com.paperturtle.managers;

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
 * The ClipboardManager class is responsible for handling copy and paste
 * operations of logic gates in the circuit canvas.
 * 
 * @see ClipboardData
 * 
 * @author Seweryn Czabanowski
 */
public class ClipboardManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The list of clipboard data representing the copied elements.
     */
    private List<ClipboardData> clipboard = new ArrayList<>();

    /**
     * The x-offset for pasting the copied gates.
     */
    private static final double OFFSET_X = 30;

    /**
     * The y-offset for pasting the copied gates.
     */
    private static final double OFFSET_Y = 30;

    /**
     * Constructs a ClipboardManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public ClipboardManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Pastes the gates from the clipboard onto the circuit canvas.
     */
    public void pasteGatesFromClipboard() {
        canvas.getGateManager().deselectAllGates();
        Map<String, LogicGate> createdGates = new HashMap<>();

        clipboard.forEach(data -> {
            LogicGate gate = GateFactory.createGate(canvas.normalizeType(data.getType()));
            if (gate == null) {
                System.out.println("Unable to create gate of type: " + data.getType());
                return;
            }

            double newX = data.getPosition().getX() + OFFSET_X;
            double newY = data.getPosition().getY() + OFFSET_Y;

            gate.setPosition(newX, newY);
            gate.setId(data.getId());
            createdGates.put(data.getId(), gate);
            canvas.drawGate(gate, newX, newY);
            gate.getImageView().getStyleClass().add("selected");
        });

        clipboard.forEach(data -> createConnections(data, createdGates));
    }

    /**
     * Creates the connections between the gates based on the clipboard data.
     * 
     * @param data         the clipboard data
     * @param createdGates the map of created gates
     */
    private void createConnections(ClipboardData data, Map<String, LogicGate> createdGates) {
        LogicGate sourceGate = createdGates.get(data.getId());
        if (sourceGate == null)
            return;

        data.getOutputs().forEach(output -> {
            LogicGate targetGate = createdGates.get(output.gateId);
            if (targetGate == null) {
                System.out.println("Output gate not found for ID: " + output.gateId);
                return;
            }
            Point2D sourcePos = sourceGate.getOutputMarker().localToParent(
                    sourceGate.getOutputMarker().getCenterX(), sourceGate.getOutputMarker().getCenterY());
            Point2D targetPos = targetGate.getInputMarkers().get(output.pointIndex).localToParent(
                    targetGate.getInputMarkers().get(output.pointIndex).getCenterX(),
                    targetGate.getInputMarkers().get(output.pointIndex).getCenterY());

            Line connectionLine = new Line(sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
            connectionLine.setStrokeWidth(3.5);
            connectionLine.setStroke(Color.BLACK);

            canvas.getChildren().add(connectionLine);
            sourceGate.addOutputConnection(connectionLine);
            targetGate.addInputConnection(connectionLine, output.pointIndex);
        });
    }

    /**
     * Copies the selected gates to the clipboard.
     */
    public void copySelectedGatesToClipboard() {
        clipboard.clear();
        canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .forEach(entry -> {
                    LogicGate gate = entry.getValue();
                    clipboard.add(gate.getGateClipboardData());
                });
    }

    /**
     * Gets the clipboard data.
     * 
     * @return the list of ClipboardData
     */
    public List<ClipboardData> getClipboard() {
        return clipboard;
    }
}
