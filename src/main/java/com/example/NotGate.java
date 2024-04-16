package com.example;

import java.util.Arrays;
import javafx.geometry.Point2D;

/**
 * The NotGate class extends the LogicGate class and represents a NOT gate in
 * a digital circuit.
 * A NOT gate is a digital logic gate that outputs true or '1' only when the
 * input is false or '0'.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * checks the input and returns the inverse of it.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class NotGate extends LogicGate {
    /**
     * Constructs a NotGate object with predefined SVG image, input points, and
     * output point.
     */
    public NotGate() {
        super("/com/example/NOT_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 25)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the NOT gate based on its input.
     * 
     * @return true if the input is false, false if the input is true.
     */
    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return true; // Default to true when no input (typically NOT gates have one input).

        // NOT gate inverts its single input
        return !inputs.get(0).getOutput();
    }
}