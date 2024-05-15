package com.paperturtle;

import javafx.scene.layout.Pane;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.paperturtle.GateData.ConnectionData;

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
    private CursorMode currentCursorMode = CursorMode.POINTER;
    private InteractionManager interactionManager;
    private ConnectionManager connectionManager;
    private GateManager gateManager;
    private ClipboardManager clipboardManager;

    public enum CursorMode {
        POINTER, GRABBY
    }

    public CircuitCanvas(double width, double height, ScrollPane scrollPane) {
        super();
        this.scrollPane = scrollPane;
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");
        this.setFocusTraversable(true);

        this.interactionManager = new InteractionManager(this);
        this.connectionManager = new ConnectionManager(this);
        this.gateManager = new GateManager(this);
        this.clipboardManager = new ClipboardManager(this);

        interactionManager.initializeSelectionMechanism();
        interactionManager.setupPanning();

        this.addEventFilter(MouseEvent.MOUSE_CLICKED, interactionManager::handleCanvasClick);

        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.C && event.isControlDown()) {
                clipboardManager.copySelectedGatesToClipboard();
                event.consume();
            } else if (event.getCode() == KeyCode.V && event.isControlDown()) {
                clipboardManager.pasteGatesFromClipboard();
                event.consume();
            } else if (event.getCode() == KeyCode.X && event.isControlDown()) {
                gateManager.removeSelectedGates();
                event.consume();
            } else if (event.getCode() == KeyCode.A && event.isControlDown()) {
                interactionManager.selectAllComponents();
                event.consume();
            }
        });

        this.getChildren().addListener((ListChangeListener<Node>) change -> updateCanvasSize());
    }

    public void updateCanvasSize() {
        double maxWidth = 0;
        double maxHeight = 0;

        for (Node node : this.getChildren()) {
            if (node.getBoundsInParent().getMaxX() > maxWidth) {
                maxWidth = node.getBoundsInParent().getMaxX();
            }
            if (node.getBoundsInParent().getMaxY() > maxHeight) {
                maxHeight = node.getBoundsInParent().getMaxY();
            }
        }

        this.setPrefSize(maxWidth + 50, maxHeight + 50);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        updateCanvasSize();
    }

    public void drawGate(LogicGate gate, double x, double y) {
        gate.createVisualRepresentation(this);
        gate.setPosition(x, y);
        interactionManager.setupDragHandlers(gate.imageView, gate);
        gateImageViews.put(gate.getImageView(), gate);
        if (gate instanceof SwitchGate) {
            ((SwitchGate) gate).updateOutputConnectionsColor();
        }
        updateCanvasSize();
    }

    public void drawTextLabel(TextLabel textLabel, double x, double y) {
        this.getChildren().add(textLabel);
        textLabel.setLayoutX(x);
        textLabel.setLayoutY(y);
        textLabels.add(textLabel);
        interactionManager.setupDragHandlersForLabel(textLabel);
        updateCanvasSize();
    }

    public void scheduleUpdate(LogicGate gate) {
        gatesToBeUpdated.add(gate);
        Platform.runLater(this::propagateUpdates);
    }

    public void propagateUpdates() {
        while (!gatesToBeUpdated.isEmpty()) {
            Set<LogicGate> currentBatch = new HashSet<>(gatesToBeUpdated);
            gatesToBeUpdated.clear();
            currentBatch.forEach(LogicGate::propagateStateChange);
        }
    }

    public List<GateData> getAllGateData() {
        List<GateData> allData = new ArrayList<>();
        for (LogicGate gate : gateImageViews.values()) {
            allData.add(gate.getGateData());
        }

        return allData;
    }

    public List<TextLabel> getAllTextLabels() {
        List<TextLabel> allLabels = new ArrayList<>();
        for (Node node : this.getChildren()) {
            if (node instanceof TextLabel) {
                allLabels.add((TextLabel) node);
            }
        }
        return allLabels;
    }

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
                if (sourceGate == null)
                    continue;

                for (ConnectionData output : gateData.outputs) {
                    LogicGate targetGate = createdGates.get(output.gateId);
                    if (targetGate == null) {
                        System.out.println("Output gate not found for ID: " + output.gateId);
                        continue;
                    }
                    Point2D sourcePos = sourceGate.getOutputMarker().localToParent(
                            sourceGate.getOutputMarker().getCenterX(), sourceGate.getOutputMarker().getCenterY());
                    Point2D targetPos = targetGate.getInputMarkers().get(output.pointIndex).localToParent(
                            targetGate.getInputMarkers().get(output.pointIndex).getCenterX(),
                            targetGate.getInputMarkers().get(output.pointIndex).getCenterY());

                    Line connectionLine = new Line(sourcePos.getX(), sourcePos.getY(), targetPos.getX(),
                            targetPos.getY());
                    connectionLine.setStrokeWidth(2);
                    connectionLine.setStroke(Color.BLACK);

                    this.getChildren().add(connectionLine);
                    sourceGate.addOutputConnection(connectionLine);
                    targetGate.addInputConnection(connectionLine, output.pointIndex);
                }
            } else if (component instanceof TextLabel) {
                TextLabel textLabel = (TextLabel) component;
                drawTextLabel(textLabel, textLabel.getLayoutX(), textLabel.getLayoutY());
            }
        }
    }

    public String normalizeType(String type) {
        if (type.endsWith("Gate")) {
            type = type.substring(0, type.length() - 4);
        }
        return type.toUpperCase();
    }

    public boolean isEmpty() {
        return gateImageViews.isEmpty();
    }

    public void removeTextLabel(TextLabel textLabel) {
        this.getChildren().remove(textLabel);
        textLabels.remove(textLabel);
        updateCanvasSize();
    }

    public void clearCanvas() {
        for (LogicGate gate : new ArrayList<>(gateImageViews.values())) {
            gateManager.removeGate(gate.getImageView());
        }
        this.getChildren().clear();
        gateImageViews.clear();
        gateMarkers.clear();
        lineToStartGateMap.clear();
    }

    public void setCurrentCursorMode(CursorMode mode) {
        this.currentCursorMode = mode;
    }

    public Point2D getLastMouseCoordinates() {
        return lastMouseCoordinates;
    }

    public void setLastMouseCoordinates(Point2D coordinates) {
        this.lastMouseCoordinates = coordinates;
    }

    public void addGateImageView(ImageView imageView, LogicGate gate) {
        gateImageViews.put(imageView, gate);
    }

    public LogicGate getGate(ImageView imageView) {
        return gateImageViews.get(imageView);
    }

    public void removeGateImageView(ImageView imageView) {
        gateImageViews.remove(imageView);
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public Map<ImageView, LogicGate> getGateImageViews() {
        return gateImageViews;
    }

    public List<TextLabel> getTextLabels() {
        return textLabels;
    }

    public CursorMode getCurrentCursorMode() {
        return currentCursorMode;
    }

    public ContextMenu getOpenContextMenu() {
        return openContextMenu;
    }

    public void setOpenContextMenu(ContextMenu openContextMenu) {
        this.openContextMenu = openContextMenu;
    }

    public Line getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(Line currentLine) {
        this.currentLine = currentLine;
    }

    public Map<ImageView, List<Circle>> getGateMarkers() {
        return gateMarkers;
    }

    public Map<Line, LogicGate> getLineToStartGateMap() {
        return lineToStartGateMap;
    }

    public Set<LogicGate> getGatesToBeUpdated() {
        return gatesToBeUpdated;
    }

    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public GateManager getGateManager() {
        return gateManager;
    }

    public List<LogicGate> getSelectedGates() {
        return gateImageViews.entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
