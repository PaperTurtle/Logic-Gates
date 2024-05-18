package com.paperturtle.components;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import javafx.geometry.Point2D;
import javafx.util.Pair;

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
        super("/com/paperturtle/AND_ANSI_Labelled.svg",
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
        if (inputs.size() != 2) {
            return false;
        }

        // Return true if there are no inputs (degenerate case)
        for (LogicGate inputGate : inputs) {
            if (!inputGate.getOutput()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false, false }, false));
        list.add(new Pair<>(new Boolean[] { false, true }, false));
        list.add(new Pair<>(new Boolean[] { true, false }, false));
        list.add(new Pair<>(new Boolean[] { true, true }, true));
        return list;
    }

}
