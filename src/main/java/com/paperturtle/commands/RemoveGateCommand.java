package com.paperturtle.commands;

import com.paperturtle.components.LogicGate;
import com.paperturtle.gui.CircuitCanvas;

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
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The logic gate to be removed from the canvas.
     */
    private LogicGate gate;

    /**
     * The x-coordinate at which the gate is placed.
     */
    private double x;

    /**
     * The y-coordinate at which the gate is placed.
     */
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
