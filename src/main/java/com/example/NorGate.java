package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class NorGate extends LogicGate {
    public NorGate() {
        super("/com/example/NOR_ANSI_Labelled.svg", Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
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
