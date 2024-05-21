package com.paperturtle.commands;

import com.paperturtle.components.LogicGate;
import com.paperturtle.gui.CircuitCanvas;

import javafx.scene.shape.Line;

/**
 * Command to add a connection between two logic gates on a circuit canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class AddConnectionCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The source gate from which the connection originates.
     */
    private LogicGate sourceGate;

    /**
     * The target gate to which the connection leads.
     */
    private LogicGate targetGate;

    /**
     * The graphical representation of the connection.
     */
    private Line connectionLine;

    /**
     * The index of the target input in the target gate.
     */
    private int targetInputIndex;

    /**
     * Constructs an AddConnectionCommand with the specified parameters.
     * 
     * @param canvas           the circuit canvas on which the connection is added
     * @param sourceGate       the source logic gate of the connection
     * @param targetGate       the target logic gate of the connection
     * @param connectionLine   the line representing the connection
     * @param targetInputIndex the index of the target gate's input to which the
     *                         connection is made
     */
    public AddConnectionCommand(CircuitCanvas canvas, LogicGate sourceGate, LogicGate targetGate, Line connectionLine,
            int targetInputIndex) {
        this.canvas = canvas;
        this.sourceGate = sourceGate;
        this.targetGate = targetGate;
        this.connectionLine = connectionLine;
        this.targetInputIndex = targetInputIndex;
    }

    /**
     * Executes the command to add a connection between the source and target logic
     * gates.
     * Updates the canvas and gate states accordingly.
     */
    @Override
    public void execute() {
        if (!canvas.getChildren().contains(connectionLine)) {
            canvas.getChildren().add(connectionLine);
        }
        sourceGate.addOutputConnection(connectionLine);
        sourceGate.addOutputGate(targetGate);
        targetGate.addInputConnection(connectionLine, targetInputIndex);
        targetGate.addInput(sourceGate);

        canvas.getLineToStartGateMap().put(connectionLine, sourceGate);

        sourceGate.evaluate();
        targetGate.evaluate();
        targetGate.propagateStateChange();
        sourceGate.updateOutputConnectionsColor(sourceGate.evaluate());

        targetGate.getInputMarkers().forEach(marker -> marker.toFront());
        sourceGate.getOutputMarker().toFront();

        canvas.scheduleUpdate(targetGate);
        canvas.scheduleUpdate(sourceGate);
    }

    @Override
    public void undo() {
        if (targetInputIndex >= 0 && targetInputIndex < targetGate.getInputConnections().size()) {
            sourceGate.removeOutputConnection(connectionLine);
            targetGate.removeInputConnection(connectionLine, targetInputIndex);
            targetGate.removeInput(sourceGate);

            canvas.getChildren().remove(connectionLine);
            canvas.getLineToStartGateMap().remove(connectionLine);

            sourceGate.evaluate();
            targetGate.evaluate();
            sourceGate.propagateStateChange();
            targetGate.propagateStateChange();

            canvas.scheduleUpdate(targetGate);
            canvas.scheduleUpdate(sourceGate);
        } else {
            System.out.println("Invalid target input index: " + targetInputIndex);
        }
    }
}
