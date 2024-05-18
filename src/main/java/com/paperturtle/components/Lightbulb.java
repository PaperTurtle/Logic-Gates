package com.paperturtle.components;

import java.util.Arrays;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.List;

import com.paperturtle.utils.SvgUtil;

import java.util.ArrayList;

import javafx.util.Pair;

/**
 * Represents a Lightbulb in a digital circuit.
 * A Lightbulb gate displays a visual representation of its state (on or off).
 * 
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class Lightbulb extends LogicGate {
    /**
     * The current state of the lightbulb.
     */
    private boolean state = false;

    /**
     * The image representing the off state of the lightbulb.
     */
    private Image offImage;

    /**
     * The image representing the on state of the lightbulb.
     */
    private Image onImage;

    /**
     * Constructs a Lightbulb object with predefined SVG images and input points.
     */
    public Lightbulb() {
        super(null,
                Arrays.asList(new Point2D(5,
                        43)),
                null);
        offImage = SvgUtil.loadSvgImage("/com/paperturtle/LIGHTBULB_ANSI_Labelled.svg");
        onImage = SvgUtil.loadSvgImage("/com/paperturtle/LIGHTBULB_ON_ANSI_Labelled.svg");
        imageView = new javafx.scene.image.ImageView(offImage);
        for (Point2D point : inputPoints) {
            Circle marker = new Circle(point.getX(), point.getY(), 5, Color.BLUE);
            inputMarkers.add(marker);
        }
    }

    /**
     * Initializes the input markers for the Lightbulb.
     */
    private void initializeMarkers() {
        inputMarkers.clear();
        for (Point2D point : inputPoints) {
            Circle marker = new Circle(point.getX(), point.getY(), 5, Color.BLUE);
            inputMarkers.add(marker);
        }
    }

    /**
     * Toggles the state of the Lightbulb.
     */
    public void toggle() {
        state = !state;
        updateVisualState();
    }

    /**
     * Toggles the light based on the current state.
     * 
     * @param currentState the current state of the Lightbulb.
     */
    public void toggleLight(boolean currentState) {
        this.state = currentState;
        updateVisualState();
    }

    /**
     * Updates the visual state of the Lightbulb.
     */
    private void updateVisualState() {
        if (imageView != null) {
            imageView.setImage(state ? onImage : offImage);
            updateMarkerPosition();
        }
    }

    @Override
    public void createVisualRepresentation(Pane canvas) {
        if (imageView != null) {
            canvas.getChildren().add(imageView);
        }
        initializeMarkers();
        for (Circle marker : inputMarkers) {
            if (marker != null) {
                canvas.getChildren().add(marker);
            }
        }
        updateMarkerPosition();
    }

    /**
     * Updates the position of the input markers.
     */
    private void updateMarkerPosition() {
        if (imageView != null && inputMarkers != null) {
            for (int i = 0; i < inputMarkers.size(); i++) {
                Circle marker = inputMarkers.get(i);
                Point2D inputPoint = inputPoints.get(i);
                marker.setCenterX(imageView.getX() + inputPoint.getX());
                marker.setCenterY(imageView.getY() + inputPoint.getY());
            }
        }
    }

    /**
     * Evaluates the state of the Lightbulb based on its inputs.
     * 
     * @return true if at least one of the inputs is true, false otherwise.
     */
    @Override
    public boolean evaluate() {
        boolean newEvaluatedState = inputs.stream().anyMatch(LogicGate::getOutput);
        if (newEvaluatedState != state) {
            state = newEvaluatedState;
            updateVisualState();
        }
        return state;
    }

    @Override
    public void addInput(LogicGate input) {
        super.addInput(input);
        this.evaluate();
        this.updateVisualState();
    }

    @Override
    public void removeInput(LogicGate input) {
        if (inputs.contains(input)) {
            inputs.remove(input);
            evaluate();
            updateVisualState();
        }
    }

    /**
     * Gets the ImageView object representing the Lightbulb.
     * 
     * @return the ImageView object representing the Lightbulb.
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     * Gets the output marker of the Lightbulb.
     * 
     * @return the output marker of the Lightbulb.
     */
    public Circle getOutputMarker() {
        return outputMarker;
    }

    @Override
    public void propagateStateChange() {
        if (evaluate()) {
            for (LogicGate gate : outputGates) {
                gate.propagateStateChange();
            }
        }
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false }, false));
        list.add(new Pair<>(new Boolean[] { true }, true));
        return list;
    }

}
