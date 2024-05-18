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

public class KeyboardShortcutManager {
    private final CircuitCanvas canvas;
    private final Map<KeyCombination, Consumer<KeyEvent>> shortcuts = new HashMap<>();

    public KeyboardShortcutManager(CircuitCanvas canvas) {
        this.canvas = canvas;
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        shortcuts.put(new KeyCombination(KeyCode.C, true), this::copy);
        shortcuts.put(new KeyCombination(KeyCode.V, true), this::paste);
        shortcuts.put(new KeyCombination(KeyCode.X, true), this::cut);
        shortcuts.put(new KeyCombination(KeyCode.A, true), this::selectAll);
        shortcuts.put(new KeyCombination(KeyCode.Z, true), this::undo);
        shortcuts.put(new KeyCombination(KeyCode.Y, true), this::redo);

        canvas.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCombination keyCombination = new KeyCombination(event.getCode(), event.isControlDown());
        Consumer<KeyEvent> action = shortcuts.get(keyCombination);
        if (action != null) {
            action.accept(event);
            event.consume();
        }
    }

    private void copy(KeyEvent event) {
        canvas.getClipboardManager().copySelectedGatesToClipboard();
    }

    private void paste(KeyEvent event) {
        canvas.getCommandManager()
                .executeCommand(new PasteGatesCommand(canvas, canvas.getClipboardManager().getClipboard(), 30, 30));
    }

    private void cut(KeyEvent event) {
        canvas.getCommandManager().executeCommand(new RemoveSelectedGatesCommand(canvas));
    }

    private void selectAll(KeyEvent event) {
        canvas.getSelectionManager().selectAllComponents();
    }

    private void undo(KeyEvent event) {
        canvas.getCommandManager().undo();
    }

    private void redo(KeyEvent event) {
        canvas.getCommandManager().redo();
    }
}
