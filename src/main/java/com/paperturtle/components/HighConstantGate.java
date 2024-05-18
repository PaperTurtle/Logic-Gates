package com.paperturtle.components;

import java.util.Collections;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class HighConstantGate extends LogicGate {

    public HighConstantGate() {
        super("/com/paperturtle/HIGHCONSTANT_ANSI_Labelled.svg",
                null,
                new Point2D(60, 20));
    }

    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Collections.singletonList(new Pair<>(new Boolean[] {}, false));
    }

}
