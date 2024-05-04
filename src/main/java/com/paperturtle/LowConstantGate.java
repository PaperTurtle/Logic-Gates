package com.paperturtle;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

public class LowConstantGate extends LogicGate {

    public LowConstantGate() {
        super("/com/paperturtle/LOWCONSTANT_ANSI_Labelled.svg",
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
