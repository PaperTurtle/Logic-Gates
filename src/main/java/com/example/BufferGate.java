package com.example;

import java.util.Arrays;
import javafx.geometry.Point2D;

/**
 * The BufferGate class extends the LogicGate class and represents a Buffer gate
 * in
 * a digital circuit.
 * A Buffer gate is a digital logic gate that outputs the same value as its
 * input.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * checks the input and returns the same value.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class BufferGate extends LogicGate {
    /**
     * Constructs a BufferGate object with predefined SVG image, input points, and
     * output point.
     */
    public BufferGate() {
        super("/com/example/BUFFER_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15, 25)),
                new Point2D(105, 25));
    }

    /**
     * Evaluates the state of the Buffer gate based on its input.
     * 
     * @return the same value as the input.
     */
    @Override
    public boolean evaluate() {
        if (inputs.isEmpty())
            return false;

        return inputs.get(0).getOutput();
    }

    @Override
    public String getTruthTable() {
        return "A | Q\n" +
                "0 | 0\n" +
                "1 | 1";
    }

}