package com.paperturtle.components;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * Represents a NAND gate in a digital circuit.
 * A NAND gate outputs true or '1' only when not all the inputs are true or '1'.
 * 
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class NandGate extends LogicGate {
    /**
     * Constructs a NandGate object with predefined SVG image, input points, and
     * output point.
     */
    public NandGate() {
        super("/com/paperturtle/NAND_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the NAND gate based on its inputs.
     * 
     * @return true if not all the inputs are true, otherwise false.
     */
    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return true; // If no inputs, NAND gate output is true by default.

        for (LogicGate input : inputs) {
            if (!input.getOutput()) {
                return true; // If any input is false, NAND gate output is true.
            }
        }
        return false; // If all inputs are true, NAND gate output is false.
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false, false }, true));
        list.add(new Pair<>(new Boolean[] { false, true }, true));
        list.add(new Pair<>(new Boolean[] { true, false }, true));
        list.add(new Pair<>(new Boolean[] { true, true }, false));
        return list;
    }

}
