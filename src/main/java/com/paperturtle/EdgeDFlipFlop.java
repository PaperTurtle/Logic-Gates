package com.paperturtle;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class EdgeDFlipFlop extends LogicGate {
    private boolean Q = false;
    private boolean lastClockState = false;

    public EdgeDFlipFlop() {
        super("/com/paperturtle/EDGE_D_FLIPFLOP_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(10, 10), new Point2D(10, 30)),
                new Point2D(90, 20));
    }

    @Override
    public boolean evaluate() {
        boolean D = inputs.get(0).getOutput();
        boolean clock = inputs.get(1).getOutput();

        if (clock && !lastClockState) {
            Q = D;
        }
        lastClockState = clock;
        return Q;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Arrays.asList(
                new Pair<>(new Boolean[] { false, false }, Q),
                new Pair<>(new Boolean[] { false, true }, Q),
                new Pair<>(new Boolean[] { true, false }, Q),
                new Pair<>(new Boolean[] { true, true }, !Q));
    }
}
