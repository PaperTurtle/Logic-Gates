package com.paperturtle.commands;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;

/**
 * Command to remove a logic gate from the circuit canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class RemoveGateCommand implements Command {
    private CircuitCanvas canvas;
    private LogicGate gate;
    private double x;
    private double y;

    /**
     * Constructs a RemoveGateCommand with the specified parameters.
     * 
     * @param canvas the circuit canvas from which the gate is removed
     * @param gate   the logic gate to be removed
     */
    public RemoveGateCommand(CircuitCanvas canvas, LogicGate gate) {
        this.canvas = canvas;
        this.gate = gate;
        Point2D position = gate.getPosition();
        this.x = position.getX();
        this.y = position.getY();
    }

    /**
     * Executes the command to remove the logic gate from the canvas.
     */
    @Override
    public void execute() {
        canvas.getGateManager().removeGate(gate.getImageView());
    }

    /**
     * Undoes the command by re-adding the logic gate to the canvas at its original
     * position.
     */
    @Override
    public void undo() {
        canvas.drawGate(gate, x, y);
    }
}
