package com.paperturtle;

import javafx.scene.layout.Pane;

import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.paperturtle.components.GateFactory;
import com.paperturtle.components.LogicGate;
import com.paperturtle.components.SwitchGate;
import com.paperturtle.components.TextLabel;
import com.paperturtle.data.GateData;
import com.paperturtle.data.GateData.ConnectionData;
import com.paperturtle.managers.*;
import com.paperturtle.utils.CircuitComponent;

/**
 * The CircuitCanvas class represents the canvas on which logic gates and
 * connections are drawn and managed.
 * It extends the JavaFX Pane class and provides methods to draw, load, and
 * manage gates and connections.
 * 
 * @see LogicGate
 * @see ClipboardManager
 * @see CommandManager
 * @see ConnectionManager
 * @see ContextMenuManager
 * @see GateManager
 * @see InteractionManager
 * @see KeyboardShortcutManager
 * @see SelectionManager
 * 
 * @author Seweryn Czabanowski
 */
public class CircuitCanvas extends Pane {
    private Line currentLine;
    private Map<ImageView, List<Circle>> gateMarkers = new HashMap<>();
    private Map<ImageView, LogicGate> gateImageViews = new HashMap<>();
    private List<TextLabel> textLabels = new ArrayList<>();
    private Point2D lastMouseCoordinates;
    private ScrollPane scrollPane;
    private Map<Line, LogicGate> lineToStartGateMap = new HashMap<>();
    private ContextMenu openContextMenu = null;
    private Set<LogicGate> gatesToBeUpdated = new HashSet<>();
    private CommandManager commandManager;
    private InteractionManager interactionManager;
    private SelectionManager selectionManager;
    private ConnectionManager connectionManager;
    private GateManager gateManager;
    private ClipboardManager clipboardManager;
    private ContextMenuManager contextMenuManager;

    /**
     * Constructs a CircuitCanvas with the specified width, height, and scroll pane.
     * 
     * @param width      the width of the canvas
     * @param height     the height of the canvas
     * @param scrollPane the scroll pane that contains the canvas
     */
    public CircuitCanvas(double width, double height, ScrollPane scrollPane) {
        super();
        this.scrollPane = scrollPane;
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");
        this.setFocusTraversable(true);

        this.commandManager = new CommandManager();
        this.interactionManager = new InteractionManager(this);
        this.selectionManager = new SelectionManager(this);
        this.connectionManager = new ConnectionManager(this);
        this.gateManager = new GateManager(this);
        this.clipboardManager = new ClipboardManager(this);
        this.contextMenuManager = new ContextMenuManager(this);

        selectionManager.initializeSelectionMechanism();

        this.addEventFilter(MouseEvent.MOUSE_CLICKED, interactionManager::handleCanvasClick);
        new KeyboardShortcutManager(this);
    }

    /**
     * Draws a logic gate at the specified coordinates on the canvas.
     * 
     * @param gate the logic gate to draw
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     */
    public void drawGate(LogicGate gate, double x, double y) {
        gate.createVisualRepresentation(this);
        gate.setPosition(x, y);
        interactionManager.setupDragHandlers(gate.getImageView(), gate);
        gateImageViews.put(gate.getImageView(), gate);
        if (gate instanceof SwitchGate) {
            ((SwitchGate) gate).updateOutputConnectionsColor();
        }
    }

    /**
     * Draws a text label at the specified coordinates on the canvas.
     * 
     * @param textLabel the text label to draw
     * @param x         the x-coordinate
     * @param y         the y-coordinate
     */
    public void drawTextLabel(TextLabel textLabel, double x, double y) {
        this.getChildren().add(textLabel);
        textLabel.setLayoutX(x);
        textLabel.setLayoutY(y);
        textLabels.add(textLabel);
        interactionManager.setupDragHandlersForLabel(textLabel);
    }

    /**
     * Schedules an update for the specified logic gate.
     * 
     * @param gate the logic gate to update
     */
    public void scheduleUpdate(LogicGate gate) {
        gatesToBeUpdated.add(gate);
        Platform.runLater(this::propagateUpdates);
    }

    /**
     * Propagates updates for all gates that need to be updated.
     */
    public void propagateUpdates() {
        while (!gatesToBeUpdated.isEmpty()) {
            Set<LogicGate> currentBatch = new HashSet<>(gatesToBeUpdated);
            gatesToBeUpdated.clear();
            currentBatch.forEach(LogicGate::propagateStateChange);
        }
    }

    /**
     * Gets the data for all gates on the canvas.
     * 
     * @return a list of GateData objects representing all gates
     */
    public List<GateData> getAllGateData() {
        List<GateData> allData = new ArrayList<>();
        for (LogicGate gate : gateImageViews.values()) {
            allData.add(gate.getGateData());
        }

        return allData;
    }

    /**
     * Gets all text labels on the canvas.
     * 
     * @return a list of TextLabel objects representing all text labels
     */
    public List<TextLabel> getAllTextLabels() {
        List<TextLabel> allLabels = new ArrayList<>();
        for (Node node : this.getChildren()) {
            if (node instanceof TextLabel) {
                allLabels.add((TextLabel) node);
            }
        }
        return allLabels;
    }

    /**
     * Loads components onto the canvas from a list of CircuitComponent objects.
     * 
     * @param components the list of components to load
     */
    public void loadComponents(List<CircuitComponent> components) {
        clearCanvas();
        Map<String, LogicGate> createdGates = new HashMap<>();

        for (CircuitComponent component : components) {
            if (component instanceof GateData) {
                GateData gateData = (GateData) component;
                String normalizedType = normalizeType(gateData.type);
                LogicGate gate = GateFactory.createGate(normalizedType);
                if (gate == null) {
                    System.out.println("Failed to create gate for type: " + gateData.type);
                    continue;
                }
                gate.setPosition(gateData.position.getX(), gateData.position.getY());
                gate.setId(gateData.id);
                createdGates.put(gateData.id, gate);
                drawGate(gate, gateData.position.getX(), gateData.position.getY());
            }
        }

        for (CircuitComponent component : components) {
            if (component instanceof GateData) {
                GateData gateData = (GateData) component;

                LogicGate sourceGate = createdGates.get(gateData.id);
                if (sourceGate == null) {
                    System.out.println("Source gate not found for ID: " + gateData.id);
                    continue;
                }

                for (ConnectionData output : gateData.outputs) {
                    LogicGate targetGate = createdGates.get(output.gateId);
                    if (targetGate == null) {
                        System.out.println("Output gate not found for ID: " + output.gateId);
                        continue;
                    }

                    if (output.pointIndex < 0 || output.pointIndex >= targetGate.getInputMarkers().size()) {
                        System.out.println("Invalid point index: " + output.pointIndex + " for target gate: "
                                + targetGate.getId());
                        continue;
                    }

                    Point2D sourcePos = sourceGate.getOutputMarker().localToParent(
                            sourceGate.getOutputMarker().getCenterX(), sourceGate.getOutputMarker().getCenterY());
                    Point2D targetPos = targetGate.getInputMarkers().get(output.pointIndex).localToParent(
                            targetGate.getInputMarkers().get(output.pointIndex).getCenterX(),
                            targetGate.getInputMarkers().get(output.pointIndex).getCenterY());

                    Line connectionLine = new Line(sourcePos.getX(), sourcePos.getY(), targetPos.getX(),
                            targetPos.getY());
                    connectionLine.setStrokeWidth(3.5);
                    connectionLine.setStroke(Color.BLACK);

                    this.getChildren().add(connectionLine);
                    sourceGate.addOutputConnection(connectionLine);
                    targetGate.addInputConnection(connectionLine, output.pointIndex);

                    sourceGate.addOutputGate(targetGate);
                    targetGate.addInput(sourceGate);
                }
            } else if (component instanceof TextLabel) {
                TextLabel textLabel = (TextLabel) component;
                drawTextLabel(textLabel, textLabel.getLayoutX(), textLabel.getLayoutY());
            }
        }
    }

    /**
     * Normalizes the type of a gate by removing the "Gate" suffix and converting to
     * uppercase.
     * 
     * @param type the type to normalize
     * @return the normalized type
     */
    public String normalizeType(String type) {
        if (type.endsWith("Gate")) {
            type = type.substring(0, type.length() - 4);
        }
        return type.toUpperCase();
    }

    /**
     * Checks if the canvas is empty.
     * 
     * @return true if the canvas is empty, false otherwise
     */
    public boolean isEmpty() {
        return gateImageViews.isEmpty();
    }

    /**
     * Removes a text label from the canvas.
     * 
     * @param textLabel the text label to remove
     */
    public void removeTextLabel(TextLabel textLabel) {
        this.getChildren().remove(textLabel);
        textLabels.remove(textLabel);
    }

    /**
     * Clears all components from the canvas.
     */
    public void clearCanvas() {
        Node selectionRect = null;
        for (Node node : this.getChildren()) {
            if (node instanceof Rectangle && node.getStyleClass().contains("selection-rectangle")) {
                selectionRect = node;
                break;
            }
        }

        this.getChildren().clear();
        if (selectionRect != null) {
            this.getChildren().add(selectionRect);
        }

        gateImageViews.clear();
        gateMarkers.clear();
        lineToStartGateMap.clear();
    }

    /**
     * Gets the last mouse coordinates recorded on the canvas.
     * 
     * @return the last mouse coordinates
     */
    public Point2D getLastMouseCoordinates() {
        return lastMouseCoordinates;
    }

    /**
     * Sets the last mouse coordinates recorded on the canvas.
     * 
     * @param coordinates the coordinates to set
     */
    public void setLastMouseCoordinates(Point2D coordinates) {
        this.lastMouseCoordinates = coordinates;
    }

    /**
     * Adds a gate ImageView and its corresponding logic gate to the canvas.
     * 
     * @param imageView the ImageView of the gate
     * @param gate      the logic gate
     */
    public void addGateImageView(ImageView imageView, LogicGate gate) {
        gateImageViews.put(imageView, gate);
    }

    /**
     * Gets the logic gate corresponding to the specified ImageView.
     * 
     * @param imageView the ImageView of the gate
     * @return the logic gate
     */
    public LogicGate getGate(ImageView imageView) {
        return gateImageViews.get(imageView);
    }

    /**
     * Removes the specified gate ImageView from the canvas.
     * 
     * @param imageView the ImageView to remove
     */
    public void removeGateImageView(ImageView imageView) {
        gateImageViews.remove(imageView);
    }

    /**
     * Gets the map of gate ImageViews to logic gates.
     * 
     * @return the map of gate ImageViews to logic gates
     */
    public Map<ImageView, LogicGate> getGateImageViews() {
        return gateImageViews;
    }

    /**
     * Gets the list of text labels on the canvas.
     * 
     * @return the list of text labels
     */
    public List<TextLabel> getTextLabels() {
        return textLabels;
    }

    /**
     * Gets the currently open context menu on the canvas.
     * 
     * @return the currently open context menu
     */
    public ContextMenu getOpenContextMenu() {
        return openContextMenu;
    }

    /**
     * Sets the currently open context menu on the canvas.
     * 
     * @param openContextMenu the context menu to set
     */
    public void setOpenContextMenu(ContextMenu openContextMenu) {
        this.openContextMenu = openContextMenu;
    }

    /**
     * Gets the current line being drawn on the canvas.
     * 
     * @return the current line
     */
    public Line getCurrentLine() {
        return currentLine;
    }

    /**
     * Sets the current line being drawn on the canvas.
     * 
     * @param currentLine the current line to set
     */
    public void setCurrentLine(Line currentLine) {
        this.currentLine = currentLine;
    }

    /**
     * Gets the map of gate markers.
     * 
     * @return the map of gate markers
     */
    public Map<ImageView, List<Circle>> getGateMarkers() {
        return gateMarkers;
    }

    /**
     * Gets the map of lines to their starting gates.
     * 
     * @return the map of lines to their starting gates
     */
    public Map<Line, LogicGate> getLineToStartGateMap() {
        return lineToStartGateMap;
    }

    /**
     * Gets the set of gates to be updated.
     * 
     * @return the set of gates to be updated
     */
    public Set<LogicGate> getGatesToBeUpdated() {
        return gatesToBeUpdated;
    }

    /**
     * Gets the command manager.
     * 
     * @return the command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Gets the interaction manager.
     * 
     * @return the interaction manager
     */
    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    /**
     * Gets the connection manager.
     * 
     * @return the connection manager
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Gets the gate manager.
     * 
     * @return the gate manager
     */
    public GateManager getGateManager() {
        return gateManager;
    }

    /**
     * Gets the clipboard manager.
     * 
     * @return the clipboard manager
     */
    public ClipboardManager getClipboardManager() {
        return clipboardManager;
    }

    /**
     * Gets the selection manager.
     * 
     * @return the selection manager
     */
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    /**
     * Gets the context menu manager.
     * 
     * @return the context menu manager
     */
    public ContextMenuManager getContextMenuManager() {
        return contextMenuManager;
    }

    /**
     * Gets the list of selected gates on the canvas.
     * 
     * @return the list of selected gates
     */
    public List<LogicGate> getSelectedGates() {
        return gateImageViews.entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
