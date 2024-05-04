package com.paperturtle;

import java.util.Arrays;
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
        throw new UnsupportedOperationException("Unimplemented method 'evaluate'");
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        throw new UnsupportedOperationException("Unimplemented method 'getTruthTableData'");
    }

}
