package com.paperturtle.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.components.GateFactory;
import com.paperturtle.components.LogicGate;
import com.paperturtle.data.ClipboardData;

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
        double offsetX = 30;
        double offsetY = 30;

        for (ClipboardData data : clipboard) {
            LogicGate gate = GateFactory.createGate(canvas.normalizeType(data.getType()));
            if (gate == null) {
                System.out.println("Unable to create gate of type: " + data.getType());
                continue;
            }

            double newX = data.getPosition().getX() + offsetX;
            double newY = data.getPosition().getY() + offsetY;

            gate.setPosition(newX, newY);
            gate.setId(data.getId());
            createdGates.put(data.getId(), gate);
            canvas.drawGate(gate, newX, newY);
            gate.getImageView().getStyleClass().add("selected");
        }

        for (ClipboardData data : clipboard) {
            LogicGate sourceGate = createdGates.get(data.getId());
            if (sourceGate == null)
                continue;

            for (ClipboardData.ConnectionData output : data.getOutputs()) {
                LogicGate targetGate = createdGates.get(output.gateId);
                if (targetGate == null) {
                    System.out.println("Output gate not found for ID: " + output.gateId);
                    continue;
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
            }
        }
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
