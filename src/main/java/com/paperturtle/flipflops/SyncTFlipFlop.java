package com.paperturtle.flipflops;

import java.util.Arrays;
import java.util.List;

import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class SyncTFlipFlop extends LogicGate {
    private boolean Q = false;
    private boolean lastClockState = false;

    public SyncTFlipFlop() {
        super("/com/paperturtle/SYNC_T_FLIPFLOP_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(10, 10), new Point2D(10, 30)),
                new Point2D(90, 20));
    }

    @Override
    public boolean evaluate() {
        boolean T = inputs.get(0).getOutput();
        boolean clock = inputs.get(1).getOutput();

        if (clock && !lastClockState) {
            if (T) {
                Q = !Q;
            }
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
