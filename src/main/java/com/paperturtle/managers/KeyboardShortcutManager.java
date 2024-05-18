package com.paperturtle.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.commands.PasteGatesCommand;
import com.paperturtle.commands.RemoveSelectedGatesCommand;
import com.paperturtle.utils.KeyCombination;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * The KeyboardShortcutManager class is responsible for managing keyboard
 * shortcuts for the circuit canvas.
 * 
 * @author Seweryn Czabanowski
 */
public class KeyboardShortcutManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private final CircuitCanvas canvas;

    /**
     * A map associating each key combination with its corresponding action.
     */
    private final Map<KeyCombination, Consumer<KeyEvent>> shortcuts = new HashMap<>();

    /**
     * Constructs a KeyboardShortcutManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public KeyboardShortcutManager(CircuitCanvas canvas) {
        this.canvas = canvas;
        setupKeyboardShortcuts();
    }

    /**
     * Sets up the keyboard shortcuts for the circuit canvas.
     */
    private void setupKeyboardShortcuts() {
        shortcuts.put(new KeyCombination(KeyCode.C, true), this::copy);
        shortcuts.put(new KeyCombination(KeyCode.V, true), this::paste);
        shortcuts.put(new KeyCombination(KeyCode.X, true), this::cut);
        shortcuts.put(new KeyCombination(KeyCode.A, true), this::selectAll);
        shortcuts.put(new KeyCombination(KeyCode.Z, true), this::undo);
        shortcuts.put(new KeyCombination(KeyCode.Y, true), this::redo);

        canvas.setOnKeyPressed(this::handleKeyPress);
    }

    /**
     * Handles key press events and executes the corresponding action for the
     * pressed key combination.
     * 
     * @param event the key event
     */
    private void handleKeyPress(KeyEvent event) {
        KeyCombination keyCombination = new KeyCombination(event.getCode(), event.isControlDown());
        Consumer<KeyEvent> action = shortcuts.get(keyCombination);
        if (action != null) {
            action.accept(event);
            event.consume();
        }
    }

    /**
     * Copies the selected gates to the clipboard.
     * 
     * @param event the key event
     */
    private void copy(KeyEvent event) {
        canvas.getClipboardManager().copySelectedGatesToClipboard();
    }

    /**
     * Pastes the gates from the clipboard to the circuit canvas.
     * 
     * @param event the key event
     */
    private void paste(KeyEvent event) {
        canvas.getCommandManager()
                .executeCommand(new PasteGatesCommand(canvas, canvas.getClipboardManager().getClipboard(), 30, 30));
    }

    /**
     * Cuts the selected gates from the circuit canvas.
     * 
     * @param event the key event
     */
    private void cut(KeyEvent event) {
        canvas.getCommandManager().executeCommand(new RemoveSelectedGatesCommand(canvas));
    }

    /**
     * Selects all components in the circuit canvas.
     * 
     * @param event the key event
     */
    private void selectAll(KeyEvent event) {
        canvas.getSelectionManager().selectAllComponents();
    }

    /**
     * Undoes the last executed command.
     * 
     * @param event the key event
     */
    private void undo(KeyEvent event) {
        canvas.getCommandManager().undo();
    }

    /**
     * Redoes the last undone command.
     * 
     * @param event the key event
     */
    private void redo(KeyEvent event) {
        canvas.getCommandManager().redo();
    }
}
