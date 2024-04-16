package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class XorGate extends LogicGate {
    public XorGate() {
        super("/com/example/XOR_ANSI_Labelled.svg", Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
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
