package com.paperturtle;

public class ToggleSwitchStateCommand implements Command {
    private SwitchGate switchGate;
    private boolean previousState;
    private boolean newState;

    public ToggleSwitchStateCommand(SwitchGate switchGate) {
        this.switchGate = switchGate;
        this.previousState = switchGate.getState();
        this.newState = !previousState;
    }

    @Override
    public void execute() {
        switchGate.setState(newState);
    }

    @Override
    public void undo() {
        switchGate.setState(previousState);
    }
}
