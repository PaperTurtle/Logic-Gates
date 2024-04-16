package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

/**
 * The AndGate class extends the LogicGate class and represents an AND gate in
 * a digital circuit.
 * An AND gate is a digital logic gate that outputs true or '1' only when all
 * the
 * inputs are true or '1'.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * checks the inputs and returns true only if all of them are true.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class AndGate extends LogicGate {
    /**
     * Constructs an AndGate object with predefined SVG image, input points, and
     * output point.
     */
    public AndGate() {
        super("/com/example/AND_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the AND gate based on its inputs.
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
                return false; // If any input is false, AND gate output is false.
            }
        }
        return true; // If all inputs are true, AND gate output is true.
    }
}
