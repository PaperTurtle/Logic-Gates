package com.paperturtle.commands;

/**
 * Interface for the command pattern. Represents an executable command with undo
 * functionality.
 * 
 * @see AddConnectionCommand
 * @see AddGateCommand
 * @see PasteGatesCommand
 * @see RemoveConnectionCommand
 * @see RemoveGateCommand
 * @see RemoveSelectedGatesCommand
 * @see ToggleSwitchStateCommand
 * 
 * @author Seweryn Czabanowski
 */
public interface Command {
    /**
     * Executes the command.
     */
    void execute();

    /**
     * Undoes the command.
     */
    void undo();
}
