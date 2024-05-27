package com.paperturtle.commands;

import com.paperturtle.gui.CircuitCanvas;

/**
 * Command to paste logic gates and their connections as well as text labels
 * from the clipboard onto
 * the circuit canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class PasteComponentsCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * Constructs an PasteComponentsCommand with the specified parameters.
     * 
     * @param canvas the circuit canvas
     */
    public PasteComponentsCommand(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Executes the command to add logic gates and text labels to the canvas at
     * the specified coordinates.
     */
    @Override
    public void execute() {
        canvas.getCommandManager()
                .executeCommand(new PasteGatesCommand(canvas, canvas.getClipboardManager().getClipboard(), 30, 30));
        canvas.getCommandManager()
                .executeCommand(
                        new PasteLabelsCommand(canvas, canvas.getClipboardManager().getClipboardLabels(), 30, 30));
    }

    /**
     * Undoes the command by removing logic gates and text labels from the canvas.
     */
    @Override
    public void undo() {
        canvas.getCommandManager().executeCommand(new RemoveSelectedComponentsCommand(canvas));
    }
}
