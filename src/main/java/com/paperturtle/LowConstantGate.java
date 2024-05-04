package com.paperturtle;

import java.util.Collections;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class LowConstantGate extends LogicGate {

    public LowConstantGate() {
        super("/com/paperturtle/LOWCONSTANT_ANSI_Labelled.svg",
                null,
                new Point2D(60, 20));
    }

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Collections.singletonList(new Pair<>(new Boolean[] {}, true));
    }

}
