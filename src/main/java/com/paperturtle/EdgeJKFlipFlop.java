package com.paperturtle;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class EdgeJKFlipFlop extends LogicGate {
    private boolean Q = false;
    private boolean lastClockState = false;

    public EdgeJKFlipFlop() {
        super("/com/paperturtle/EDGE_JK_FLIPFLOP_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(10, 10), new Point2D(10, 30), new Point2D(10, 50)),
                new Point2D(90, 20));
    }

    @Override
    public boolean evaluate() {
        boolean J = inputs.get(0).getOutput();
        boolean K = inputs.get(1).getOutput();
        boolean clock = inputs.get(2).getOutput();

        if (clock && !lastClockState) {
            if (J && K) {
                Q = !Q;
            } else if (J) {
                Q = true;
            } else if (K) {
                Q = false;
            }
        }
        lastClockState = clock;
        return Q;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Arrays.asList(
                new Pair<>(new Boolean[] { false, false, false }, Q),
                new Pair<>(new Boolean[] { false, false, true }, Q),
                new Pair<>(new Boolean[] { false, true, false }, Q),
                new Pair<>(new Boolean[] { false, true, true }, false),
                new Pair<>(new Boolean[] { true, false, false }, Q),
                new Pair<>(new Boolean[] { true, false, true }, true),
                new Pair<>(new Boolean[] { true, true, false }, Q),
                new Pair<>(new Boolean[] { true, true, true }, !Q));
    }
}
