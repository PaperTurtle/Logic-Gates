package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class OrGate extends LogicGate {
    public OrGate() {
        super("/com/example/OR_ANSI_Labelled.svg", Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
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
