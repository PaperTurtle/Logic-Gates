package com.paperturtle.flipflops;

import java.util.Arrays;
import java.util.List;

import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class SyncRSFlipFlop extends LogicGate {
    private boolean Q = false;

    public SyncRSFlipFlop() {
        super("/com/paperturtle/SYNC_RS_FLIPFLOP_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(10, 10), new Point2D(10, 30), new Point2D(10, 50)),
                new Point2D(90, 20));
    }

    @Override
    public boolean evaluate() {
        boolean R = inputs.get(0).getOutput();
        boolean S = inputs.get(1).getOutput();
        boolean clock = inputs.get(2).getOutput();

        if (clock) {
            if (R && S) {
                return Q;
            } else if (R) {
                Q = false;
            } else if (S) {
                Q = true;
            }
        }
        return Q;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Arrays.asList(
                new Pair<>(new Boolean[] { false, false, false }, Q),
                new Pair<>(new Boolean[] { false, false, true }, Q),
                new Pair<>(new Boolean[] { false, true, false }, Q),
                new Pair<>(new Boolean[] { false, true, true }, true),
                new Pair<>(new Boolean[] { true, false, false }, Q),
                new Pair<>(new Boolean[] { true, false, true }, false),
                new Pair<>(new Boolean[] { true, true, false }, Q),
                new Pair<>(new Boolean[] { true, true, true }, Q));
    }
}
