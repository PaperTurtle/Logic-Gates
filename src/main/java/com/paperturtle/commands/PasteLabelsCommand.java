package com.paperturtle.commands;

import java.util.ArrayList;
import java.util.List;

import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Command to paste text labels from the clipboard onto
 * the circuit canvas.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class PasteLabelsCommand implements Command {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The data from the clipboard to be pasted.
     */
    private List<TextLabel> clipboardData;

    /**
     * The list of logic gates that have been pasted.
     */
    private List<TextLabel> pastedLabels = new ArrayList<>();

    /**
     * The x-coordinate offset for pasting.
     */
    private double offsetX;

    /**
     * The y-coordinate offset for pasting.
     */
    private double offsetY;

    /**
     * The global x-coordinate offset for pasting.
     */
    private static double globalOffsetX = 0;

    /**
     * The global y-coordinate offset for pasting.
     */
    private static double globalOffsetY = 0;

    /**
     * Constructs a PasteLabelsCommand with the specified parameters.
     * 
     * @param canvas        the circuit canvas to which gates are pasted
     * @param clipboardData the clipboard data representing gates and connections to
     *                      be pasted
     * @param offsetX       the x offset for pasting the gates
     * @param offsetY       the y offset for pasting the gates
     */
    public PasteLabelsCommand(CircuitCanvas canvas, List<TextLabel> clipboardData, double offsetX, double offsetY) {
        this.canvas = canvas;
        this.clipboardData = clipboardData;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Executes the command to paste logic gates and their connections onto the
     * canvas.
     * The gates and connections are positioned according to the provided offsets.
     */
    @Override
    public void execute() {
        canvas.getSelectionManager().deselectAllLabels();
        createLabels();
        incrementGlobalOffset();
    }

    /**
     * Undoes the command by removing the pasted gates and connections from the
     * canvas.
     */
    @Override
    public void undo() {
        removeGates();
        decrementGlobalOffset();
    }

    /**
     * Creates logic gates from the clipboard data and adds them to the canvas.
     * 
     * @return a map of the created gates, with the gate ID as the key
     */
    private void createLabels() {
        clipboardData.forEach(originalLabel -> {
            // Create a new TextLabel instance with the same properties as the original
            TextLabel newLabel = new TextLabel(originalLabel.getLabel(), originalLabel.getWidth(),
                    originalLabel.getHeight());
            double newX = originalLabel.getLayoutX() + offsetX + globalOffsetX;
            double newY = originalLabel.getLayoutY() + offsetY + globalOffsetY;

            newLabel.setLayoutX(newX);
            newLabel.setLayoutY(newY);
            newLabel.setFillColor((Color) originalLabel.getFillColor());
            newLabel.setBackgroundColor((Color) originalLabel.getBackgroundColor());
            newLabel.setFontFamily(originalLabel.getFontFamily());
            newLabel.setFontSize(originalLabel.getFontSize());
            newLabel.setUnderline(originalLabel.isUnderline());
            newLabel.setStrikethrough(originalLabel.isStrikethrough());
            newLabel.setAutoSize(originalLabel.isAutoSize());
            newLabel.setFont(Font.font(originalLabel.getFontFamily(),
                    originalLabel.getFontWeight() == FontWeight.BOLD ? FontWeight.BOLD : FontWeight.NORMAL,
                    originalLabel.getFontPosture() == FontPosture.ITALIC ? FontPosture.ITALIC : FontPosture.REGULAR,
                    originalLabel.getFontSize()));

            canvas.drawTextLabel(newLabel, newX, newY);
            newLabel.getStyleClass().add("selected");
            pastedLabels.add(newLabel);
        });
    }

    /**
     * Removes the pasted gates from the canvas.
     */
    private void removeGates() {
        pastedLabels.forEach(label -> canvas.removeTextLabel(label));
        pastedLabels.clear();
    }

    /**
     * Increments the global offset for pasting gates and connections.
     */
    private void incrementGlobalOffset() {
        globalOffsetX += 30;
        globalOffsetY += 30;
    }

    /**
     * Decrements the global offset for pasting gates and connections.
     */
    private void decrementGlobalOffset() {
        globalOffsetX -= 30;
        globalOffsetY -= 30;
    }

    /**
     * Sets the global offset for pasting gates and connections.
     */
    public static void setGlobalOffset(double x, double y) {
        globalOffsetX = x;
        globalOffsetY = y;
    }

    /**
     * Set global offset to 0.
     */
    public static void resetGlobalOffset() {
        globalOffsetX = 0;
        globalOffsetY = 0;
    }
}
