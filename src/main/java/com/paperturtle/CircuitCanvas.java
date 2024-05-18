package com.paperturtle;

import javafx.scene.layout.Pane;

import java.util.List;

import javafx.application.Platform;
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
    private CommandManager commandManager;
    private InteractionManager interactionManager;
    private ConnectionManager connectionManager;
    private GateManager gateManager;
    private ClipboardManager clipboardManager;

    public CircuitCanvas(double width, double height, ScrollPane scrollPane) {
        super();
        this.scrollPane = scrollPane;
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");
        this.setFocusTraversable(true);

        this.commandManager = new CommandManager();
        this.interactionManager = new InteractionManager(this);
        this.connectionManager = new ConnectionManager(this);
        this.gateManager = new GateManager(this);
        this.clipboardManager = new ClipboardManager(this);

        interactionManager.initializeSelectionMechanism();

        this.addEventFilter(MouseEvent.MOUSE_CLICKED, interactionManager::handleCanvasClick);

        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.C && event.isControlDown()) {
                clipboardManager.copySelectedGatesToClipboard();
                event.consume();
            } else if (event.getCode() == KeyCode.V && event.isControlDown()) {
                commandManager.executeCommand(new PasteGatesCommand(this, clipboardManager.getClipboard(), 30, 30));
                event.consume();
            } else if (event.getCode() == KeyCode.X && event.isControlDown()) {
                commandManager.executeCommand(new RemoveSelectedGatesCommand(this));
                event.consume();
            } else if (event.getCode() == KeyCode.A && event.isControlDown()) {
                interactionManager.selectAllComponents();
                event.consume();
            } else if (event.getCode() == KeyCode.Z && event.isControlDown()) {
                commandManager.undo();
                event.consume();
            } else if (event.getCode() == KeyCode.Y && event.isControlDown()) {
                commandManager.redo();
                event.consume();
            }
        });

    }

    public void drawGate(LogicGate gate, double x, double y) {
        gate.createVisualRepresentation(this);
        gate.setPosition(x, y);
        interactionManager.setupDragHandlers(gate.imageView, gate);
        gateImageViews.put(gate.getImageView(), gate);
        if (gate instanceof SwitchGate) {
            ((SwitchGate) gate).updateOutputConnectionsColor();
        }
    }

    public void drawTextLabel(TextLabel textLabel, double x, double y) {
        this.getChildren().add(textLabel);
        textLabel.setLayoutX(x);
        textLabel.setLayoutY(y);
        textLabels.add(textLabel);
        interactionManager.setupDragHandlersForLabel(textLabel);
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

    public CommandManager getCommandManager() {
        return commandManager;
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

    public ClipboardManager getClipboardManager() {
        return clipboardManager;
    }

    public List<LogicGate> getSelectedGates() {
        return gateImageViews.entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
