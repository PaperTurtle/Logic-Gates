package com.paperturtle.components;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class TriStateGate extends LogicGate {
    private boolean enable;

    public TriStateGate() {
        super("/com/paperturtle/TRISTATE_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 25), new Point2D(66, 0)),
                new Point2D(105, 25));
        this.enable = false;
    }

    @Override
    public boolean evaluate() {
        if (inputs.size() < 2) {
            return false;
        }
        LogicGate enableGate = inputs.get(1);
        enable = enableGate.getOutput();

        if (!enable) {
            return false;
        }

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

}
