package com.example;

import javafx.scene.layout.Pane;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;

public class CircuitCanvas extends Pane {
    private Line currentLine;
    private LogicGate startGate;
    private Mode currentMode = Mode.WORK;
    private Map<ImageView, List<Circle>> gateMarkers = new HashMap<>();
    private Map<ImageView, LogicGate> gateImageViews = new HashMap<>();
    private Point2D lastMouseCoordinates;
    private ScrollPane scrollPane;

    public enum Mode {
        PAN, WORK
    }

    public CircuitCanvas(double width, double height) {
        super();
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");
        this.setFocusTraversable(true);
        setupModeChangeHandlers();
    }

    private void setupModeChangeHandlers() {
        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case P:
                    currentMode = Mode.PAN;
                    updateMarkersVisibility();
                    // enablePanning();
                    System.out.println("Switched to Pan Mode");
                    break;
                case W:
                    currentMode = Mode.WORK;
                    // updateMarkersVisibility();
                    System.out.println("Switched to Work Mode");
                    break;
            }
        });
    }

    public void enablePanning() {
        setOnMousePressed(event -> {
            if (currentMode == Mode.PAN) {
                lastMouseCoordinates = new Point2D(event.getX(), event.getY());
                requestFocus();
                setCursor(Cursor.CLOSED_HAND);
            }
        });

        setOnMouseDragged(event -> {
            if (currentMode == Mode.PAN && lastMouseCoordinates != null) {
                double deltaX = event.getX() - lastMouseCoordinates.getX();
                double deltaY = event.getY() - lastMouseCoordinates.getY();

                double newTranslateX = getTranslateX() - deltaX;
                double newTranslateY = getTranslateY() - deltaY;
                setTranslateX(newTranslateX);
                setTranslateY(newTranslateY);

                scrollPane.setHvalue(scrollPane.getHvalue() + deltaX / scrollPane.getWidth());
                scrollPane.setVvalue(scrollPane.getVvalue() + deltaY / scrollPane.getHeight());

                lastMouseCoordinates = new Point2D(event.getX(), event.getY());
            }
        });

        setOnMouseReleased(event -> {
            lastMouseCoordinates = null;
        });
    }

    public void drawGate(LogicGate gate, double x, double y) {
        gate.createVisualRepresentation(this);
        gate.setPosition(x, y);
        setupDragHandlers(gate.imageView, gate);
        gateImageViews.put(gate.getImageView(), gate);
    }

    public void setupOutputInteraction(Circle outputMarker, LogicGate gate) {
        outputMarker.setOnMouseClicked(event -> {
            if (currentMode == Mode.WORK && currentLine == null) {
                Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(), outputMarker.getCenterY());
                currentLine = new Line(outputPos.getX(), outputPos.getY(), event.getX(), event.getY());
                currentLine.setStroke(Color.BLUE);
                this.getChildren().add(currentLine);
                gate.addOutputConnection(currentLine);
                showInputMarkers(true, outputMarker);
                setupConnectionHandlers();
                event.consume();
            }
        });
    }

    private boolean finalizeConnection(double x, double y, Circle outputMarker) {
        for (Node node : this.getChildren()) {
            if (node instanceof Circle && node != outputMarker) {
                Circle inputMarker = (Circle) node;
                if (inputMarker.contains(x, y) && inputMarker.getOpacity() == 1.0) {
                    Point2D inputPos = inputMarker.localToParent(inputMarker.getCenterX(), inputMarker.getCenterY());
                    currentLine.setEndX(inputPos.getX());
                    currentLine.setEndY(inputPos.getY());

                    LogicGate targetGate = findGateForInputMarker(inputMarker);
                    int inputIndex = findInputMarkerIndex(targetGate, inputMarker);

                    if (targetGate != null) {
                        targetGate.addInputConnection(currentLine, inputIndex);
                    }

                    return true;
                }
            }
        }
        if (currentLine != null && startGate != null) {
            this.getChildren().remove(currentLine);
            startGate.removeOutputConnection(currentLine);
            currentLine = null;
        }
        return false;
    }

    private LogicGate findGateForInputMarker(Circle inputMarker) {
        for (Map.Entry<ImageView, LogicGate> entry : gateImageViews.entrySet()) {
            LogicGate gate = entry.getValue();
            if (gate.getInputMarkers().contains(inputMarker)) {
                return gate;
            }
        }
        return null;
    }

    private int findInputMarkerIndex(LogicGate gate, Circle inputMarker) {
        return gate.getInputMarkers().indexOf(inputMarker);
    }

    private void showInputMarkers(boolean show, Circle outputMarker) {
        for (Node node : this.getChildren()) {
            if (node instanceof Circle && node != outputMarker) {
                Circle marker = (Circle) node;
                marker.setOpacity(show ? 1.0 : 0.2);
            }
        }
    }

    private void resetInteractionHandlers() {
        this.setOnMouseMoved(null);
        this.setOnMouseClicked(null);
    }

    private void setupDragHandlers(ImageView imageView, LogicGate gate) {
        imageView.setOnMousePressed(event -> {
            if (currentMode == Mode.PAN) {
                double offsetX = event.getSceneX() - imageView.getX();
                double offsetY = event.getSceneY() - imageView.getY();
                imageView.setUserData(new Object[] { offsetX, offsetY, gate });
                imageView.setCursor(Cursor.CLOSED_HAND);
            }
            event.consume();
        });

        imageView.setOnMouseDragged(event -> {
            if (currentMode == Mode.PAN) {
                Object[] data = (Object[]) imageView.getUserData();
                double[] offset = new double[] { (double) data[0], (double) data[1] };
                LogicGate draggedGate = (LogicGate) data[2];
                double newX = event.getSceneX() - offset[0];
                double newY = event.getSceneY() - offset[1];
                draggedGate.handleDrag(newX, newY);
            }
            event.consume();
        });

        imageView.setOnMouseReleased(event -> {
            if (currentMode == Mode.PAN) {
                imageView.setCursor(Cursor.HAND);
            }
            event.consume();
        });
    }

    private void updateMarkers(ImageView gate, double newX, double newY) {
        List<Circle> markers = gateMarkers.get(gate);
        if (markers != null) {
            for (Circle marker : markers) {
                Point2D offset = new Point2D(marker.getCenterX() - gate.getX(), marker.getCenterY() - gate.getY());
                marker.setCenterX(newX + offset.getX());
                marker.setCenterY(newY + offset.getY());
            }
        }
    }

    public void removeGate(ImageView gate) {
        if (gateMarkers.containsKey(gate)) {
            for (Circle marker : gateMarkers.get(gate)) {
                this.getChildren().remove(marker);
            }
            gateMarkers.remove(gate);
        }
        this.getChildren().remove(gate);
    }

    public void removeConnection(Line connection) {
        this.getChildren().remove(connection);
    }

    private void setupConnectionHandlers() {
        this.setOnMouseMoved(mouseMoveEvent -> {
            if (currentMode == Mode.WORK && currentLine != null) {
                currentLine.setEndX(mouseMoveEvent.getX());
                currentLine.setEndY(mouseMoveEvent.getY());
            }
        });

        this.setOnMouseClicked(mouseClickEvent -> {
            if (currentMode == Mode.WORK && currentLine != null && mouseClickEvent.getClickCount() == 1) {
                if (!finalizeConnection(mouseClickEvent.getX(), mouseClickEvent.getY(), null)) {
                    this.getChildren().remove(currentLine);
                }
                currentLine = null;
                showInputMarkers(false, null);
                resetInteractionHandlers();
            }
        });
    }

    private void updateMarkersVisibility() {
        boolean showMarkers = currentMode == Mode.WORK;
        for (Map.Entry<ImageView, LogicGate> entry : gateImageViews.entrySet()) {
            List<Circle> markers = entry.getValue().getInputMarkers();
            if (markers != null) {
                for (Circle marker : markers) {
                    marker.setVisible(showMarkers);
                }
            }
            if (entry.getValue().outputMarker != null) {
                entry.getValue().outputMarker.setVisible(showMarkers);
            }
        }
    }

}
