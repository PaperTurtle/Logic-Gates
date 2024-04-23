package com.paperturtle;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * The XorGate class extends the LogicGate class and represents an XOR gate in
 * a digital circuit.
 * An XOR gate (also known as exclusive OR gate) is a digital logic gate that
 * outputs true or '1' only when the number of true inputs is odd.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * calculates the number of true inputs and returns true if the number is odd.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class XorGate extends LogicGate {
    /**
     * Constructs an XorGate object with predefined SVG image, input points, and
     * output point.
     */
    public XorGate() {
        super("/com/paperturtle/XOR_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the XOR gate based on its inputs.
     * 
     * @return true if the number of true inputs is odd, false otherwise.
     */
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

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false, false }, false));
        list.add(new Pair<>(new Boolean[] { false, true }, true));
        list.add(new Pair<>(new Boolean[] { true, false }, true));
        list.add(new Pair<>(new Boolean[] { true, true }, false));
        return list;
    }

}
