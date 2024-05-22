package com.paperturtle.components.inputs;

import java.util.Collections;
import java.util.List;

import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * Represents a High Constant gate in a digital circuit.
 * A High Constant gate always outputs true or '1'.
 * 
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class HighConstantGate extends LogicGate {
    /**
     * Constructs a HighConstantGate object with predefined SVG image and output
     * point.
     */
    public HighConstantGate() {
        super("/com/paperturtle/HIGHCONSTANT_ANSI_Labelled.svg",
                null,
                new Point2D(60, 20));
    }

    /**
     * Evaluates the state of the High Constant gate.
     * 
     * @return true always.
     */
    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Collections.singletonList(new Pair<>(new Boolean[] {}, true));
    }

}
