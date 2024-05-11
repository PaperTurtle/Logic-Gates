package com.paperturtle;

import java.util.Arrays;
import java.util.List;

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
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

public class ClockGate extends LogicGate {
    private boolean state = false;
    private boolean isRunning = true;
    private Image offImage;
    private Image onImage;
    private Timeline timeline;
    private double signalDuration = 1.0;

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

    private void setupClock(double duration) {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(duration), event -> toggle()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void toggle() {
        state = !state;
        updateOutputConnectionsColor();
        updateVisualState();
        propagateStateChange();
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

    private void updateOutputConnectionsColor() {
        javafx.scene.paint.Color lineColor = state ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.BLACK;
        for (javafx.scene.shape.Line line : outputConnections) {
            line.setStroke(lineColor);
        }
    }

    public void stopClock() {
        if (timeline != null) {
            timeline.stop();
            isRunning = false;
        }
    }

    public void startClock() {
        if (timeline != null) {
            timeline.play();
            isRunning = true;
        }
    }

    public double getSignalDuration() {
        return signalDuration;
    }

    public void setSignalDuration(double duration) {
        signalDuration = duration;
        if (timeline != null) {
            timeline.stop();
            timeline.getKeyFrames().set(0, new KeyFrame(Duration.seconds(duration), event -> toggle()));
            timeline.play();
        }
    }

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
}
