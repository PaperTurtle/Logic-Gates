package com.example;

/**
 * Interface for logic gates.
 * 
 * This interface defines the methods that all logic gates must implement.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 * 
 */
public interface GateInterface {
    /**
     * Computes the output based on the inputs and returns it.
     * 
     * This method performs the logical operation defined by the specific type of
     * gate, using the inputs provided to the gate.
     * 
     * @return the computed output as a boolean.
     */
    boolean evaluate();

    /**
     * Adds an input gate.
     * 
     * This method adds a LogicGate to the list of inputs for this gate. The added
     * gate's output will be used as an input when evaluating this gate.
     * 
     * @param input the LogicGate to add as an input.
     */
    void addInput(LogicGate input);

    /**
     * Removes an input gate.
     * 
     * This method removes a LogicGate from the list of inputs for this gate. The
     * removed gate's output will no longer be used as an input when evaluating this
     * gate.
     * 
     * @param input the LogicGate to remove from inputs.
     */
    void removeInput(LogicGate input);

    /**
     * Toggles the state of the gate (useful for gates like switches).
     * 
     * This method provides functionality to change the state of a gate,
     * primarily used in interactive gates where manual control is required.
     * For example, in a switch gate, calling this method would change the output
     * from true to false, or vice versa.
     */
    void toggle();
}
