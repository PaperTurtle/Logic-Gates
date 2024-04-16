package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class XnorGate extends LogicGate {
    public XnorGate() {
        super("/com/example/XNOR_ANSI_Labelled.svg", Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
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
