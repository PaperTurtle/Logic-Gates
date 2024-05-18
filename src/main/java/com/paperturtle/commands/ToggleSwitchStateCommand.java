package com.paperturtle.commands;

import com.paperturtle.components.SwitchGate;

/**
 * Command to toggle the state of a switch gate.
 * Implements the Command interface to provide execute and undo functionality.
 * 
 * @see Command
 * @see SwitchGate
 * 
 * @author Seweryn Czabanowski
 */
public class ToggleSwitchStateCommand implements Command {
    private SwitchGate switchGate;
    private boolean previousState;
    private boolean newState;

    /**
     * Constructs a ToggleSwitchStateCommand for the specified switch gate.
     * 
     * @param switchGate the switch gate whose state is toggled
     */
    public ToggleSwitchStateCommand(SwitchGate switchGate) {
        this.switchGate = switchGate;
        this.previousState = switchGate.getState();
        this.newState = !previousState;
    }

    /**
     * Executes the command to toggle the switch gate state.
     */
    @Override
    public void execute() {
        switchGate.setState(newState);
    }

    /**
     * Undoes the command by reverting the switch gate to its previous state.
     */
    @Override
    public void undo() {
        switchGate.setState(previousState);
    }
}
