package com.paperturtle;

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

public class SwitchGate extends LogicGate {
    private boolean state = false;
    private Image offImage;
    private Image onImage;
    private boolean isSelected = false;
    private CircuitCanvas canvas;

    public SwitchGate() {
        super(null,
                null,
                new Point2D(70, 25));

        offImage = SvgUtil.loadSvgImage("/com/paperturtle/SWITCH_ANSI_Labelled.svg");
        onImage = SvgUtil.loadSvgImage("/com/paperturtle/SWITCH_ON_ANSI_Labelled.svg");
        imageView = new javafx.scene.image.ImageView(offImage);
        outputMarker = new Circle(outputPoint.getX(), outputPoint.getY(), 5, Color.RED);
    }

    @Override
    public boolean evaluate() {
        return state;
    }

    public void toggle() {
        CommandManager commandManager = canvas.getCommandManager();
        commandManager.executeCommand(new ToggleSwitchStateCommand(this));
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
        this.canvas = (CircuitCanvas) canvas;
        if (imageView != null) {
            canvas.getChildren().add(imageView);
            imageView.setOnMouseClicked(this::handleMouseClicked);
        }
        if (outputMarker != null) {
            canvas.getChildren().add(outputMarker);
            ((CircuitCanvas) canvas).getInteractionManager().setupOutputInteraction(outputMarker, this);
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
        if (!isSelected) {
            isSelected = true;
        } else {
            toggle();
        }
        event.consume();
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
        list.add(new Pair<>(new Boolean[] {}, false));
        list.add(new Pair<>(new Boolean[] {}, true));
        return list;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
        updateVisualState();
        updateOutputConnectionsColor();
    }

}
