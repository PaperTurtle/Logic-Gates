package com.paperturtle;

import java.util.ArrayList;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class ConnectionManager {
    private CircuitCanvas canvas;
    private LogicGate startGate;

    public ConnectionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    public void removeConnection(Line connection) {
        LogicGate sourceGate = canvas.getLineToStartGateMap().get(connection);
        if (sourceGate != null) {
            sourceGate.removeOutputConnection(connection);

            LogicGate targetGate = canvas.getGateManager().findTargetGate(connection);
            if (targetGate != null) {
                int index = targetGate.findInputConnectionIndex(connection);
                if (index != -1) {
                    targetGate.removeInputConnection(connection, index);
                    targetGate.removeInput(sourceGate);
                }

                targetGate.evaluate();
                targetGate.propagateStateChange();
                canvas.scheduleUpdate(targetGate);
            }

            canvas.getChildren().remove(connection);
            canvas.getLineToStartGateMap().remove(connection);

            sourceGate.evaluate();
            sourceGate.propagateStateChange();
            canvas.scheduleUpdate(sourceGate);
        } else {
            System.out.println("No source gate found for the connection.");
        }
    }

    public void removeAllConnections(LogicGate logicGate) {
        new ArrayList<>(logicGate.getOutputConnections()).forEach(line -> {
            LogicGate targetGate = canvas.getLineToStartGateMap().get(line);
            if (targetGate != null) {
                targetGate.removeInput(logicGate);
                targetGate.evaluate();
                targetGate.propagateStateChange();
            }
            canvas.getLineToStartGateMap().remove(line);
            canvas.getChildren().remove(line);
        });

        logicGate.getInputConnections().forEach(connections -> new ArrayList<>(connections).forEach(line -> {
            LogicGate sourceGate = canvas.getLineToStartGateMap().get(line);
            if (sourceGate != null) {
                sourceGate.getOutputConnections().remove(line);
                sourceGate.evaluate();
                sourceGate.propagateStateChange();
            }
            canvas.getLineToStartGateMap().remove(line);
            canvas.getChildren().remove(line);
        }));
    }

    public boolean finalizeConnection(double x, double y, Circle outputMarker) {
        for (Node node : canvas.getChildren()) {
            if (node instanceof Circle && node != outputMarker) {
                Circle inputMarker = (Circle) node;
                if (inputMarker.contains(x, y) && inputMarker.getOpacity() == 1.0) {
                    Point2D inputPos = inputMarker.localToParent(inputMarker.getCenterX(), inputMarker.getCenterY());
                    canvas.getCurrentLine().setEndX(inputPos.getX());
                    canvas.getCurrentLine().setEndY(inputPos.getY());

                    LogicGate targetGate = canvas.getGateManager().findGateForInputMarker(inputMarker);
                    LogicGate sourceGate = canvas.getLineToStartGateMap().get(canvas.getCurrentLine());
                    if (targetGate != null && sourceGate != null && targetGate != sourceGate) {
                        int inputIndex = canvas.getGateManager().findInputMarkerIndex(targetGate, inputMarker);
                        targetGate.addInputConnection(canvas.getCurrentLine(), inputIndex);
                        targetGate.addInput(sourceGate);
                        sourceGate.addOutputConnection(canvas.getCurrentLine());
                        sourceGate.addOutputGate(targetGate);
                        targetGate.evaluate();
                        targetGate.propagateStateChange();
                        sourceGate.updateOutputConnectionsColor(sourceGate.evaluate());
                        canvas.scheduleUpdate(targetGate);
                        startGate = null;
                    } else {
                        if (canvas.getCurrentLine() != null && startGate != null) {
                            canvas.getChildren().remove(canvas.getCurrentLine());
                            startGate.removeOutputConnection(canvas.getCurrentLine());
                            canvas.setCurrentLine(null);
                        }
                        return false;
                    }

                    return true;
                }
            }
        }
        if (canvas.getCurrentLine() != null && startGate != null) {
            canvas.getChildren().remove(canvas.getCurrentLine());
            startGate.removeOutputConnection(canvas.getCurrentLine());
            canvas.setCurrentLine(null);
        }
        return false;
    }

    public void setStartGate(LogicGate startGate) {
        this.startGate = startGate;
    }

    public LogicGate getStartGate() {
        return startGate;
    }
}
