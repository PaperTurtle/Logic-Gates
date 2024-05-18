package com.paperturtle.flipflops;

import java.util.Arrays;
import java.util.List;

import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class LevelDFlipFlop extends LogicGate {
    private boolean Q = false;

    public LevelDFlipFlop() {
        super("/com/paperturtle/LEVEL_D_FLIPFLOP_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(10, 10), new Point2D(10, 30)),
                new Point2D(90, 20));
    }

    @Override
    public boolean evaluate() {
        boolean D = inputs.get(0).getOutput();
        boolean clock = inputs.get(1).getOutput();

        if (clock) {
            Q = D;
        }
        return Q;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Arrays.asList(
                new Pair<>(new Boolean[] { false, false }, Q),
                new Pair<>(new Boolean[] { false, true }, false),
                new Pair<>(new Boolean[] { true, false }, Q),
                new Pair<>(new Boolean[] { true, true }, true));
    }
}
