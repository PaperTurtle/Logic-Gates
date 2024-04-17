package com.example;

import javafx.geometry.Point2D;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class SwitchGate extends LogicGate implements GateInterface {
    private boolean state = false; // false by default, can be toggled to true
    private Image offImage;
    private Image onImage;

    public SwitchGate() {
        super("/com/example/SWITCH_ANSI_Labelled.svg",
                null,
                new Point2D(70, 25));

        offImage = SvgUtil.loadSvgImage("/com/example/SWITCH_ANSI_Labelled.svg");
        onImage = SvgUtil.loadSvgImage("/com/example/SWITCH_ON_ANSI_Labelled.svg");
        imageView = new javafx.scene.image.ImageView(offImage);
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
            imageView.setImage(state ? onImage : offImage);
        }
    }

    @Override
    public void createVisualRepresentation(Pane canvas) {
        if (imageView != null) {
            canvas.getChildren().add(imageView);
            imageView.setOnMouseClicked(this::handleMouseClicked);
        }
    }

    private void handleMouseClicked(MouseEvent event) {
        toggle();
    }
}
