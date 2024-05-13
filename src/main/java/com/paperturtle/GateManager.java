package com.paperturtle;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class GateManager {
    private CircuitCanvas canvas;

    public GateManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

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
            logicGate.inputs.forEach(inputGate -> {
                inputGate.getOutputGates().remove(logicGate);
                inputGate.evaluate();
                inputGate.propagateStateChange();
            });

            logicGate.outputGates.forEach(outputGate -> {
                outputGate.inputs.remove(logicGate);
                outputGate.evaluate();
                outputGate.propagateStateChange();
            });
            logicGate.getInputMarkers().clear();

        }
        canvas.propagateUpdates();
    }

    public void deselectAllGates() {
        canvas.getGateImageViews().values().forEach(gate -> {
            gate.getImageView().getStyleClass().remove("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(false);
            }
        });
        canvas.getInteractionManager().setHighlightedGate(null);
    }

    public void removeSelectedGates() {
        List<ImageView> selectedGates = canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        selectedGates.forEach(canvas.getGateManager()::removeGate);
    }

    public LogicGate findGateForInputMarker(Circle inputMarker) {
        for (Map.Entry<ImageView, LogicGate> entry : canvas.getGateImageViews().entrySet()) {
            LogicGate gate = entry.getValue();
            if (gate.getInputMarkers().contains(inputMarker)) {
                return gate;
            }
        }
        return null;
    }

    public int findInputMarkerIndex(LogicGate gate, Circle inputMarker) {
        return gate.getInputMarkers().indexOf(inputMarker);
    }

    public LogicGate findTargetGate(Line connection) {
        for (LogicGate gate : canvas.getGateImageViews().values()) {
            if (gate.getInputConnections().stream().anyMatch(list -> list.contains(connection))) {
                return gate;
            }
        }
        return null;
    }
}
