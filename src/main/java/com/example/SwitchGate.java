package com.example;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import java.util.List;
import java.util.ArrayList;
import javafx.util.Pair;

public class SwitchGate extends LogicGate implements GateInterface {
    private boolean state = false;
    private Image offImage;
    private Image onImage;

    public SwitchGate() {
        super(null,
                null,
                new Point2D(70, 25));

        offImage = SvgUtil.loadSvgImage("/com/example/SWITCH_ANSI_Labelled.svg");
        onImage = SvgUtil.loadSvgImage("/com/example/SWITCH_ON_ANSI_Labelled.svg");
        imageView = new javafx.scene.image.ImageView(offImage);
        outputMarker = new Circle(outputPoint.getX(), outputPoint.getY(), 5, Color.RED);
    }

    @Override
    public boolean evaluate() {
        return state;
    }

    @Override
    public void toggle() {
        state = !state;
        updateVisualState();
        updateOutputConnectionsColor();
        propagateStateChange();
    }

    public void updateOutputConnectionsColor() {
        Color lineColor = state ? Color.RED : Color.BLACK;
        for (Line line : outputConnections) {
            line.setStroke(lineColor);
        }
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
            imageView.setOnMouseClicked(this::handleMouseClicked);
        }
        if (outputMarker != null) {
            canvas.getChildren().add(outputMarker);
            ((CircuitCanvas) canvas).setupOutputInteraction(outputMarker, this);
            updateMarkerPosition();
        }
    }

    private void updateMarkerPosition() {
        if (imageView != null && outputMarker != null) {
            outputMarker.setCenterX(imageView.getX() + outputPoint.getX());
            outputMarker.setCenterY(imageView.getY() + outputPoint.getY());
        }
    }

    private void handleMouseClicked(MouseEvent event) {
        toggle();
    }

    @Override
    public void propagateStateChange() {
        for (LogicGate outputGate : outputGates) {
            outputGate.evaluate();
            outputGate.propagateStateChange();
        }
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        List<Pair<Boolean[], Boolean>> list = new ArrayList<>();
        list.add(new Pair<>(new Boolean[] {}, false)); // Initially off
        list.add(new Pair<>(new Boolean[] {}, true)); // Can be toggled on
        return list;
    }

}
