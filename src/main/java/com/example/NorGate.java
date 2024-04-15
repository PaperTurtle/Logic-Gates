package com.example;

public class NorGate extends LogicGate {
    public NorGate() {
        super("/com/example/NOR_ANSI_Labelled.svg");
    }

    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return true; // If no inputs, NOR gate output is true.

        for (LogicGate input : inputs) {
            if (input.getOutput()) {
                return false;
            }
        }
        return true;
    }
}
