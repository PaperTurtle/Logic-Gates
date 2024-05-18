package com.paperturtle.managers;

import java.util.Map;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.components.LogicGate;
import com.paperturtle.components.SwitchGate;
import com.paperturtle.components.TextLabel;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SelectionManager {
    private CircuitCanvas canvas;
    private Rectangle selectionRect = new Rectangle();
    private boolean isSelecting = false;

    public SelectionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    public void initializeSelectionMechanism() {
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.BLUE.deriveColor(0, 1.2, 1, 0.2));
        selectionRect.setVisible(false);
        selectionRect.getStyleClass().add("selection-rectangle");
        canvas.getChildren().add(selectionRect);
        final double dragThreshold = 10.0;

        canvas.setOnMousePressed(event -> {
            double startX = Math.max(0, Math.min(event.getX(), canvas.getWidth()));
            double startY = Math.max(0, Math.min(event.getY(), canvas.getHeight()));
            canvas.setLastMouseCoordinates(new Point2D(startX, startY));
            selectionRect.setX(startX);
            selectionRect.setY(startY);
            selectionRect.setWidth(0);
            selectionRect.setHeight(0);
            selectionRect.setVisible(true);
            isSelecting = true;
        });

        canvas.setOnMouseDragged(event -> {
            if (isSelecting) {
                double endX = Math.max(0, Math.min(event.getX(), canvas.getWidth()));
                double endY = Math.max(0, Math.min(event.getY(), canvas.getHeight()));
                double minX = Math.min(canvas.getLastMouseCoordinates().getX(), endX);
                double maxX = Math.max(canvas.getLastMouseCoordinates().getX(), endX);
                double minY = Math.min(canvas.getLastMouseCoordinates().getY(), endY);
                double maxY = Math.max(canvas.getLastMouseCoordinates().getY(), endY);

                selectionRect.setX(minX);
                selectionRect.setY(minY);
                selectionRect.setWidth(maxX - minX);
                selectionRect.setHeight(maxY - minY);
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (isSelecting && (selectionRect.getWidth() > dragThreshold
                    || selectionRect.getHeight() > dragThreshold)) {
                this.selectGatesInRectangle();
            }
            selectionRect.setVisible(false);
            canvas.getInteractionManager().setJustSelected(false);
            isSelecting = false;
        });
    }

    public void selectGatesInRectangle() {
        for (Map.Entry<ImageView, LogicGate> entry : canvas.getGateImageViews().entrySet()) {
            ImageView gateView = entry.getKey();
            LogicGate gate = entry.getValue();

            boolean intersects = gateView.getBoundsInParent().intersects(selectionRect.getBoundsInParent());
            boolean isSelected = gateView.getStyleClass().contains("selected");

            if (intersects && !isSelected) {
                gateView.getStyleClass().add("selected");
                if (gate instanceof SwitchGate) {
                    ((SwitchGate) gate).setSelected(true);
                }
            } else if (!intersects && isSelected) {
                gateView.getStyleClass().remove("selected");
                if (gate instanceof SwitchGate) {
                    ((SwitchGate) gate).setSelected(false);
                }
            }
        }

        for (TextLabel textLabel : canvas.getTextLabels()) {
            boolean intersects = selectionRect.getBoundsInParent()
                    .intersects(textLabel.localToScene(textLabel.getBoundsInLocal()));
            boolean isSelected = textLabel.getStyleClass().contains("selected");

            if (intersects && !isSelected) {
                if (!textLabel.getStyleClass().contains("selected")) {
                    textLabel.getStyleClass().add("selected");
                }

            } else if (!intersects && isSelected) {
                textLabel.getStyleClass().remove("selected");
            }
        }
    }

    public void deselectAllGates() {
        canvas.getGateImageViews().values().forEach(gate -> {
            gate.getImageView().getStyleClass().remove("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(false);
            }
        });
        canvas.getInteractionManager().setHighlightedGate(null);
    }

    public void deselectAllGatesExcept(ImageView exceptImageView) {
        canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey() != exceptImageView)
                .forEach(entry -> {
                    entry.getValue().getImageView().getStyleClass().remove("selected");
                    if (entry.getValue() instanceof SwitchGate) {
                        ((SwitchGate) entry.getValue()).setSelected(false);
                    }
                });
    }

    public void deselectAllLabels() {
        for (TextLabel label : canvas.getTextLabels()) {
            label.getStyleClass().remove("selected");
        }
    }

    public void deselectAllLabelsExcept(TextLabel textLabel) {
        for (TextLabel label : canvas.getTextLabels()) {
            if (!label.equals(textLabel)) {
                label.getStyleClass().remove("selected");
            }
        }
    }

    public void selectAllComponents() {
        for (Map.Entry<ImageView, LogicGate> entry : canvas.getGateImageViews().entrySet()) {
            ImageView gateView = entry.getKey();
            if (!gateView.getStyleClass().contains("selected")) {
                gateView.getStyleClass().add("selected");
            }
        }

        for (TextLabel textLabel : canvas.getTextLabels()) {
            if (!textLabel.getStyleClass().contains("selected")) {
                textLabel.getStyleClass().add("selected");
            }
        }
    }
}
