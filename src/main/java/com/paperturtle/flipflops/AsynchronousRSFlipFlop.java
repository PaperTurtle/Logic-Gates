package com.paperturtle.flipflops;

import java.util.Arrays;
import java.util.List;

import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class AsynchronousRSFlipFlop extends LogicGate {
    private boolean Q = false;

    public AsynchronousRSFlipFlop() {
        super("/com/paperturtle/ASYNC_RS_FLIPFLOP_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(10, 10), new Point2D(10, 30)),
                new Point2D(90, 20));
    }

    @Override
    public boolean evaluate() {
        boolean R = inputs.get(0).getOutput();
        boolean S = inputs.get(1).getOutput();

        if (R && S) {
            return Q;
        } else if (R) {
            Q = false;
        } else if (S) {
            Q = true;
        }
        return Q;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Arrays.asList(
                new Pair<>(new Boolean[] { false, false }, Q),
                new Pair<>(new Boolean[] { false, true }, true),
                new Pair<>(new Boolean[] { true, false }, false),
                new Pair<>(new Boolean[] { true, true }, Q));
    }
}