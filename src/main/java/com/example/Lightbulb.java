package com.example;

import java.util.Arrays;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Lightbulb extends LogicGate implements GateInterface {
    private boolean state = false;
    private ImageView imageView;
    private Circle outputMarker;
    private Point2D position;

    private static final String LIGHTBULB_OFF_IMAGE_PATH = "/com/example/LIGHTBULB_ANSI_Labelled.svg";
    private static final String LIGHTBULB_ON_IMAGE_PATH = "/com/example/LIGHTBULB_ON_ANSI_Labelled.svg";

    public Lightbulb() {
        super("/com/example/LIGHTBULB_ANSI_Labelled.svg",
                Arrays.asList(new Point2D(15,
                        25)),
                null);
    }

    public void toggle() {
        state = !state;
        updateVisualState();
    }

    private void updateVisualState() {
        if (state) {
            imageView.setImage(SvgUtil.loadSvgImage(LIGHTBULB_ON_IMAGE_PATH));
            outputMarker.setFill(Color.YELLOW);
        } else {
            imageView.setImage(SvgUtil.loadSvgImage(LIGHTBULB_OFF_IMAGE_PATH));
            outputMarker.setFill(Color.GREY);
        }
    }

    @Override
    public boolean evaluate() {
        return state;
    }

    @Override
    public void addInput(LogicGate input) {
        // Not used for Lightbulb
    }

    @Override
    public void removeInput(LogicGate input) {
        // Not used for Lightbulb
    }

    // Getters for position and visual elements
    public ImageView getImageView() {
        return imageView;
    }

    public Circle getOutputMarker() {
        return outputMarker;
    }

    public Point2D getPosition() {
        return position;
    }
}
