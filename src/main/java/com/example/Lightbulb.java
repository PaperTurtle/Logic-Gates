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
    private Image offImage;
    private Image onImage;

    public Lightbulb() {
        super(null,
                Arrays.asList(new Point2D(15,
                        25)),
                null);
        offImage = SvgUtil.loadSvgImage("/com/example/LIGHTBULB_ANSI_Labelled.svg");
        onImage = SvgUtil.loadSvgImage("/com/example/LIGHTBULB_ON_ANSI_Labelled.svg");
        imageView = new javafx.scene.image.ImageView(offImage);
        for (Point2D point : inputPoints) {
            Circle marker = new Circle(point.getX(), point.getY(), 5, Color.BLUE);
            inputMarkers.add(marker);
        }

    }

    public void toggle() {
        state = !state;
        updateVisualState();
    }

    public void toggleLight(boolean currentState) {
        this.state = currentState;
        updateVisualState();
    }

    private void updateVisualState() {
        if (imageView != null) {
            imageView.setImage(state ? onImage : offImage);
        }
    }

    @Override
    public void createVisualRepresentation(Pane canvas) {
        if (imageView != null) {
            canvas.getChildren().add(imageView);
        }
        for (Circle marker : inputMarkers) {
            if (marker != null) {
                canvas.getChildren().add(marker);
            }
        }
    }

    // private void updateMarkerPosition() {
    // if (imageView != null && outputMarker != null) {
    // outputMarker.setCenterX(imageView.getX() + outputPoint.getX());
    // outputMarker.setCenterY(imageView.getY() + outputPoint.getY());
    // }
    // }

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
}
