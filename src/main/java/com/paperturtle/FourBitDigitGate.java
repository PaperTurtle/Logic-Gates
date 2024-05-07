package com.paperturtle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

/**
 * The FourBitDigitGate class extends the LogicGate class and represents a
 * four-bit digit gate in a digital circuit.
 * 
 * A FourBitDigitGate is a digital logic gate that handles four bits of input
 * and output.
 * 
 * The class constructor sets the SVG image representing the gate, the list of
 * input points, and the output point.
 * 
 * The evaluate method overrides the abstract method in the LogicGate class. It
 * checks the inputs and returns the corresponding four-bit output.
 * 
 * The state field represents the current state of the gate (true or false).
 * 
 * The images field is a list of images that represent the different states of
 * the gate.
 * 
 * The previousActiveCount field keeps track of the number of active inputs from
 * the previous state of the gate.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class FourBitDigitGate extends LogicGate {
    private boolean state = false;
    private List<Image> images;
    private int previousActiveCount = 0;

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
        for (Point2D point : inputPoints) {
            Circle marker = new Circle(point.getX(), point.getY(), 5, Color.BLUE);
            inputMarkers.add(marker);
        }

    }

    public void toggle() {
        state = !state;
        updateVisualState();
    }

    private void updateVisualState() {
        if (imageView != null && !inputs.isEmpty()) {
            int activeCount = (int) inputs.stream().filter(LogicGate::getOutput).count();
            if (activeCount != previousActiveCount) {
                previousActiveCount = activeCount;
                int index = Math.min(activeCount, images.size() - 1);
                imageView.setImage(images.get(index));
                updateMarkerPosition();
            }
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
        updateMarkerPosition();
    }

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

    @Override
    public boolean evaluate() {
        int activeCount = (int) inputs.stream().filter(LogicGate::getOutput).count();
        boolean newEvaluatedState = activeCount > 0;
        if (newEvaluatedState != state || activeCount != previousActiveCount) {
            state = newEvaluatedState;
            updateVisualState();
        }
        return state;
    }

    @Override
    public void addInput(LogicGate input) {
        super.addInput(input);
        input.addOutputGate(this);
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

    public ImageView getImageView() {
        return imageView;
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
