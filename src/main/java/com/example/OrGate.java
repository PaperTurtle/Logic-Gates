package com.example;

public class OrGate extends LogicGate {
    public OrGate() {
        super("/com/example/OR_ANSI_Labelled.svg");
    }

    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return false; // If no inputs, OR gate output is false by default.

        for (LogicGate input : inputs) {
            if (input.getOutput()) {
                return true;
            }
        }
        return false;
    }
}
