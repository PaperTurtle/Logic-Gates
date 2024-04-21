package com.example;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * The OrGate class extends the LogicGate class and represents an OR gate in
 * a digital circuit.
 * An OR gate is a digital logic gate that outputs true or '1' only when at
 * least one of the inputs is true.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * checks the inputs and returns true if at least one of them is true.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class OrGate extends LogicGate {
    /**
     * Constructs an OrGate object with predefined SVG image, input points, and
     * output point.
     */
    public OrGate() {
        super("/com/example/OR_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the OR gate based on its inputs.
     * 
     * @return true if at least one of the inputs is true, false otherwise.
     */
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

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false, false }, false));
        list.add(new Pair<>(new Boolean[] { false, true }, true));
        list.add(new Pair<>(new Boolean[] { true, false }, true));
        list.add(new Pair<>(new Boolean[] { true, true }, true));
        return list;
    }

}