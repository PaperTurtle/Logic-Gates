package com.paperturtle;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoveSelectedGatesCommand implements Command {
    private CircuitCanvas canvas;
    private List<LogicGate> removedGates = new ArrayList<>();
    private List<Line> removedConnections = new ArrayList<>();
    private List<TextLabel> removedLabels = new ArrayList<>();

    public RemoveSelectedGatesCommand(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void execute() {
        List<ImageView> selectedGates = canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<TextLabel> selectedLabels = canvas.getTextLabels().stream()
                .filter(entry -> entry.getStyleClass().contains("selected")).collect(Collectors.toList());

        for (ImageView imageView : selectedGates) {
            LogicGate gate = canvas.getGate(imageView);
            if (gate != null) {
                removedGates.add(gate);
                removedConnections.addAll(gate.getOutputConnections());
                removedConnections
                        .addAll(gate.getInputConnections().stream().flatMap(List::stream).collect(Collectors.toList()));
                canvas.getGateManager().removeGate(imageView);
            }
        }
        for (TextLabel label : selectedLabels) {
            removedLabels.add(label);
            label.removeSelf();
        }
    }

    @Override
    public void undo() {
        for (LogicGate gate : removedGates) {
            canvas.drawGate(gate, gate.getPosition().getX(), gate.getPosition().getY());
            if (!gate.getImageView().getStyleClass().contains("selected")) {
                gate.getImageView().getStyleClass().add("selected");
            }
        }
        for (Line connection : removedConnections) {
            if (!canvas.getChildren().contains(connection)) {
                canvas.getChildren().add(connection);
            }
            LogicGate sourceGate = canvas.getLineToStartGateMap().get(connection);
            LogicGate targetGate = canvas.getGateManager().findTargetGate(connection);
            if (sourceGate != null && targetGate != null) {
                int index = targetGate.findInputConnectionIndex(connection);
                if (index != -1) {
                    sourceGate.addOutputConnection(connection);
                    targetGate.addInputConnection(connection, index);
                    targetGate.addInput(sourceGate);
                }
            }
            canvas.getLineToStartGateMap().put(connection, sourceGate);
        }
        for (TextLabel label : removedLabels) {
            canvas.drawTextLabel(label, label.getLayoutX(), label.getLayoutY());
            if (!label.getStyleClass().contains("selected")) {
                label.getStyleClass().add("selected");
            }
        }
        removedGates.clear();
        removedConnections.clear();
        removedLabels.clear();
    }
}
