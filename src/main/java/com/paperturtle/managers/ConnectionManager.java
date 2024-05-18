package com.paperturtle.managers;

import java.util.ArrayList;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.commands.AddConnectionCommand;
import com.paperturtle.commands.RemoveConnectionCommand;
import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * The ConnectionManager class is responsible for managing connections between
 * logic gates in the circuit canvas.
 * 
 * @see CircuitCanvas
 * @see AddConnectionCommand
 * @see RemoveConnectionCommand
 * @see LogicGate
 * @see Line
 * @see Circle
 * 
 * @see Point2D
 * @see Node
 * @see CommandManager
 * 
 * @author Seweryn Czabanowski
 */
public class ConnectionManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The logic gate where the connection starts.
     */
    private LogicGate startGate;

    /**
     * Constructs a ConnectionManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public ConnectionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Removes a connection line from the circuit canvas.
     * 
     * @param connection the connection line to remove
     */
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

    /**
     * Removes all connections for the specified logic gate.
     * 
     * @param logicGate the logic gate whose connections are to be removed
     */
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

    /**
     * Finalizes a connection between logic gates.
     * 
     * @param x            the x-coordinate of the end point
     * @param y            the y-coordinate of the end point
     * @param outputMarker the output marker of the source gate
     * @return true if the connection is successfully finalized, false otherwise
     */
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

    /**
     * Sets the start gate for the connection.
     * 
     * @param startGate the start gate to set
     */
    public void setStartGate(LogicGate startGate) {
        this.startGate = startGate;
    }

    /**
     * Gets the start gate for the connection.
     * 
     * @return the start gate
     */
    public LogicGate getStartGate() {
        return startGate;
    }
}
