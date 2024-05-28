package com.paperturtle.managers;

import java.util.ArrayList;
import java.util.List;

import com.paperturtle.commands.PasteComponentsCommand;
import com.paperturtle.components.LogicGate;
import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.data.ClipboardData;
import com.paperturtle.gui.CircuitCanvas;

/**
 * The ClipboardManager class is responsible for handling copy and paste
 * operations of logic gates in the circuit canvas.
 * 
 * @see ClipboardData
 * 
 * @author Seweryn Czabanowski
 */
public class ClipboardManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The list of clipboard data representing the copied elements.
     */
    private List<ClipboardData> clipboard = new ArrayList<>();

    /**
     * The list of clipboard data representing the copied text labels.
     */
    private List<TextLabel> clipboardLabels = new ArrayList<>();

    /**
     * The x-offset for pasting the copied gates.
     */
    private static final double OFFSET_X = 30;

    /**
     * The y-offset for pasting the copied gates.
     */
    private static final double OFFSET_Y = 30;

    /**
     * Constructs a ClipboardManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public ClipboardManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Pastes the gates from the clipboard onto the circuit canvas.
     */
    public void pasteGatesFromClipboard() {
        canvas.getCommandManager()
                .executeCommand(new PasteComponentsCommand(canvas, clipboard, clipboardLabels, OFFSET_Y, OFFSET_X));
    }

    public void copySelectedComponentsToClipboard() {
        copySelectedGatesToClipboard();
        copySelectedLabelsToClipboard();
    }

    /**
     * Copies the selected gates to the clipboard.
     */
    private void copySelectedGatesToClipboard() {
        clipboard.clear();
        canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .forEach(entry -> {
                    LogicGate gate = entry.getValue();
                    clipboard.add(gate.getGateClipboardData());
                });
        PasteComponentsCommand.resetGlobalOffset();
    }

    /**
     * Copies the selected textlabels to the clipboard.
     */
    private void copySelectedLabelsToClipboard() {
        clipboardLabels.clear();
        canvas.getSelectedTextLabels().forEach(label -> {
            clipboardLabels.add(label);
        });
        PasteComponentsCommand.resetGlobalOffset();
    }

    /**
     * Gets the clipboard data.
     * 
     * @return the list of ClipboardData
     */
    public List<ClipboardData> getClipboard() {
        return clipboard;
    }

    /**
     * Gets the clipboard labels.
     * 
     * @return the list of TextLabel
     */
    public List<TextLabel> getClipboardLabels() {
        return clipboardLabels;
    }
}
