package com.paperturtle.components;

import java.util.Collections;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * Represents a Low Constant gate in a digital circuit.
 * A Low Constant gate always outputs false or '0'.
 * 
 * @see LogicGate
 * @see Point2D
 * @see Pair
 * 
 * @author Seweryn Czabanowski
 */
public class LowConstantGate extends LogicGate {

    /**
     * Constructs a LowConstantGate object with predefined SVG image and output
     * point.
     */
    public LowConstantGate() {
        super("/com/paperturtle/LOWCONSTANT_ANSI_Labelled.svg",
                null,
                new Point2D(60, 20));
    }

    /**
     * Evaluates the state of the Low Constant gate.
     * 
     * @return false always.
     */
    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Collections.singletonList(new Pair<>(new Boolean[] {}, false));
    }

}
