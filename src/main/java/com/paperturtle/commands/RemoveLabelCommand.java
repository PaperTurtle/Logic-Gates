package com.paperturtle.commands;

import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

/**
 * Command to remove a text label from the circuit canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class RemoveLabelCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The text label to be removed from the canvas.
     */
    private TextLabel label;

    /**
     * The x-coordinate at which the text label is placed.
     */
    private double x;

    /**
     * The y-coordinate at which the text label is placed.
     */
    private double y;

    /**
     * Constructs a RemoveGateCommand with the specified parameters.
     * 
     * @param canvas the circuit canvas from which the gate is removed
     * @param gate   the text label to be removed
     */
    public RemoveLabelCommand(CircuitCanvas canvas, TextLabel label) {
        this.canvas = canvas;
        this.label = label;
        this.x = this.label.getLayoutX();
        this.y = this.label.getLayoutY();
    }

    /**
     * Executes the command to remove the text label from the canvas.
     */
    @Override
    public void execute() {
        label.removeSelf();
    }

    /**
     * Undoes the command by re-adding the text label to the canvas at its original
     * position.
     */
    @Override
    public void undo() {
        canvas.drawTextLabel(label, x, y);
    }
}
