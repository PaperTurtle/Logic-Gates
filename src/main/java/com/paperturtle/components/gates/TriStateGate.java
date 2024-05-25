package com.paperturtle.components.gates;

import java.util.Arrays;
import java.util.List;

import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * Represents a TriState gate in a digital circuit.
 * A TriState gate has three states: high, low, and high-impedance.
 * 
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class TriStateGate extends LogicGate {
    /**
     * Represents the enable state of the TriState gate.
     */
    private boolean enable;

    /**
     * Represents the high-impedance state of the TriState gate.
     */
    private boolean highImpedance;

    /**
     * Constructs a TriStateGate object with predefined SVG image, input points, and
     * output point.
     */
    public TriStateGate() {
        super("/com/paperturtle/TRISTATE_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 25), new Point2D(66, 0)),
                new Point2D(105, 25));
        this.enable = false;
        this.highImpedance = true;
    }

    @Override
    public boolean evaluate() {
        if (inputs.size() < 2) {
            highImpedance = true;
            return false;
        }

        LogicGate enableGate = inputs.get(1);
        enable = enableGate.getOutput();

        if (!enable) {
            highImpedance = true;
            return false;
        }

        highImpedance = false;
        LogicGate inputGate = inputs.get(0);
        return inputGate.getOutput();
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Arrays.asList(
                new Pair<>(new Boolean[] { false, false }, false),
                new Pair<>(new Boolean[] { false, true }, false),
                new Pair<>(new Boolean[] { true, false }, false),
                new Pair<>(new Boolean[] { true, true }, true));
    }

    /**
     * Checks if the gate is in a high-impedance state.
     * 
     * @return true if the gate is in high-impedance state, false otherwise.
     */
    public boolean isHighImpedance() {
        return highImpedance;
    }

}
