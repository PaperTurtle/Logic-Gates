package com.paperturtle.components.gates;

import java.util.Arrays;
import java.util.List;

import com.paperturtle.components.LogicGate;

import java.util.ArrayList;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * Represents a Buffer gate in a digital circuit.
 * A Buffer gate outputs the same value as its input.
 * 
 * @see LogicGate
 * @see Point2D
 * @see Pair
 * 
 * @author Seweryn Czabanowski
 */
public class BufferGate extends LogicGate {
    /**
     * Constructs a BufferGate object with predefined SVG image, input points, and
     * output point.
     */
    public BufferGate() {
        super("/com/paperturtle/BUFFER_ANSI_Labelled.svg",
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
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false }, false));
        list.add(new Pair<>(new Boolean[] { true }, true));
        return list;
    }

}