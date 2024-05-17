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
            LogicGate targetGate = canvas.getGateManager().findTargetGate(connection);
            if (targetGate != null) {
                int index = targetGate.findInputConnectionIndex(connection);
                if (index != -1) {
                    canvas.getCommandManager().executeCommand(
                            new RemoveConnectionCommand(canvas, sourceGate, targetGate, connection, index));
                }
            } else {
                System.out.println("No target gate found for the connection.");
            }
        } else {
            System.out.println("No source gate found for the connection.");
        }
    }

    public void removeAllConnections(LogicGate logicGate) {
        new ArrayList<>(logicGate.getOutputConnections()).forEach(line -> {
            LogicGate targetGate = canvas.getLineToStartGateMap().get(line);
            if (targetGate != null) {
                int index = targetGate.findInputConnectionIndex(line);
                if (index != -1) {
                    canvas.getCommandManager()
                            .executeCommand(new RemoveConnectionCommand(canvas, logicGate, targetGate, line, index));
                }
            }
        });

        logicGate.getInputConnections().forEach(connections -> new ArrayList<>(connections).forEach(line -> {
            LogicGate sourceGate = canvas.getLineToStartGateMap().get(line);
            if (sourceGate != null) {
                int index = logicGate.findInputConnectionIndex(line);
                if (index != -1) {
                    canvas.getCommandManager()
                            .executeCommand(new RemoveConnectionCommand(canvas, sourceGate, logicGate, line, index));
                }
            }
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

                        if (!targetGate.getInputConnections(inputIndex).isEmpty()) {
                            System.out.println("Input marker already has a connection.");
                            canvas.getChildren().remove(canvas.getCurrentLine());
                            startGate.removeOutputConnection(canvas.getCurrentLine());
                            canvas.setCurrentLine(null);
                            return false;
                        }

                        Line connectionLine = canvas.getCurrentLine();
                        canvas.setCurrentLine(null);
                        canvas.getCommandManager().executeCommand(
                                new AddConnectionCommand(canvas, sourceGate, targetGate, connectionLine, inputIndex));

                        return true;
                    } else {
                        if (canvas.getCurrentLine() != null && startGate != null) {
                            canvas.getChildren().remove(canvas.getCurrentLine());
                            startGate.removeOutputConnection(canvas.getCurrentLine());
                            canvas.setCurrentLine(null);
                        }
                        return false;
                    }
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
