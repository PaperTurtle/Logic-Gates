package com.paperturtle.managers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.paperturtle.components.LogicGate;
import com.paperturtle.components.SwitchGate;
import com.paperturtle.components.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * The GateManager class is responsible for managing logic gates in the circuit
 * canvas.
 * 
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class GateManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * Constructs a GateManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public GateManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Removes a logic gate from the circuit canvas.
     * 
     * @param gate the ImageView of the gate to remove
     */
    public void removeGate(ImageView gate) {
        LogicGate logicGate = canvas.getGateImageViews().get(gate);
        if (logicGate != null) {
            canvas.getConnectionManager().removeAllConnections(logicGate);
            canvas.getChildren().removeAll(logicGate.getInputMarkers());
            if (logicGate.getOutputMarker() != null) {
                canvas.getChildren().remove(logicGate.getOutputMarker());
            }
            canvas.getChildren().remove(gate);
            canvas.getGateImageViews().remove(gate);
            canvas.getGateMarkers().remove(gate);

            logicGate.getInputs().forEach(inputGate -> {
                inputGate.getOutputGates().remove(logicGate);
                inputGate.evaluate();
                inputGate.propagateStateChange();
            });

            logicGate.getOutputGates().forEach(outputGate -> {
                outputGate.getInputs().remove(logicGate);
                outputGate.evaluate();
                outputGate.propagateStateChange();
            });

            logicGate.getInputMarkers().clear();
        }
        canvas.propagateUpdates();
    }

    /**
     * Deselects all gates in the circuit canvas.
     */
    public void deselectAllGates() {
        canvas.getGateImageViews().values().forEach(gate -> {
            gate.getImageView().getStyleClass().remove("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(false);
            }
        });
        canvas.getInteractionManager().setHighlightedGate(null);
    }

    /**
     * Removes all selected gates from the circuit canvas.
     */
    public void removeSelectedGates() {
        List<ImageView> selectedGates = canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<TextLabel> selectedLabels = canvas.getTextLabels().stream()
                .filter(entry -> entry.getStyleClass().contains("selected")).collect(Collectors.toList());

        selectedGates.forEach(canvas.getGateManager()::removeGate);
        selectedLabels.forEach(label -> label.removeSelf());
    }

    /**
     * Finds the logic gate for the specified input marker.
     * 
     * @param inputMarker the input marker to find the gate for
     * @return the logic gate that corresponds to the input marker, or null if not
     *         found
     */
    public LogicGate findGateForInputMarker(Circle inputMarker) {
        return canvas.getGateImageViews().values().stream()
                .filter(gate -> gate.getInputMarkers().contains(inputMarker))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds the index of the specified input marker in the logic gate's input
     * markers.
     * 
     * @param gate        the logic gate
     * @param inputMarker the input marker to find the index for
     * @return the index of the input marker, or -1 if not found
     */
    public int findInputMarkerIndex(LogicGate gate, Circle inputMarker) {
        return gate.getInputMarkers().indexOf(inputMarker);
    }

    /**
     * Finds the target gate for the specified connection line.
     * 
     * @param connection the connection line to find the target gate for
     * @return the target gate that corresponds to the connection line, or null if
     *         not found
     */
    public LogicGate findTargetGate(Line connection) {
        return canvas.getGateImageViews().values().stream()
                .filter(gate -> gate.getInputConnections().stream()
                        .anyMatch(list -> list.contains(connection)))
                .findFirst()
                .orElse(null);
    }
}
