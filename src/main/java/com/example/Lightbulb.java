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
                Arrays.asList(new Point2D(5,
                        43)),
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
            updateMarkerPosition();
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

    public ImageView getImageView() {
        return imageView;
    }

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
    public String getTruthTable() {
        return "A | Q\n" +
                "0 | OFF\n" +
                "1 | ON";
    }

}
