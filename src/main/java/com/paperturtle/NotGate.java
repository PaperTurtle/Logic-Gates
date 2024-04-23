package com.paperturtle;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

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
        super("/com/paperturtle/NOT_ANSI_Labelled.svg",
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
        if (inputs.isEmpty()) {
            return false;
        }

        return !inputs.get(0).getOutput();
    }

    @Override
    public void removeInput(LogicGate input) {
        super.removeInput(input);
        evaluate();
        propagateStateChange();
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false }, true));
        list.add(new Pair<>(new Boolean[] { true }, false));
        return list;
    }

}