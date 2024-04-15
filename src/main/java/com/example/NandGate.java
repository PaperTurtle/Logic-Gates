package com.example;

public class NandGate extends LogicGate {
    public NandGate() {
        super("/com/example/NAND_ANSI_Labelled.svg");
    }

    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())

            return true; // If no inputs, NAND gate output is true.

        for (LogicGate input : inputs) {
            if (!input.getOutput()) {
                return true;
            }
        }
        return false;
    }
}
