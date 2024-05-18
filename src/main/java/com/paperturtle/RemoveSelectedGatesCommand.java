package com.paperturtle;

import javafx.geometry.Point2D;
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
    private List<LogicGate> sourceGatesForConnections = new ArrayList<>();
    private List<LogicGate> targetGatesForConnections = new ArrayList<>();
    private List<Integer> inputIndices = new ArrayList<>();

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
                for (Line connection : gate.getOutputConnections()) {
                    removedConnections.add(connection);
                    sourceGatesForConnections.add(gate);
                    LogicGate targetGate = canvas.getGateManager().findTargetGate(connection);
                    targetGatesForConnections.add(targetGate);
                    inputIndices.add(targetGate != null ? targetGate.findInputConnectionIndex(connection) : -1);
                }
                for (List<Line> inputConnectionList : gate.getInputConnections()) {
                    for (Line connection : inputConnectionList) {
                        removedConnections.add(connection);
                        LogicGate sourceGate = canvas.getLineToStartGateMap().get(connection);
                        sourceGatesForConnections.add(sourceGate);
                        targetGatesForConnections.add(gate);
                        inputIndices.add(gate != null ? gate.findInputConnectionIndex(connection) : -1);
                    }
                }
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

            if (gate instanceof Lightbulb) {
                System.out.println(gate.getInputMarkers());
                System.out.println(gate.getOutputMarker());
                break;
            }
        }

        for (int i = 0; i < removedConnections.size(); i++) {
            Line connection = removedConnections.get(i);
            LogicGate sourceGate = sourceGatesForConnections.get(i);
            LogicGate targetGate = targetGatesForConnections.get(i);
            int inputIndex = inputIndices.get(i);

            if (sourceGate != null) {
                sourceGate.addOutputConnection(connection);
            }

            if (targetGate != null && inputIndex != -1) {
                Point2D sourcePos = sourceGate.getOutputMarker().localToParent(
                        sourceGate.getOutputMarker().getCenterX(), sourceGate.getOutputMarker().getCenterY());
                Point2D targetPos = targetGate.getInputMarkers().get(inputIndex).localToParent(
                        targetGate.getInputMarkers().get(inputIndex).getCenterX(),
                        targetGate.getInputMarkers().get(inputIndex).getCenterY());

                connection.setStartX(sourcePos.getX());
                connection.setStartY(sourcePos.getY());
                connection.setEndX(targetPos.getX());
                connection.setEndY(targetPos.getY());

                targetGate.addInputConnection(connection, inputIndex);
                targetGate.addInput(sourceGate);
            }

            if (!canvas.getChildren().contains(connection)) {
                canvas.getChildren().add(connection);
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
        sourceGatesForConnections.clear();
        targetGatesForConnections.clear();
        inputIndices.clear();
    }
}
