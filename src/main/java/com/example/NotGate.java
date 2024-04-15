package com.example;

public class NotGate extends LogicGate {
    public NotGate() {
        super();
    }

    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return true; // Default to true when no input (typically NOT gates have one input).

        // NOT gate inverts its single input
        return !inputs.get(0).getOutput();
    }
}