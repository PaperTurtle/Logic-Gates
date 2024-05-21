package com.paperturtle.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.paperturtle.utils.SvgUtil;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

/**
 * Represents a four-bit digit gate in a digital circuit.
 * A FourBitDigitGate handles four bits of input and output.
 * 
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class FourBitDigitGate extends LogicGate {
    /**
     * The current state of the FourBitDigitGate (true for on, false for off).
     */
    private boolean state = false;

    /**
     * The list of images representing the different states of the FourBitDigitGate.
     */
    private List<Image> images;

    /**
     * The number of active inputs in the previous state of the FourBitDigitGate.
     */
    private int previousActiveCount = 0;

    /**
     * Constructs a FourBitDigitGate object with predefined SVG images and input
     * points.
     */
    public FourBitDigitGate() {
        super(null,
                Arrays.asList(new Point2D(5, 71), new Point2D(5, 53),
                        new Point2D(5, 35), new Point2D(5, 17)),
                null);

        images = Arrays.asList(
                SvgUtil.loadSvgImage("/com/paperturtle/FOURBITDIGIT_ANSI_Labelled.svg"),
                SvgUtil.loadSvgImage("/com/paperturtle/FOURBITDIGIT_1_ANSI_Labelled.svg"),
                SvgUtil.loadSvgImage("/com/paperturtle/FOURBITDIGIT_2_ANSI_Labelled.svg"),
                SvgUtil.loadSvgImage("/com/paperturtle/FOURBITDIGIT_3_ANSI_Labelled.svg"),
                SvgUtil.loadSvgImage("/com/paperturtle/FOURBITDIGIT_4_ANSI_Labelled.svg"));
        imageView = new ImageView(images.get(0));
        initializeMarkers();
    }

    /**
     * Initializes the input markers.
     */
    private void initializeMarkers() {
        inputMarkers.clear();
        inputMarkers.addAll(inputPoints.stream()
                .map(point -> new Circle(point.getX(), point.getY(), 5, Color.BLUE))
                .collect(Collectors.toList()));
    }

    /**
     * Toggles the state of the four-bit digit gate.
     */
    public void toggle() {
        state = !state;
        updateVisualState();
    }

    /**
     * Updates the visual state of the four-bit digit gate.
     */
    private void updateVisualState() {
        if (imageView != null && !inputs.isEmpty()) {
            int activeCount = (int) inputs.stream().filter(LogicGate::getOutput).count();
            if (activeCount != previousActiveCount) {
                previousActiveCount = activeCount;
                imageView.setImage(images.get(Math.min(activeCount, images.size() - 1)));
                updateMarkerPosition();
            }
        }
    }

    @Override
    public void createVisualRepresentation(Pane canvas) {
        if (imageView != null) {
            canvas.getChildren().add(imageView);
        }
        initializeMarkers();
        inputMarkers.forEach(marker -> {
            if (marker != null) {
                canvas.getChildren().add(marker);
            }
        });
        updateMarkerPosition();
    }

    /**
     * Updates the position of the input markers.
     */
    private void updateMarkerPosition() {
        if (imageView != null) {
            for (int i = 0; i < inputMarkers.size(); i++) {
                Circle marker = inputMarkers.get(i);
                Point2D inputPoint = inputPoints.get(i);
                marker.setCenterX(imageView.getX() + inputPoint.getX());
                marker.setCenterY(imageView.getY() + inputPoint.getY());
            }
        }
    }

    @Override
    public boolean evaluate() {
        int activeCount = (int) inputs.stream().filter(LogicGate::getOutput).count();
        boolean newEvaluatedState = activeCount > 0;
        if (newEvaluatedState != state || activeCount != previousActiveCount) {
            state = newEvaluatedState;
        }
        updateVisualState();
        return state;
    }

    @Override
    public void addInput(LogicGate input) {
        super.addInput(input);
        input.addOutputGate(this);
        evaluate();
    }

    @Override
    public void removeInput(LogicGate input) {
        if (inputs.remove(input)) {
            evaluate();
            propagateStateChange();
        }
    }

    /**
     * Gets the image view of the four-bit digit gate.
     * 
     * @return the image view
     */
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void propagateStateChange() {
        if (evaluate()) {
            outputGates.forEach(LogicGate::propagateStateChange);
        }
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] { false }, false));
        list.add(new Pair<>(new Boolean[] { true }, true));
        return list;
    }

    /**
     * Gets the output value of the four-bit digit gate.
     * 
     * @return the output value
     */
    public int getOutputValue() {
        int activeCount = (int) inputs.stream().filter(LogicGate::getOutput).count();
        return Math.min(activeCount, 4);
    }
}
