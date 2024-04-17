package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class SwitchGate extends LogicGate implements GateInterface {
    private boolean state = false; // false by default, can be toggled to true

    public SwitchGate() {
        super("/com/example/SWITCH_ANSI_Labelled.svg",
                null,
                new Point2D(105, 25));
    }

    @Override
    public boolean evaluate() {
        return state;
    }

    @Override
    public void toggle() {
        state = !state;
        updateVisualState();
    }

    private void updateVisualState() {
        if (imageView != null) {
            imageView.setEffect(new ColorAdjust(0, 0, state ? 0.5 : -0.5, 0));
        }
    }

    @Override
    public void createVisualRepresentation(Pane canvas) {
        super.createVisualRepresentation(canvas);
        imageView.setOnMouseClicked(this::handleMouseClicked);
    }

    private void handleMouseClicked(MouseEvent event) {
        toggle();
    }
}
