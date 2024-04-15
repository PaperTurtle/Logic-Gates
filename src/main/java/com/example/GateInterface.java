package com.example;

public interface GateInterface {
    /**
     * Computes the output based on the inputs and returns it.
     * 
     * @return the computed output as a boolean.
     */
    boolean evaluate();

    /**
     * Adds an input gate.
     * 
     * @param input the logic gate to add as an input.
     */
    void addInput(LogicGate input);

    /**
     * Removes an input gate.
     * 
     * @param input the logic gate to remove from inputs.
     */
    void removeInput(LogicGate input);

    /**
     * Toggles the state of the gate (useful for gates like switches).
     * This method provides functionality to change the state of a gate,
     * primarily used in interactive gates where manual control is required.
     */
    void toggle();
}
