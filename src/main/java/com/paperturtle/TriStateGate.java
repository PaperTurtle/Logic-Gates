package com.paperturtle;

import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class TriStateGate extends LogicGate {
    public TriStateGate() {
        super("/com/paperturtle/TRISTATE_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    @Override
    public boolean evaluate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'evaluate'");
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTruthTableData'");
    }

}
