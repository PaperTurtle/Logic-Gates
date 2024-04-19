package com.example;

import javafx.scene.layout.Pane;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;

public class CircuitCanvas extends Pane {
    private Line currentLine;
    private LogicGate startGate;
    private Mode currentMode = Mode.WORK;
    private Map<ImageView, List<Circle>> gateMarkers = new HashMap<>();
    private Map<ImageView, LogicGate> gateImageViews = new HashMap<>();
    private Point2D lastMouseCoordinates;
    private ScrollPane scrollPane;
    private Map<Line, LogicGate> lineToStartGateMap = new HashMap<>();
    private LogicGate highlightedGate = null;
    private ContextMenu openContextMenu = null;
    private Set<LogicGate> gatesToBeUpdated = new HashSet<>();

    public enum Mode {
        PAN, WORK
    }

    public CircuitCanvas(double width, double height, ScrollPane scrollPane) {
        super();
        this.scrollPane = scrollPane;
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");
        this.setFocusTraversable(true);
        setupModeChangeHandlers();

        this.setOnMouseClicked(event -> {
            if (highlightedGate != null) {
                highlightedGate.unhighlight();
                highlightedGate = null;
            }
            this.requestFocus();
        });
    }

    private void setupModeChangeHandlers() {
        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case P:
                    currentMode = Mode.PAN;
                    this.setCursor(Cursor.OPEN_HAND);
                    updateMarkersVisibility();
                    System.out.println("Switched to Pan Mode");
                    break;
                case W:
                    currentMode = Mode.WORK;
                    this.setCursor(Cursor.DEFAULT);
                    updateMarkersVisibility();
                    System.out.println("Switched to Work Mode");
                    break;
            }
        });
    }

    public void drawGate(LogicGate gate, double x, double y) {
        gate.createVisualRepresentation(this);
        gate.setPosition(x, y);
        setupDragHandlers(gate.imageView, gate);
        gateImageViews.put(gate.getImageView(), gate);
        if (gate instanceof SwitchGate) {
            ((SwitchGate) gate).updateOutputConnectionsColor();
        }
    }

    public void setupOutputInteraction(Circle outputMarker, LogicGate gate) {
        outputMarker.setOnMouseClicked(event -> {
            if (currentMode == Mode.WORK && currentLine == null) {
                Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(), outputMarker.getCenterY());
                currentLine = new Line(outputPos.getX(), outputPos.getY(), event.getX(), event.getY());
                currentLine.setStroke(Color.BLACK);
                this.getChildren().add(currentLine);
                lineToStartGateMap.put(currentLine, gate);
                startGate = gate;
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
                    LogicGate sourceGate = lineToStartGateMap.get(currentLine);
                    if (targetGate != null && sourceGate != null) {
                        int inputIndex = findInputMarkerIndex(targetGate, inputMarker);
                        targetGate.addInputConnection(currentLine, inputIndex);
                        targetGate.addInput(sourceGate);
                        sourceGate.addOutputConnection(currentLine);
                        sourceGate.addOutputGate(targetGate);
                        scheduleUpdate(targetGate);
                        lineToStartGateMap.remove(currentLine);
                        startGate = null;
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
        imageView.setPickOnBounds(true);

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

        imageView.setOnMousePressed(event -> {
            if (highlightedGate != null && highlightedGate != gate) {
                highlightedGate.unhighlight();
            }
            highlightedGate = gate;
            gate.highlight();

            if (event.getButton() == MouseButton.SECONDARY) {
                if (openContextMenu != null) {
                    openContextMenu.hide();
                }
                ContextMenu contextMenu = new ContextMenu();
                MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction(e -> {
                    removeGate(imageView);
                    if (highlightedGate == gate) {
                        highlightedGate = null;
                    }
                });
                MenuItem propertiesItem = new MenuItem("Properties");
                propertiesItem.setOnAction(e -> showPropertiesDialog(gate));
                contextMenu.getItems().addAll(deleteItem, propertiesItem);

                contextMenu.show(imageView, event.getScreenX(), event.getScreenY());
                contextMenu.setAutoHide(false);
                contextMenu.setOnShown(e -> contextMenu.requestFocus());

                openContextMenu = contextMenu;
                event.consume();
            }

            if (event.getButton() == MouseButton.PRIMARY && currentMode == Mode.PAN) {
                double offsetX = event.getSceneX() - imageView.getX();
                double offsetY = event.getSceneY() - imageView.getY();
                imageView.setUserData(new Object[] { offsetX, offsetY, gate });
                imageView.setCursor(Cursor.CLOSED_HAND);
                event.consume();
            }
        });

        imageView.setOnMouseReleased(event -> {
            if (currentMode == Mode.PAN && event.getButton() == MouseButton.PRIMARY) {
                imageView.setCursor(Cursor.HAND);
            }
        });

        this.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof ImageView)) {
                if (highlightedGate != null) {
                    highlightedGate.unhighlight();
                    highlightedGate = null;
                }
                if (openContextMenu != null) {
                    openContextMenu.hide();
                    openContextMenu = null;
                }
                this.requestFocus();
            }
        });
    }

    private void showPropertiesDialog(LogicGate gate) {
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

    public void propagateUpdates() {
        while (!gatesToBeUpdated.isEmpty()) {
            Set<LogicGate> currentBatch = new HashSet<>(gatesToBeUpdated);
            gatesToBeUpdated.clear();
            for (LogicGate gate : currentBatch) {
                gate.propagateStateChange();
            }
        }
    }

    public void scheduleUpdate(LogicGate gate) {
        gatesToBeUpdated.add(gate);
    }
}
