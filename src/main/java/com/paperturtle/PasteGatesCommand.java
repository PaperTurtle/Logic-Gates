package com.paperturtle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class PasteGatesCommand implements Command {
    private CircuitCanvas canvas;
    private List<ClipboardData> clipboardData;
    private List<LogicGate> pastedGates = new ArrayList<>();
    private List<Line> pastedConnections = new ArrayList<>();
    private double offsetX;
    private double offsetY;

    public PasteGatesCommand(CircuitCanvas canvas, List<ClipboardData> clipboardData, double offsetX, double offsetY) {
        this.canvas = canvas;
        this.clipboardData = clipboardData;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void execute() {
        Map<String, LogicGate> createdGates = new HashMap<>();

        for (ClipboardData data : clipboardData) {
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
            if (!gate.getImageView().getStyleClass().contains("selected")) {
                gate.getImageView().getStyleClass().add("selected");
            }
            pastedGates.add(gate);
        }

        for (ClipboardData data : clipboardData) {
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
        }
    }

    @Override
    public void undo() {
        for (Line connection : pastedConnections) {
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
        }

        for (LogicGate gate : pastedGates) {
            canvas.getGateManager().removeGate(gate.getImageView());
        }
    }
}
