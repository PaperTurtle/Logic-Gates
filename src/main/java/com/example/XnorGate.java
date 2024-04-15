package com.example;

public class XnorGate extends LogicGate {
    public XnorGate() {
        super();
    }

    @Override
    public boolean evaluate() {
        int trueCount = 0;
        for (LogicGate input : inputs) {
            if (input.getOutput())
                trueCount++;
        }
        // XNOR is true only if an even number of inputs are true.
        return trueCount % 2 == 0;
    }
}
