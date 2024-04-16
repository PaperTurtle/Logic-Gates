package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class AndGate extends LogicGate {
    public AndGate() {
        super("/com/example/AND_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    /**
     * Computes the output of the AND gate based on its inputs.
     * 
     * @return true if all inputs are true, otherwise false.
     */
    @Override
    public boolean evaluate() {
        // Return true if there are no inputs (degenerate case)
        if (inputs.isEmpty())
            return true;

        // Check if all inputs are true
        for (LogicGate input : inputs) {
            if (!input.getOutput()) {
                return false;
            }
        }
        return true;
    }
}
