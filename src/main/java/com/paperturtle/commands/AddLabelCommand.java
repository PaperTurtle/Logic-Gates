package com.paperturtle.commands;

import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

/**
 * Command to add a text label to the circuit canvas at a specified location.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class AddLabelCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The logic gate to be added to the canvas.
     */
    private TextLabel label;

    /**
     * The x-coordinate at which the gate is to be placed.
     */
    private double x;

    /**
     * The y-coordinate at which the gate is to be placed.
     */
    private double y;

    /**
     * Constructs an AddGateCommand with the specified parameters.
     * 
     * @param canvas the circuit canvas to which the gate is added
     * @param label  the text label to be added
     * @param x      the x-coordinate where the gate is placed
     * @param y      the y-coordinate where the gate is placed
     */
    public AddLabelCommand(CircuitCanvas canvas, TextLabel label, double x, double y) {
        this.canvas = canvas;
        this.label = label;
        this.x = x;
        this.y = y;
    }

    /**
     * Executes the command to add a logic gate to the canvas at the specified
     * coordinates.
     * 
     * @see CircuitCanvas#drawTextLabel(TextLabel, double, double)
     */
    @Override
    public void execute() {
        canvas.drawTextLabel(label, x, y);
    }

    /**
     * Undoes the command by removing the logic gate from the canvas.
     * 
     * @see CircuitCanvas#getGateManager()
     * @see TextLabel#removeSelf()
     */
    @Override
    public void undo() {
        label.removeSelf();
    }

}
