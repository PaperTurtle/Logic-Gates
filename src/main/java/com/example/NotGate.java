package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class NotGate extends LogicGate {
    public NotGate() {
        super("/com/example/NOT_ANSI_Labelled.svg", Arrays.asList(new Point2D(15, 25)),
                new Point2D(105, 25));
    }

    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return true; // Default to true when no input (typically NOT gates have one input).

        // NOT gate inverts its single input
        return !inputs.get(0).getOutput();
    }
}