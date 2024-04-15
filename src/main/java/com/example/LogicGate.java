package com.example;

import java.util.ArrayList;
import java.util.List;

public abstract class LogicGate {
    // List to hold input gates
    protected List<LogicGate> inputs;

    public LogicGate() {
        this.inputs = new ArrayList<>();
    }

    /**
     * Abstract method to compute the output based on the inputs.
     */
    public abstract boolean evaluate();

    /**
     * Adds an input gate to this logic gate.
     * 
     * @param input the LogicGate to be added.
     */
    public void addInput(LogicGate input) {
        if (!inputs.contains(input)) {
            inputs.add(input);
        }
    }

    /**
     * Removes an input gate from this logic gate.
     * 
     * @param input the LogicGate to be removed.
     */
    public void removeInput(LogicGate input) {
        inputs.remove(input);
    }

    /**
     * Returns the current output value of the gate.
     * This method will invoke evaluate() to ensure the latest input is returned.
     * 
     * @return the current output of the logic gate.
     */
    public boolean getOutput() {
        return evaluate();
    }

    /**
     * Optional method to update the output of this gate.
     * This could be used to notify or update connected output devices or gates.
     */
    public void updateOutput() {
        // TODO Implement functionality later
    }
}
