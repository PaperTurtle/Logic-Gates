package com.example;

public class XorGate extends LogicGate {
    public XorGate() {
        super("com/example/XOR_ANSI_Labelled.svg");
    }

    @Override
    public boolean evaluate() {
        int trueCount = 0;
        for (LogicGate input : inputs) {
            if (input.getOutput())
                trueCount++;
        }
        // XOR is true only if an odd number of inputs are true.
        return trueCount % 2 == 1;
    }
}
