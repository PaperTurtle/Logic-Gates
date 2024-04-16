package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class NandGate extends LogicGate {
    public NandGate() {
        super("/com/example/NAND_ANSI_Labelled.svg", Arrays.asList(new Point2D(15, 15), new Point2D(15, 35)),
                new Point2D(105, 25));
    }

    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())

            return true; // If no inputs, NAND gate output is true.

        for (LogicGate input : inputs) {
            if (!input.getOutput()) {
                return true;
            }
        }
        return false;
    }
}
