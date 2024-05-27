package com.paperturtle.managers;

import com.paperturtle.components.LogicGate;
import com.paperturtle.components.inputs.SwitchGate;
import com.paperturtle.components.utilities.TextLabel;
import com.paperturtle.gui.CircuitCanvas;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * The InteractionManager class is responsible for handling user interactions
 * with the circuit canvas, including dragging, clicking, and creating
 * connections.
 * 
 * @see CircuitCanvas
 * 
 * @author Seweryn Czabanowski
 */
public class InteractionManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * A flag indicating whether a logic gate was just selected.
     */
    private boolean justSelected = false;

    /**
     * The logic gate that is currently highlighted.
     */
    private LogicGate highlightedGate = null;

    /**
     * The drag manager for the circuit canvas.
     */
    private DragManager dragManager;

    /**
     * Constructs an InteractionManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public InteractionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
        this.dragManager = new DragManager(canvas);
    }

    /**
     * Sets up drag handlers for a logic gate.
     * 
     * @param imageView the ImageView of the gate
     * @param gate      the logic gate
     */
    public void setupDragHandlers(ImageView imageView, LogicGate gate) {
        dragManager.setupDragHandlers(imageView, gate);
    }

    /**
     * Sets up drag handlers for a text label.
     * 
     * @param textLabel the text label
     */
    public void setupDragHandlersForLabel(TextLabel textLabel) {
        dragManager.setupDragHandlersForLabel(textLabel);
    }

    /**
     * Handles canvas click events.
     * 
     * @param event the mouse event
     */
    public void handleCanvasClick(MouseEvent event) {
        if (!(event.getTarget() instanceof ImageView)) {
            if (justSelected) {
                justSelected = false;
                return;
            }
            canvas.requestFocus();
        } else {
            ImageView clickedImageView = (ImageView) event.getTarget();
            LogicGate clickedGate = canvas.getGateImageViews().get(clickedImageView);
            if (highlightedGate != null && highlightedGate != clickedGate) {
                canvas.getGateManager().deselectAllGates();
                if (clickedGate != null) {
                    highlightedGate = clickedGate;
                    if (!clickedImageView.getStyleClass().contains("selected")) {
                        clickedImageView.getStyleClass().add("selected");
                    }
                    if (clickedGate instanceof SwitchGate) {
                        ((SwitchGate) clickedGate).setSelected(true);
                    }
                }
            }
        }
    }

    /**
     * Sets up connection handlers for creating connections between logic gates.
     */
    public void setupConnectionHandlers() {
        canvas.setOnMouseMoved(mouseMoveEvent -> {
            if (canvas.getCurrentLine() != null) {
                canvas.getCurrentLine().setEndX(mouseMoveEvent.getX());
                canvas.getCurrentLine().setEndY(mouseMoveEvent.getY());
            }
        });

        canvas.setOnMouseClicked(mouseClickEvent -> {
            if (canvas.getCurrentLine() != null && mouseClickEvent.getClickCount() == 1) {
                if (!canvas.getConnectionManager().finalizeConnection(mouseClickEvent.getX(), mouseClickEvent.getY(),
                        null)) {
                    canvas.getChildren().remove(canvas.getCurrentLine());
                }
                canvas.setCurrentLine(null);
                resetInteractionHandlers();
            }
        });

        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY && event.getTarget() instanceof Line) {
                Line targetLine = (Line) event.getTarget();
                if (canvas.getOpenContextMenu() != null) {
                    canvas.getOpenContextMenu().hide();
                }
                ContextMenu lineContextMenu = new ContextMenu();
                MenuItem deleteLine = new MenuItem("Remove");
                deleteLine.setOnAction(e -> {
                    canvas.getConnectionManager().removeConnection(targetLine);
                    e.consume();
                });
                lineContextMenu.getItems().add(deleteLine);
                lineContextMenu.show(targetLine, event.getScreenX(), event.getScreenY());
                canvas.setOpenContextMenu(lineContextMenu);
            }
        });
    }

    /**
     * Resets the interaction handlers.
     */
    private void resetInteractionHandlers() {
        canvas.setOnMouseMoved(null);
        canvas.setOnMouseClicked(null);
    }

    /**
     * Sets up output interaction for a logic gate.
     * 
     * @param outputMarker the output marker of the gate
     * @param gate         the logic gate
     */
    public void setupOutputInteraction(Circle outputMarker, LogicGate gate) {
        outputMarker.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && canvas.getCurrentLine() == null) {
                if (gate.getOutputConnections().size() < gate.getMaxOutputConnections()) {
                    Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(),
                            outputMarker.getCenterY());
                    canvas.setCurrentLine(new Line(outputPos.getX(), outputPos.getY(), event.getX(), event.getY()));
                    Color lineColor = gate.evaluate() ? Color.RED : Color.BLACK;
                    canvas.getCurrentLine().setStroke(lineColor);
                    canvas.getCurrentLine().setStrokeWidth(3.5);
                    canvas.getChildren().add(canvas.getCurrentLine());
                    canvas.getLineToStartGateMap().put(canvas.getCurrentLine(), gate);
                    canvas.getConnectionManager().setStartGate(gate);
                    setupConnectionHandlers();
                    event.consume();
                }
            }
        });
    }

    /**
     * Get the highlighted gate.
     * 
     * @return the highlighted gate
     */
    public LogicGate getHighlightedGate() {
        return highlightedGate;
    }

    /**
     * Sets the highlighted gate.
     * 
     * @param highlightedGate the highlighted gate to set
     */
    public void setHighlightedGate(LogicGate highlightedGate) {
        this.highlightedGate = highlightedGate;
    }

    /**
     * Checks if the gate was just selected.
     * 
     * @return true if the gate was just selected, false otherwise
     */
    public boolean isJustSelected() {
        return justSelected;
    }

    /**
     * Sets the justSelected flag.
     * 
     * @param justSelected the flag to set
     */
    public void setJustSelected(boolean justSelected) {
        this.justSelected = justSelected;
    }
}
