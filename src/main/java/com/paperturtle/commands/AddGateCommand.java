package com.paperturtle.commands;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.components.LogicGate;
import com.paperturtle.managers.GateManager;

/**
 * Command to add a logic gate to the circuit canvas at a specified location.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class AddGateCommand implements Command {
    private CircuitCanvas canvas;
    private LogicGate gate;
    private double x;
    private double y;

    /**
     * Constructs an AddGateCommand with the specified parameters.
     * 
     * @param canvas the circuit canvas to which the gate is added
     * @param gate   the logic gate to be added
     * @param x      the x-coordinate where the gate is placed
     * @param y      the y-coordinate where the gate is placed
     */
    public AddGateCommand(CircuitCanvas canvas, LogicGate gate, double x, double y) {
        this.canvas = canvas;
        this.gate = gate;
        this.x = x;
        this.y = y;
    }

    /**
     * Executes the command to add a logic gate to the canvas at the specified
     * coordinates.
     * 
     * @see CircuitCanvas#drawGate(LogicGate, double, double)
     */
    @Override
    public void execute() {
        canvas.drawGate(gate, x, y);
    }

    /**
     * Undoes the command by removing the logic gate from the canvas.
     * 
     * @see CircuitCanvas#getGateManager()
     * @see GateManager#removeGate(javafx.scene.image.ImageView)
     */
    @Override
    public void undo() {
        canvas.getGateManager().removeGate(gate.getImageView());
    }
}
