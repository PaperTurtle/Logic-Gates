package com.paperturtle.commands;

import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

import com.paperturtle.components.LogicGate;
import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

/**
 * Command to remove all selected gates and their connections from the circuit
 * canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * @see CircuitCanvas
 * @see LogicGate
 * @see TextLabel
 * 
 * @author Seweryn Czabanowski
 */
public class RemoveSelectedGatesCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The list of logic gates that have been removed.
     */
    private List<LogicGate> removedGates = new ArrayList<>();

    /**
     * The list of connections that have been removed.
     */
    private List<Line> removedConnections = new ArrayList<>();

    /**
     * The list of labels that have been removed.
     */
    private List<TextLabel> removedLabels = new ArrayList<>();

    /**
     * A list of ConnectionInfo objects representing the connections of the removed
     * gates.
     */
    private List<ConnectionInfo> removedConnectionInfos = new ArrayList<>();

    /**
     * A static inner class representing the connection information between two
     * logic gates.
     */
    private static class ConnectionInfo {
        /**
         * The source logic gate of the connection.
         */
        LogicGate sourceGate;

        /**
         * The target logic gate of the connection.
         */
        LogicGate targetGate;

        /**
         * The line representing the connection.
         */
        Line connection;

        /**
         * The index of the input where the connection is made.
         */
        int inputIndex;

        /**
         * Constructs a ConnectionInfo object with the given source gate, target gate,
         * connection line, and input index.
         *
         * @param sourceGate the source logic gate of the connection
         * @param targetGate the target logic gate of the connection
         * @param connection the line representing the connection
         * @param inputIndex the index of the input where the connection is made
         */
        ConnectionInfo(LogicGate sourceGate, LogicGate targetGate, Line connection, int inputIndex) {
            this.sourceGate = sourceGate;
            this.targetGate = targetGate;
            this.connection = connection;
            this.inputIndex = inputIndex;
        }
    }

    /**
     * Constructs a RemoveSelectedGatesCommand for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas from which the selected gates are removed
     */
    public RemoveSelectedGatesCommand(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Executes the command to remove all selected gates and their connections from
     * the canvas.
     */
    @Override
    public void execute() {
        removeGatesAndConnections(canvas.getSelectedGates());
        removeLabels(canvas.getSelectedTextLabels());
    }

    /**
     * Undoes the command by re-adding the removed gates, connections, and labels to
     * the canvas.
     */
    @Override
    public void undo() {
        reAddGates();
        reAddConnections();
        reAddLabels();
        clearRemovedLists();
    }

    /**
     * Redoes the command by removing the gates, connections, and labels that were
     * 
     * @param selectedGates the list of selected gates to be removed
     */
    private void removeGatesAndConnections(List<LogicGate> selectedGates) {
        selectedGates.forEach(gate -> {
            removedGates.add(gate);
            collectConnections(gate);
            canvas.getGateManager().removeGate(gate.getImageView());
        });
    }

    /**
     * Collects the connections of the specified logic gate and adds them to the
     * 
     * @param gate the logic gate from which connections are collected
     */
    private void collectConnections(LogicGate gate) {
        gate.getOutputConnections().forEach(connection -> {
            removedConnections.add(connection);
            LogicGate targetGate = canvas.getGateManager().findTargetGate(connection);
            if (targetGate != null) {
                int index = targetGate.findInputConnectionIndex(connection);
                removedConnectionInfos.add(new ConnectionInfo(gate, targetGate, connection, index));
            } else {
                removedConnectionInfos.add(new ConnectionInfo(gate, null, connection, -1));
            }
        });

        gate.getInputConnections().forEach(connectionList -> connectionList.forEach(connection -> {
            removedConnections.add(connection);
            LogicGate sourceGate = canvas.getLineToStartGateMap().get(connection);
            if (sourceGate != null) {
                int index = gate.findInputConnectionIndex(connection);
                removedConnectionInfos.add(new ConnectionInfo(sourceGate, gate, connection, index));
            } else {
                removedConnectionInfos.add(new ConnectionInfo(null, gate, connection, -1));
            }
        }));
    }

    /**
     * Removes the specified text labels from the canvas.
     * 
     * @param selectedLabels the list of text labels to be removed
     */
    private void removeLabels(List<TextLabel> selectedLabels) {
        selectedLabels.forEach(label -> {
            removedLabels.add(label);
            label.removeSelf();
        });
    }

    /**
     * Re-adds the removed gates to the canvas.
     */
    private void reAddGates() {
        removedGates.forEach(gate -> {
            canvas.drawGate(gate, gate.getPosition().getX(), gate.getPosition().getY());
            gate.getImageView().getStyleClass().add("selected");
        });
    }

    /**
     * Re-adds the removed connections to the canvas.
     */
    private void reAddConnections() {
        removedConnectionInfos.forEach(info -> {
            if (info.sourceGate != null) {
                info.sourceGate.addOutputConnection(info.connection);
            }

            if (info.targetGate != null && info.inputIndex != -1) {
                reconnect(info);
            }

            if (!canvas.getChildren().contains(info.connection)) {
                canvas.getChildren().add(info.connection);
            }

            canvas.getLineToStartGateMap().put(info.connection, info.sourceGate);
        });
    }

    /**
     * Reconnects the specified connection between the source and target gates.
     * 
     * @param info the connection information to be reconnected
     */
    private void reconnect(ConnectionInfo info) {
        if (info.sourceGate == null || info.targetGate == null) {
            System.out.println("Source or target gate is null. Cannot reconnect.");
            return;
        }

        Point2D sourcePos = info.sourceGate.getOutputMarker().localToParent(
                info.sourceGate.getOutputMarker().getCenterX(), info.sourceGate.getOutputMarker().getCenterY());
        Point2D targetPos = info.targetGate.getInputMarkers().get(info.inputIndex).localToParent(
                info.targetGate.getInputMarkers().get(info.inputIndex).getCenterX(),
                info.targetGate.getInputMarkers().get(info.inputIndex).getCenterY());

        info.connection.setStartX(sourcePos.getX());
        info.connection.setStartY(sourcePos.getY());
        info.connection.setEndX(targetPos.getX());
        info.connection.setEndY(targetPos.getY());

        info.targetGate.addInputConnection(info.connection, info.inputIndex);
        info.targetGate.addInput(info.sourceGate);
    }

    /**
     * Re-adds the removed labels to the canvas.
     */
    private void reAddLabels() {
        removedLabels.forEach(label -> {
            canvas.drawTextLabel(label, label.getLayoutX(), label.getLayoutY());
            label.getStyleClass().add("selected");
        });
    }

    /**
     * Clears the removed gates, connections, labels, and connection information
     */
    private void clearRemovedLists() {
        removedGates.clear();
        removedConnections.clear();
        removedLabels.clear();
        removedConnectionInfos.clear();
    }
}
