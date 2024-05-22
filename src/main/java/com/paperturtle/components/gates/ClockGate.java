package com.paperturtle.components.gates;

import java.util.Arrays;
import java.util.List;

import com.paperturtle.components.LogicGate;
import com.paperturtle.gui.CircuitCanvas;
import com.paperturtle.utils.SvgUtil;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

/**
 * Represents a Clock gate in a digital circuit.
 * A Clock gate oscillates between on and off states at a specified interval.
 * 
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class ClockGate extends LogicGate {
    /**
     * The current state of the ClockGate (true for on, false for off).
     */
    private boolean state = false;

    /**
     * A flag indicating whether the ClockGate is currently running.
     */
    private boolean isRunning = true;

    /**
     * The image representing the off state of the ClockGate.
     */
    private Image offImage;

    /**
     * The image representing the on state of the ClockGate.
     */
    private Image onImage;

    /**
     * The timeline used for scheduling the state changes of the ClockGate.
     */
    private Timeline timeline;

    /**
     * The duration of the signal produced by the ClockGate, in seconds.
     */
    private double signalDuration = 1.0;

    /**
     * Constructs a ClockGate object with predefined SVG images and output point.
     */
    public ClockGate() {
        super(null,
                null,
                new Point2D(105, 20));
        offImage = SvgUtil.loadSvgImage("/com/paperturtle/CLOCK_ANSI_Labelled.svg");
        onImage = SvgUtil.loadSvgImage("/com/paperturtle/CLOCK_ON_ANSI_Labelled.svg");
        imageView = new javafx.scene.image.ImageView(offImage);
        outputMarker = new Circle(outputPoint.getX(), outputPoint.getY(), 5, Color.RED);
        setupClock(signalDuration);
    }

    @Override
    public boolean evaluate() {
        return state;
    }

    @Override
    public List<Pair<Boolean[], Boolean>> getTruthTableData() {
        return Arrays.asList(
                new Pair<>(new Boolean[] {}, false),
                new Pair<>(new Boolean[] {}, true));
    }

    /**
     * Sets up the clock with the specified duration.
     * 
     * @param duration the duration for the clock signal
     */
    private void setupClock(double duration) {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(duration), event -> toggle()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Toggles the state of the clock gate.
     */
    private void toggle() {
        state = !state;
        updateOutputConnectionsColor();
        updateVisualState();
        propagateStateChange();
    }

    /**
     * Updates the visual state of the clock gate.
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
     * Updates the color of the output connections.
     */
    private void updateOutputConnectionsColor() {
        Color lineColor = state ? Color.RED : Color.BLACK;
        for (Line line : outputConnections) {
            line.setStroke(lineColor);
        }
    }

    /**
     * Stops the clock.
     */
    public void stopClock() {
        if (timeline != null) {
            timeline.stop();
            isRunning = false;
        }
    }

    /**
     * Starts the clock.
     */
    public void startClock() {
        if (timeline != null) {
            timeline.play();
            isRunning = true;
        }
    }

    /**
     * Gets the signal duration.
     * 
     * @return the signal duration
     */
    public double getSignalDuration() {
        return signalDuration;
    }

    /**
     * Sets the signal duration.
     * 
     * @param duration the new signal duration
     */
    public void setSignalDuration(double duration) {
        signalDuration = duration;
        if (timeline != null) {
            timeline.stop();
            timeline.getKeyFrames().set(0, new KeyFrame(Duration.seconds(duration), event -> toggle()));
            timeline.play();
        }
    }

    /**
     * Shows a dialog to edit the clock signal duration.
     */
    public void showTimeEditDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Clock Signal Duration");
        alert.setHeaderText("Adjust the duration for the clock signal.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField durationField = new TextField(String.valueOf(this.getSignalDuration()));
        durationField.setPromptText("Enter signal duration in seconds");

        grid.add(new Label("Signal Duration (seconds):"), 0, 0);
        grid.add(durationField, 1, 0);

        Button toggleButton = new Button(isRunning ? "Pause" : "Resume");
        toggleButton.setOnAction(e -> {
            if (isRunning) {
                stopClock();
                toggleButton.setText("Resume");
            } else {
                startClock();
                toggleButton.setText("Pause");
            }
        });
        grid.add(toggleButton, 1, 1);

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();

        try {
            double duration = Double.parseDouble(durationField.getText());
            if (duration > 0) {
                this.setSignalDuration(duration);
                alert.close();
            } else {
                durationField.setText("Enter a positive number!");
            }
        } catch (NumberFormatException nfe) {
            durationField.setText("Invalid input!");
        }
    }

    @Override
    public void propagateStateChange() {
        if (!outputConnections.isEmpty()) {
            super.propagateStateChange();
        }
    }
}
