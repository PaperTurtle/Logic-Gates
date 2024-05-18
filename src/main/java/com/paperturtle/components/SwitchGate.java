package com.paperturtle.components;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import java.util.List;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.commands.ToggleSwitchStateCommand;
import com.paperturtle.managers.CommandManager;
import com.paperturtle.utils.SvgUtil;

import java.util.ArrayList;
import javafx.util.Pair;

/**
 * Represents a Switch gate in a digital circuit.
 * A Switch gate toggles its state (on or off) when clicked.
 * 
 * @see LogicGate
 * @see Point2D
 * @see Pair
 * @see SvgUtil
 * @see CircuitCanvas
 * 
 * @author Seweryn Czabanowski
 */
public class SwitchGate extends LogicGate {
    /**
     * The current state of the switch gate (true for on, false for off).
     */
    private boolean state = false;

    /**
     * The image representing the off state of the switch gate.
     */
    private Image offImage;

    /**
     * The image representing the on state of the switch gate.
     */
    private Image onImage;

    /**
     * A flag indicating whether the switch gate is currently selected.
     */
    private boolean isSelected = false;

    /**
     * The canvas on which the switch gate is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * Constructs a SwitchGate object with predefined SVG images and output point.
     */
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

    /**
     * Toggles the state of the Switch gate.
     */
    public void toggle() {
        CommandManager commandManager = canvas.getCommandManager();
        commandManager.executeCommand(new ToggleSwitchStateCommand(this));
        propagateStateChange();
    }

    /**
     * Updates the color of the output connections based on the state.
     */
    public void updateOutputConnectionsColor() {
        Color lineColor = state ? Color.RED : Color.BLACK;
        for (Line line : outputConnections) {
            line.setStroke(lineColor);
        }
    }

    /**
     * Updates the visual state of the Switch gate.
     */
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

    /**
     * Updates the position of the output marker.
     */
    private void updateMarkerPosition() {
        if (imageView != null && outputMarker != null) {
            outputMarker.setCenterX(imageView.getX() + outputPoint.getX());
            outputMarker.setCenterY(imageView.getY() + outputPoint.getY());
        }
    }

    /**
     * Handles the mouse click event on the Switch gate.
     */
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

    /**
     * Sets the selection state of the Switch gate.
     * 
     * @param selected true if the gate is selected, false otherwise.
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * Returns the selection state of the Switch gate.
     * 
     * @return true if the gate is selected, false otherwise.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Returns the current state of the Switch gate.
     * 
     * @return the current state of the gate.
     */
    public boolean getState() {
        return state;
    }

    /**
     * Sets the state of the Switch gate.
     * 
     * @param state the state to set.
     */
    public void setState(boolean state) {
        this.state = state;
        updateVisualState();
        updateOutputConnectionsColor();
    }

}
