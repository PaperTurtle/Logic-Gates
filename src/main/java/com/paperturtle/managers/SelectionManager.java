package com.paperturtle.managers;

import java.util.Map;

import com.paperturtle.components.LogicGate;
import com.paperturtle.components.SwitchGate;
import com.paperturtle.components.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The SelectionManager class is responsible for managing the selection of
 * components in the circuit canvas.
 * 
 * @see CircuitCanvas
 * @see LogicGate
 * 
 * @author Seweryn Czabanowski
 */
public class SelectionManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The rectangle used to visualize the selection area.
     */
    private Rectangle selectionRect = new Rectangle();

    /**
     * A flag indicating whether a selection operation is currently in progress.
     */
    private boolean isSelecting = false;

    /**
     * The threshold for the drag operation.
     */
    private static final double DRAG_THRESHOLD = 10.0;

    /**
     * Constructs a SelectionManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public SelectionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
        initializeSelectionMechanism();
    }

    /**
     * Initializes the selection mechanism for the circuit canvas.
     */
    private void initializeSelectionMechanism() {
        configureSelectionRect();
        canvas.getChildren().add(selectionRect);

        canvas.setOnMousePressed(event -> startSelection(event.getX(), event.getY()));
        canvas.setOnMouseDragged(event -> updateSelection(event.getX(), event.getY()));
        canvas.setOnMouseReleased(event -> finalizeSelection());
    }

    /**
     * Configures the selection rectangle.
     */
    private void configureSelectionRect() {
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.BLUE.deriveColor(0, 1.2, 1, 0.2));
        selectionRect.setVisible(false);
        selectionRect.getStyleClass().add("selection-rectangle");
    }

    /**
     * Finalizes the selection operation.
     */
    private void finalizeSelection() {
        if (isSelecting && (selectionRect.getWidth() > DRAG_THRESHOLD || selectionRect.getHeight() > DRAG_THRESHOLD)) {
            selectComponentsInRectangle();
        }
        selectionRect.setVisible(false);
        canvas.getInteractionManager().setJustSelected(false);
        isSelecting = false;
    }

    /**
     * Starts the selection operation.
     * 
     * @param x the x-coordinate of the starting point
     * @param y the y-coordinate of the starting point
     */
    private void startSelection(double x, double y) {
        double startX = clamp(x, 0, canvas.getWidth());
        double startY = clamp(y, 0, canvas.getHeight());
        canvas.setLastMouseCoordinates(new Point2D(startX, startY));
        selectionRect.setX(startX);
        selectionRect.setY(startY);
        selectionRect.setWidth(0);
        selectionRect.setHeight(0);
        selectionRect.setVisible(true);
        isSelecting = true;
    }

    /**
     * Updates the selection rectangle.
     * 
     * @param x the x-coordinate of the current point
     * @param y the y-coordinate of the current point
     */
    private void updateSelection(double x, double y) {
        if (isSelecting) {
            double endX = clamp(x, 0, canvas.getWidth());
            double endY = clamp(y, 0, canvas.getHeight());
            double minX = Math.min(canvas.getLastMouseCoordinates().getX(), endX);
            double maxX = Math.max(canvas.getLastMouseCoordinates().getX(), endX);
            double minY = Math.min(canvas.getLastMouseCoordinates().getY(), endY);
            double maxY = Math.max(canvas.getLastMouseCoordinates().getY(), endY);

            selectionRect.setX(minX);
            selectionRect.setY(minY);
            selectionRect.setWidth(maxX - minX);
            selectionRect.setHeight(maxY - minY);
        }
    }

    /**
     * Selects components within the selection rectangle.
     */
    private void selectComponentsInRectangle() {
        selectGates();
        selectLabels();
    }

    /**
     * Selects gates within the selection rectangle.
     */
    private void selectGates() {
        canvas.getGateImageViews().forEach((imageView, gate) -> {
            boolean intersects = imageView.getBoundsInParent().intersects(selectionRect.getBoundsInParent());
            updateSelectionState(imageView, gate, intersects);
        });
    }

    /**
     * Selects labels within the selection rectangle.
     */
    private void selectLabels() {
        canvas.getTextLabels().forEach(label -> {
            boolean intersects = selectionRect.getBoundsInParent()
                    .intersects(label.localToScene(label.getBoundsInLocal()));
            updateSelectionState(label, intersects);
        });
    }

    /**
     * Updates the selection state of the specified ImageView.
     * 
     * @param imageView  the ImageView to update
     * @param gate       the LogicGate associated with the ImageView
     * @param intersects a flag indicating whether the ImageView intersects the
     *                   selection rectangle
     */
    private void updateSelectionState(ImageView imageView, LogicGate gate, boolean intersects) {
        if (intersects) {
            addSelection(imageView, gate);
        } else {
            removeSelection(imageView, gate);
        }
    }

    /**
     * Updates the selection state of the specified TextLabel.
     * 
     * @param label      the TextLabel to update
     * @param intersects a flag indicating whether the TextLabel intersects the
     *                   selection rectangle
     */
    private void updateSelectionState(TextLabel label, boolean intersects) {
        if (intersects) {
            addSelection(label);
        } else {
            removeSelection(label);
        }
    }

    /**
     * Adds a selection to the specified ImageView.
     * 
     * @param imageView the ImageView to add the selection to
     * @param gate      the LogicGate associated with the ImageView
     */
    private void addSelection(ImageView imageView, LogicGate gate) {
        if (!imageView.getStyleClass().contains("selected")) {
            imageView.getStyleClass().add("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(true);
            }
        }
    }

    /**
     * Removes a selection from the specified ImageView.
     * 
     * @param imageView the ImageView to remove the selection from
     * @param gate      the LogicGate associated with the ImageView
     */
    private void removeSelection(ImageView imageView, LogicGate gate) {
        if (imageView.getStyleClass().contains("selected")) {
            imageView.getStyleClass().remove("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(false);
            }
        }
    }

    /**
     * Adds a selection to the specified TextLabel.
     * 
     * @param label the TextLabel to add the selection to
     */
    private void addSelection(TextLabel label) {
        if (!label.getStyleClass().contains("selected")) {
            label.getStyleClass().add("selected");
        }
    }

    /**
     * Removes a selection from the specified TextLabel.
     * 
     * @param label the TextLabel to remove the selection from
     */
    private void removeSelection(TextLabel label) {
        if (label.getStyleClass().contains("selected")) {
            label.getStyleClass().remove("selected");
        }
    }

    /**
     * Selects gates within the selection rectangle.
     */
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

    /**
     * Deselects all gates in the circuit canvas.
     */
    public void deselectAllGates() {
        canvas.getGateImageViews().values().forEach(gate -> {
            gate.getImageView().getStyleClass().remove("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(false);
            }
        });
        canvas.getInteractionManager().setHighlightedGate(null);
    }

    /**
     * Deselects all gates except the specified ImageView.
     * 
     * @param exceptImageView the ImageView to keep selected
     */
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

    /**
     * Deselects all labels in the circuit canvas.
     */
    public void deselectAllLabels() {
        canvas.getTextLabels().forEach(label -> label.getStyleClass().remove("selected"));
    }

    /**
     * Deselects all labels except the specified TextLabel.
     * 
     * @param textLabel the TextLabel to keep selected
     */
    public void deselectAllLabelsExcept(TextLabel textLabel) {
        canvas.getTextLabels().stream()
                .filter(label -> !label.equals(textLabel))
                .forEach(label -> label.getStyleClass().remove("selected"));
    }

    /**
     * Selects all components in the circuit canvas.
     */
    public void selectAllComponents() {
        canvas.getGateImageViews().keySet().forEach(imageView -> {
            if (!imageView.getStyleClass().contains("selected")) {
                imageView.getStyleClass().add("selected");
            }
        });

        canvas.getTextLabels().forEach(label -> {
            if (!label.getStyleClass().contains("selected")) {
                label.getStyleClass().add("selected");
            }
        });
    }

    /**
     * Clamps a value between a minimum and maximum.
     * 
     * @param value The value to clamp
     * @param min   The minimum value
     * @param max   The maximum value
     * @return The clamped value
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
