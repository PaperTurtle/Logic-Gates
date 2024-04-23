package com.paperturtle;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * The XnorGate class extends the LogicGate class and represents an XNOR gate in
 * a digital circuit.
 * An XNOR gate (also known as equivalence gate) is a digital logic gate that
 * outputs true or '1' only when the number of true inputs is even.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * calculates the number of true inputs and returns true if the number is even.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class XnorGate extends LogicGate {
    /**
     * Constructs an XnorGate object with predefined SVG image, input points, and
     * output point.
     */
    public XnorGate() {
        super("/com/paperturtle/XNOR_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the XNOR gate based on its inputs.
     * 
     * @return true if the number of true inputs is even, false otherwise.
     */
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

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false, false }, true));
        list.add(new Pair<>(new Boolean[] { false, true }, false));
        list.add(new Pair<>(new Boolean[] { true, false }, false));
        list.add(new Pair<>(new Boolean[] { true, true }, true));
        return list;
    }

}
