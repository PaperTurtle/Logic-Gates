package com.paperturtle.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.paperturtle.commands.AddConnectionCommand;
import com.paperturtle.commands.RemoveConnectionCommand;
import com.paperturtle.components.LogicGate;
import com.paperturtle.gui.CircuitCanvas;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * The ConnectionManager class is responsible for managing connections between
 * logic gates in the circuit canvas.
 * 
 * @see LogicGate
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
        Optional.ofNullable(canvas.getLineToStartGateMap().get(connection))
                .ifPresentOrElse(sourceGate -> {
                    LogicGate targetGate = canvas.getGateManager().findTargetGate(connection);
                    if (targetGate != null) {
                        int index = targetGate.findInputConnectionIndex(connection);
                        if (index != -1) {
                            canvas.getCommandManager().executeCommand(
                                    new RemoveConnectionCommand(canvas, sourceGate, targetGate, connection, index));
                        } else {
                            System.out.println("No input index found for the connection.");
                        }
                    } else {
                        System.out.println("No target gate found for the connection.");
                    }
                }, () -> System.out.println("No source gate found for the connection."));
    }

    /**
     * Removes all connections for the specified logic gate.
     * 
     * @param logicGate the logic gate whose connections are to be removed
     */
    public void removeAllConnections(LogicGate logicGate) {
        List<Line> outputConnections = new ArrayList<>(logicGate.getOutputConnections());
        for (Line line : outputConnections) {
            LogicGate targetGate = canvas.getGateManager().findTargetGate(line);
            if (targetGate != null) {
                int index = targetGate.findInputConnectionIndex(line);
                if (index != -1) {
                    targetGate.removeInputConnection(line, index);
                    targetGate.getInputs().remove(logicGate);
                    targetGate.evaluate();
                    targetGate.propagateStateChange();
                }
            }
            canvas.getLineToStartGateMap().remove(line);
            canvas.getChildren().remove(line);
        }

        List<List<Line>> inputConnections = logicGate.getInputConnections();
        for (List<Line> connections : inputConnections) {
            List<Line> connectionsCopy = new ArrayList<>(connections);
            for (Line line : connectionsCopy) {
                LogicGate sourceGate = canvas.getLineToStartGateMap().get(line);
                if (sourceGate != null) {
                    sourceGate.getOutputConnections().remove(line);
                    sourceGate.getOutputGates().remove(logicGate);
                    sourceGate.evaluate();
                    sourceGate.propagateStateChange();
                }
                canvas.getLineToStartGateMap().remove(line);
                canvas.getChildren().remove(line);
            }
        }
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
            if (node instanceof Circle inputMarker && node != outputMarker && inputMarker.contains(x, y)
                    && inputMarker.getOpacity() == 1.0) {
                return tryToConnect(inputMarker, outputMarker);
            }
        }
        cleanupCurrentLine();
        return false;
    }

    /**
     * Attempts to connect the output marker of the source gate to the input marker
     * of
     * 
     * @param inputMarker  The input marker to connect to
     * @param outputMarker The output marker to connect from
     * @return true if the connection is successfully finalized, false otherwise
     */
    private boolean tryToConnect(Circle inputMarker, Circle outputMarker) {
        Point2D inputPos = inputMarker.localToParent(inputMarker.getCenterX(), inputMarker.getCenterY());
        Line currentLine = canvas.getCurrentLine();
        currentLine.setEndX(inputPos.getX());
        currentLine.setEndY(inputPos.getY());

        LogicGate targetGate = canvas.getGateManager().findGateForInputMarker(inputMarker);
        LogicGate sourceGate = canvas.getLineToStartGateMap().get(currentLine);

        if (isConnectionValid(targetGate, sourceGate, inputMarker)) {
            int inputIndex = canvas.getGateManager().findInputMarkerIndex(targetGate, inputMarker);
            if (isInputAvailable(targetGate, inputIndex)) {
                finalizeConnection(sourceGate, targetGate, currentLine, inputIndex);
                return true;
            } else {
                System.out.println("Input marker already has a connection.");
            }
        }
        cleanupCurrentLine();
        return false;
    }

    /**
     * Checks if the connection is valid.
     * 
     * @param targetGate the target gate of the connection
     * @param sourceGate the source gate of the connection
     * 
     * @return true if the connection is valid, false otherwise
     */
    private boolean isConnectionValid(LogicGate targetGate, LogicGate sourceGate, Circle inputMarker) {
        return targetGate != null && sourceGate != null && targetGate != sourceGate;
    }

    /**
     * Checks if the input marker is available for connection.
     * 
     * @param targetGate the target gate of the connection
     * @param inputIndex the index of the input marker
     * 
     * @return true if the input marker is available, false otherwise
     */
    private boolean isInputAvailable(LogicGate targetGate, int inputIndex) {
        return targetGate.getInputConnections(inputIndex).isEmpty();
    }

    /**
     * Finalizes the connection between the source gate and the target gate.
     * 
     * @param sourceGate     the source gate of the connection
     * @param targetGate     the target gate of the connection
     * @param connectionLine the connection line
     * @param inputIndex     the index of the input marker
     */
    private void finalizeConnection(LogicGate sourceGate, LogicGate targetGate, Line connectionLine, int inputIndex) {
        canvas.setCurrentLine(null);
        canvas.getCommandManager().executeCommand(
                new AddConnectionCommand(canvas, sourceGate, targetGate, connectionLine, inputIndex));
    }

    /**
     * Cleans up the current line.
     */
    private void cleanupCurrentLine() {
        if (canvas.getCurrentLine() != null && startGate != null) {
            canvas.getChildren().remove(canvas.getCurrentLine());
            startGate.removeOutputConnection(canvas.getCurrentLine());
            canvas.setCurrentLine(null);
        }
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
