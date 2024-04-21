package com.example;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * The NorGate class extends the LogicGate class and represents a NOR gate in
 * a digital circuit.
 * A NOR gate is a digital logic gate that outputs true or '1' only when all the
 * inputs are false or '0'.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * checks the inputs and returns true only if all of them are false.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class NorGate extends LogicGate {
    /**
     * Constructs a NorGate object with predefined SVG image, input points, and
     * output point.
     */
    public NorGate() {
        super("/com/example/NOR_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the NOR gate based on its inputs.
     * 
     * @return true if all the inputs are false, false otherwise.
     */
    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return true; // If no inputs, NOR gate output is true by default.

        for (LogicGate input : inputs) {
            if (input.getOutput()) {
                return false; // If any input is true, NOR gate output is false.
            }
        }
        return true; // If all inputs are false, NOR gate output is true.
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false, false }, true));
        list.add(new Pair<>(new Boolean[] { false, true }, false));
        list.add(new Pair<>(new Boolean[] { true, false }, false));
        list.add(new Pair<>(new Boolean[] { true, true }, false));
        return list;
    }

}
