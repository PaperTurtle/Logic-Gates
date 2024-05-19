package com.paperturtle.managers;

import com.paperturtle.components.LogicGate;
import com.paperturtle.components.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class DragManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private final CircuitCanvas canvas;

    /**
     * Constructs a DragManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public DragManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Sets up drag handlers for an ImageView representing a logic gate.
     * 
     * @param imageView the ImageView
     * @param gate      the logic gate
     */
    public void setupDragHandlers(ImageView imageView, LogicGate gate) {
        imageView.setPickOnBounds(true);

        imageView.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && imageView.getStyleClass().contains("selected")) {
                handleMouseDragged(imageView, gate, event);
                event.consume();
            }
        });

        imageView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleMousePressed(imageView, gate, event);
            }
            handleMousePressedForGate(imageView, gate, event);
        });

        imageView.setOnMouseReleased(event -> {
            if (imageView.getStyleClass().contains("selected")) {
                event.consume();
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getTarget() instanceof Pane) {
                canvas.getGateManager().deselectAllGates();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof ImageView)) {
                clearSelection();
                canvas.requestFocus();
            }
        });
    }

    /**
     * Sets up drag handlers for a text label.
     * 
     * @param textLabel the text label
     */
    public void setupDragHandlersForLabel(TextLabel textLabel) {
        textLabel.setOnMousePressed(event -> {
            double offsetX = event.getSceneX() - textLabel.getLayoutX();
            double offsetY = event.getSceneY() - textLabel.getLayoutY();
            textLabel.setUserData(new double[] { offsetX, offsetY });

            if (!event.isControlDown() && !textLabel.getStyleClass().contains("selected")) {
                canvas.getSelectionManager().deselectAllLabelsExcept(textLabel);
            }
            if (!textLabel.getStyleClass().contains("selected")) {
                textLabel.getStyleClass().add("selected");
            }
        });

        textLabel.setOnMouseDragged(dragEvent -> {
            if (dragEvent.getButton() == MouseButton.PRIMARY && textLabel.getStyleClass().contains("selected")) {
                handleLabelDragged(textLabel, dragEvent);
                dragEvent.consume();
            }
        });

        textLabel.setOnMouseReleased(event -> {
            if (textLabel.getStyleClass().contains("selected")) {
                event.consume();
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getTarget() instanceof Pane) {
                canvas.getSelectionManager().deselectAllLabels();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof TextLabel)) {
                clearSelection();
                canvas.requestFocus();
            }
        });
    }

    /**
     * Handles mouse dragged events for an ImageView.
     * 
     * @param imageView the ImageView
     * @param gate      the logic gate
     * @param event     the mouse event
     */
    private void handleMouseDragged(ImageView imageView, LogicGate gate, MouseEvent event) {
        Object[] userData = (Object[]) imageView.getUserData();
        double baseX = (double) userData[0];
        double baseY = (double) userData[1];

        double deltaX = event.getSceneX() - baseX;
        double deltaY = event.getSceneY() - baseY;

        canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .forEach(entry -> relocateGate(entry.getValue(), deltaX, deltaY));

        canvas.getTextLabels().stream()
                .filter(label -> label.getStyleClass().contains("selected"))
                .forEach(label -> relocateLabel(label, deltaX, deltaY));

        imageView.setUserData(new Object[] { event.getSceneX(), event.getSceneY(), gate });
    }

    /**
     * Relocates a logic gate by a given delta x and delta y.
     * 
     * @param gate   the logic gate
     * @param deltaX the change in x
     * @param deltaY the change in y
     */
    private void relocateGate(LogicGate gate, double deltaX, double deltaY) {
        ImageView imageView = gate.getImageView();
        double newX = imageView.getX() + deltaX;
        double newY = imageView.getY() + deltaY;

        double clampedX = clamp(newX, 0, canvas.getWidth() - imageView.getBoundsInLocal().getWidth());
        double clampedY = clamp(newY, 0, canvas.getHeight() - imageView.getBoundsInLocal().getHeight());

        gate.setPosition(clampedX, clampedY);
        imageView.relocate(clampedX, clampedY);
    }

    /**
     * Relocates a text label by a given delta x and delta y.
     * 
     * @param label  the text label
     * @param deltaX the change in x
     * @param deltaY the change in y
     */
    private void relocateLabel(TextLabel label, double deltaX, double deltaY) {
        double newX = label.getLayoutX() + deltaX;
        double newY = label.getLayoutY() + deltaY;

        double clampedX = clamp(newX, 0, canvas.getWidth() - label.getWidth());
        double clampedY = clamp(newY, 0, canvas.getHeight() - label.getHeight());

        label.setLayoutX(clampedX);
        label.setLayoutY(clampedY);
    }

    /**
     * Handles mouse pressed events for an ImageView.
     * 
     * @param imageView the ImageView
     * @param gate      the logic gate
     * @param event     the mouse event
     */
    private void handleMousePressed(ImageView imageView, LogicGate gate, MouseEvent event) {
        double offsetX = event.getSceneX() - imageView.getLayoutX();
        double offsetY = event.getSceneY() - imageView.getLayoutY();
        if (!event.isControlDown() && !imageView.getStyleClass().contains("selected")) {
            canvas.getSelectionManager().deselectAllGatesExcept(imageView);
            canvas.getSelectionManager().deselectAllLabels();
        }
        imageView.getStyleClass().add("selected");
        imageView.setUserData(new Object[] { offsetX, offsetY, gate });
    }

    /**
     * Handles mouse pressed events for a logic gate.
     * 
     * @param imageView the ImageView of the gate
     * @param gate      the logic gate
     * @param event     the mouse event
     */
    private void handleMousePressedForGate(ImageView imageView, LogicGate gate, MouseEvent event) {
        LogicGate highlightedGate = canvas.getInteractionManager().getHighlightedGate();
        if (highlightedGate != null && highlightedGate != gate) {
            highlightedGate.getImageView().getStyleClass().remove("selected");
        }
        canvas.getInteractionManager().setHighlightedGate(gate);

        if (!imageView.getStyleClass().contains("selected")) {
            imageView.getStyleClass().add("selected");
        }

        double offsetX = event.getSceneX() - imageView.getLayoutX();
        double offsetY = event.getSceneY() - imageView.getLayoutY();
        imageView.setUserData(new Object[] { offsetX, offsetY, gate });

        if (event.getButton() == MouseButton.SECONDARY) {
            canvas.getContextMenuManager().showContextMenu(imageView, gate, event);
        }
    }

    /**
     * Handles mouse dragged events for a text label.
     * 
     * @param textLabel the text label
     * @param dragEvent the mouse event
     */
    private void handleLabelDragged(TextLabel textLabel, MouseEvent dragEvent) {
        double[] userData = (double[]) textLabel.getUserData();
        double offsetX = userData[0];
        double offsetY = userData[1];

        double newX = dragEvent.getSceneX() - offsetX;
        double newY = dragEvent.getSceneY() - offsetY;

        double deltaX = newX - textLabel.getLayoutX();
        double deltaY = newY - textLabel.getLayoutY();

        newX = clamp(newX, 0, canvas.getWidth() - textLabel.getWidth());
        newY = clamp(newY, 0, canvas.getHeight() - textLabel.getHeight());

        textLabel.setLayoutX(newX);
        textLabel.setLayoutY(newY);

        canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .forEach(entry -> relocateGate(entry.getValue(), deltaX, deltaY));

        canvas.getTextLabels().stream()
                .filter(label -> label != textLabel && label.getStyleClass().contains("selected"))
                .forEach(label -> relocateLabel(label, deltaX, deltaY));
    }

    /**
     * Clears the selection of a logic gate and hides the context menu if it is
     * open.
     */
    private void clearSelection() {
        LogicGate highlightedGate = canvas.getInteractionManager().getHighlightedGate();
        if (highlightedGate != null) {
            highlightedGate.getImageView().getStyleClass().remove("selected");
            canvas.getInteractionManager().setHighlightedGate(null);
        }
        if (canvas.getOpenContextMenu() != null) {
            canvas.getOpenContextMenu().hide();
            canvas.setOpenContextMenu(null);
        }
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
