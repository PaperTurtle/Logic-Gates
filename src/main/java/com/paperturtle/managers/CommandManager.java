package com.paperturtle.managers;

import java.util.Stack;

import com.paperturtle.commands.Command;

/**
 * The CommandManager class is responsible for managing the execution, undo, and
 * redo of commands.
 * 
 * @see Command
 * 
 * @author Seweryn Czabanowski
 */
public class CommandManager {
    /**
     * The stack of executed commands that can be undone.
     */
    private Stack<Command> undoStack = new Stack<>();

    /**
     * The stack of undone commands that can be redone.
     */
    private Stack<Command> redoStack = new Stack<>();

    /**
     * Executes a command and pushes it onto the undo stack.
     * Clears the redo stack.
     * 
     * @param command the command to execute
     */
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Undoes the last executed command and pushes it onto the redo stack.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /**
     * Redoes the last undone command and pushes it back onto the undo stack.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
}
