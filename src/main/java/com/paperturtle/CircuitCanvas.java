package com.paperturtle;

import javafx.scene.layout.Pane;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.paperturtle.Action.ActionType;
import com.paperturtle.GateData.ConnectionData;

import javafx.application.Platform;

public class CircuitCanvas extends Pane {
    private Line currentLine;
    private LogicGate startGate;
    private Map<ImageView, List<Circle>> gateMarkers = new HashMap<>();
    private Map<ImageView, LogicGate> gateImageViews = new HashMap<>();
    private Point2D lastMouseCoordinates;
    private ScrollPane scrollPane;
    private Map<Line, LogicGate> lineToStartGateMap = new HashMap<>();
    private LogicGate highlightedGate = null;
    private ContextMenu openContextMenu = null;
    private Set<LogicGate> gatesToBeUpdated = new HashSet<>();
    private Rectangle selectionRect = new Rectangle();
    private boolean isSelecting = false;
    private boolean justSelected = false;
    private double lastX = 0;
    private double lastY = 0;
    private double viewOffsetX = 0;
    private double viewOffsetY = 0;
    private Point2D virtualOrigin = new Point2D(0, 0);
    private CursorMode currentCursorMode = CursorMode.POINTER;
    private List<ClipboardData> clipboard = new ArrayList<>();
    private Stack<Action> undoStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();
    private boolean isUndoOrRedo = false;
    private InteractionManager interactionManager;

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

        initializeSelectionMechanism();
        initializeZoomHandling();

        this.addEventFilter(MouseEvent.MOUSE_CLICKED, interactionManager::handleCanvasClick);
        this.setOnMousePressed(interactionManager::handleMousePressed);

        // this.setOnMouseDragged(this::handleMouseDragged);
        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.C && event.isControlDown()) {
                copySelectedGatesToClipboard();
                event.consume();
            } else if (event.getCode() == KeyCode.V && event.isControlDown()) {
                pasteGatesFromClipboard();
                event.consume();
            } else if (event.getCode() == KeyCode.X && event.isControlDown()) {
                removeSelectedGates();
                event.consume();
            } else if (event.getCode() == KeyCode.Z && event.isControlDown()) {
                undo();
                event.consume();
            } else if (event.getCode() == KeyCode.Y && event.isControlDown()) {
                redo();
                event.consume();
            }
        });
    }

    public void logAction(Action action, boolean fromUserAction) {
        if (fromUserAction) {
            undoStack.push(action);
            redoStack.clear();
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Action lastAction = undoStack.pop();
            isUndoOrRedo = true;
            boolean actionApplied = applyAction(lastAction, true);
            if (actionApplied) {
                redoStack.push(lastAction);
            }
            isUndoOrRedo = false;
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Action nextAction = redoStack.pop();
            isUndoOrRedo = true;
            boolean actionApplied = applyAction(nextAction, false);
            if (actionApplied) {
                undoStack.push(nextAction);
            }
            isUndoOrRedo = false;
        }
    }

    private boolean applyAction(Action action, boolean isUndo) {
        boolean stateChanged = false;
        switch (action.type) {
            case ADD:
                if (isUndo) {
                    removeComponents(action.affectedComponents);
                    stateChanged = true;
                } else {
                    addComponents(action.affectedComponents);
                    stateChanged = true;
                }
                break;
            case MOVE:
                moveComponents(action.affectedComponents, isUndo ? action.oldPositions : action.newPositions);
                stateChanged = true;
                break;
            case REMOVE:
                if (isUndo) {
                    addComponents(action.affectedComponents);
                    stateChanged = true;
                } else {
                    removeComponents(action.affectedComponents);
                    stateChanged = true;
                }
                break;
        }
        return stateChanged;
    }

    private void addComponents(List<CircuitComponent> components) {
        for (CircuitComponent component : components) {
            if (component instanceof LogicGate) {
                LogicGate gate = (LogicGate) component;

                drawGate(gate, gate.getPosition().getX(), gate.getPosition().getY());
            } else if (component instanceof TextLabel) {
                TextLabel label = (TextLabel) component;
                drawTextLabel(label, label.getLayoutX(), label.getLayoutY());
            }
        }
    }

    // Method to remove components from the canvas
    private void removeComponents(List<CircuitComponent> components) {
        for (CircuitComponent component : components) {
            if (component instanceof LogicGate) {
                LogicGate gate = (LogicGate) component;
                removeGate(gate.getImageView());
            } else if (component instanceof TextLabel) {
                TextLabel label = (TextLabel) component;
                this.getChildren().remove(label);
            }
        }
    }

    private void moveComponents(List<CircuitComponent> components, List<Point2D> newPositions) {
        List<Point2D> oldPositions = new ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            CircuitComponent component = components.get(i);
            Point2D newPosition = newPositions.get(i);
            if (component instanceof LogicGate) {
                LogicGate gate = (LogicGate) component;
                oldPositions.add(gate.getPosition());
                gate.setPosition(newPosition.getX(), newPosition.getY());
                gate.getImageView().relocate(newPosition.getX(), newPosition.getY());
            } else if (component instanceof TextLabel) {
                TextLabel label = (TextLabel) component;
                oldPositions.add(new Point2D(label.getLayoutX(), label.getLayoutY()));
                label.setLayoutX(newPosition.getX());
                label.setLayoutY(newPosition.getY());
            }
        }
        Action moveAction = new Action(ActionType.MOVE, components, oldPositions, newPositions);
        logAction(moveAction, !isUndoOrRedo);
    }

    private void pasteGatesFromClipboard() {
        deselectAllGates();
        Map<String, LogicGate> createdGates = new HashMap<>();
        double offsetX = 30;
        double offsetY = 30;

        for (ClipboardData data : clipboard) {
            LogicGate gate = GateFactory.createGate(normalizeType(data.getType()));
            if (gate == null) {
                System.out.println("Unable to create gate of type: " + data.getType());
                continue;
            }

            double newX = data.getPosition().getX() + offsetX;
            double newY = data.getPosition().getY() + offsetY;

            gate.setPosition(newX, newY);
            gate.setId(data.getId());
            createdGates.put(data.getId(), gate);
            drawGate(gate, newX, newY);
            gate.getImageView().getStyleClass().add("selected");
        }

        for (ClipboardData data : clipboard) {
            LogicGate sourceGate = createdGates.get(data.getId());
            if (sourceGate == null)
                continue;

            for (ClipboardData.ConnectionData output : data.getOutputs()) {
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

                Line connectionLine = new Line(sourcePos.getX(), sourcePos.getY(), targetPos.getX(), targetPos.getY());
                connectionLine.setStrokeWidth(3.5);
                connectionLine.setStroke(Color.BLACK);

                this.getChildren().add(connectionLine);
                sourceGate.addOutputConnection(connectionLine);
                targetGate.addInputConnection(connectionLine, output.pointIndex);
            }
        }
    }

    private void copySelectedGatesToClipboard() {
        clipboard.clear();
        gateImageViews.entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .forEach(entry -> {
                    LogicGate gate = entry.getValue();
                    clipboard.add(gate.getGateClipboardData());
                });
    }

    private void handleMouseDragged(MouseEvent event) {
        if (currentCursorMode == CursorMode.GRABBY) {
            double dampingFactor = 0.2;
            double deltaX = (event.getX() - lastX) * dampingFactor;
            double deltaY = (event.getY() - lastY) * dampingFactor;

            if (deltaX != 0 || deltaY != 0) {
                virtualOrigin = virtualOrigin.subtract(deltaX, deltaY);
                for (Node child : getChildren()) {
                    child.setTranslateX(child.getTranslateX() + deltaX);
                    child.setTranslateY(child.getTranslateY() + deltaY);
                }

                updateScrollPaneViewport(-deltaX, -deltaY);
            }

        } else if (currentCursorMode == CursorMode.POINTER && isSelecting) {
            double transformedX = scrollPane.getHvalue() * (scrollPane.getContent().getBoundsInLocal().getWidth()
                    - scrollPane.getViewportBounds().getWidth());
            double transformedY = scrollPane.getVvalue() * (scrollPane.getContent().getBoundsInLocal().getHeight()
                    - scrollPane.getViewportBounds().getHeight());

            double x = event.getX() + transformedX - virtualOrigin.getX();
            double y = event.getY() + transformedY - virtualOrigin.getY();

            selectionRect.setWidth(Math.abs(x - lastMouseCoordinates.getX()));
            selectionRect.setHeight(Math.abs(y - lastMouseCoordinates.getY()));
            selectionRect.setX(Math.min(x, lastMouseCoordinates.getX()));
            selectionRect.setY(Math.min(y, lastMouseCoordinates.getY()));
        }

        lastX = event.getX() + viewOffsetX;
        lastY = event.getY() + viewOffsetY;
    }

    private void updateScrollPaneViewport(double deltaX, double deltaY) {
        double hValue = scrollPane.getHvalue() + (deltaX / scrollPane.getContent().getBoundsInLocal().getWidth());
        double vValue = scrollPane.getVvalue() + (deltaY / scrollPane.getContent().getBoundsInLocal().getHeight());

        scrollPane.setHvalue(Math.max(0, Math.min(hValue, 1)));
        scrollPane.setVvalue(Math.max(0, Math.min(vValue, 1)));
    }

    private void initializeZoomHandling() {
        this.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double scaleFactor = 1.1;
                double deltaY = event.getDeltaY();
                if (deltaY > 0) {
                    this.setScaleX(this.getScaleX() * scaleFactor);
                    this.setScaleY(this.getScaleY() * scaleFactor);
                } else if (deltaY < 0) {
                    this.setScaleX(this.getScaleX() / scaleFactor);
                    this.setScaleY(this.getScaleY() / scaleFactor);
                }
                event.consume();
            }
        });
    }

    private void deselectAllGates() {
        gateImageViews.values().forEach(gate -> {
            gate.getImageView().getStyleClass().remove("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(false);
            }
        });
        highlightedGate = null;
    }

    private void handleCanvasClick(MouseEvent event) {
        if (!(event.getTarget() instanceof ImageView)) {
            if (justSelected) {
                justSelected = false;
                return;
            }
            deselectAllGates();
            this.requestFocus();
            return;
        } else if (event.getTarget() instanceof ImageView) {
            ImageView clickedImageView = (ImageView) event.getTarget();
            if (highlightedGate != null && !highlightedGate.getImageView().equals(clickedImageView)) {
                deselectAllGates();
                LogicGate clickedGate = gateImageViews.get(clickedImageView);
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

    private void initializeSelectionMechanism() {
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.BLUE.deriveColor(0, 1.2, 1, 0.2));
        selectionRect.setVisible(false);
        this.getChildren().add(selectionRect);
        final double dragThreshold = 10.0;

        this.setOnMousePressed(event -> {
            lastMouseCoordinates = new Point2D(Math.max(0, Math.min(event.getX(),
                    getWidth())),
                    Math.max(0, Math.min(event.getY(), getHeight())));
            selectionRect.setX(lastMouseCoordinates.getX());
            selectionRect.setY(lastMouseCoordinates.getY());
            selectionRect.setWidth(0);
            selectionRect.setHeight(0);
            selectionRect.setVisible(true);
            isSelecting = true;
        });

        this.setOnMouseDragged(event -> {
            if (isSelecting) {
                double x = Math.max(0, Math.min(event.getX(), getWidth()));
                double y = Math.max(0, Math.min(event.getY(), getHeight()));
                selectionRect.setWidth(Math.abs(x - lastMouseCoordinates.getX()));
                selectionRect.setHeight(Math.abs(y - lastMouseCoordinates.getY()));
                selectionRect.setX(Math.min(x, lastMouseCoordinates.getX()));
                selectionRect.setY(Math.min(y, lastMouseCoordinates.getY()));
            }
        });

        this.setOnMouseReleased(event -> {
            if (currentCursorMode == CursorMode.POINTER && isSelecting
                    && (selectionRect.getWidth() > dragThreshold || selectionRect.getHeight() > dragThreshold)) {
                interactionManager.selectGatesInRectangle();
            }
            selectionRect.setVisible(false);
            isSelecting = false;
            justSelected = true;
        });
    }

    private void selectGatesInRectangle() {
        for (Map.Entry<ImageView, LogicGate> entry : gateImageViews.entrySet()) {
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
    }

    private void removeSelectedGates() {
        List<ImageView> selectedGates = gateImageViews.entrySet().stream()
                .filter(entry -> entry.getKey().getStyleClass().contains("selected"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        selectedGates.forEach(this::removeGate);
    }

    public void drawGate(LogicGate gate, double x, double y) {
        gate.createVisualRepresentation(this);
        gate.setPosition(x, y);
        interactionManager.setupDragHandlers(gate.imageView, gate);
        gateImageViews.put(gate.getImageView(), gate);
        if (gate instanceof SwitchGate) {
            ((SwitchGate) gate).updateOutputConnectionsColor();
        }
        Action addAction = new Action(ActionType.ADD, Collections.singletonList((CircuitComponent) gate));
        logAction(addAction, !isUndoOrRedo);
    }

    public void drawTextLabel(TextLabel textLabel, double x, double y) {
        this.getChildren().add(textLabel);
        textLabel.setLayoutX(x);
        textLabel.setLayoutY(y);
        interactionManager.setupDragHandlersForLabel(textLabel);
    }

    public void setupOutputInteraction(Circle outputMarker, LogicGate gate) {
        outputMarker.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && currentLine == null) {
                Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(), outputMarker.getCenterY());
                currentLine = new Line(outputPos.getX(), outputPos.getY(), event.getX(), event.getY());
                Color lineColor = gate.evaluate() ? Color.RED : Color.BLACK;
                currentLine.setStroke(lineColor);
                currentLine.setStrokeWidth(3.5);
                this.getChildren().add(currentLine);
                lineToStartGateMap.put(currentLine, gate);
                startGate = gate;
                gate.addOutputConnection(currentLine);
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
                    if (targetGate != null && sourceGate != null && targetGate != sourceGate) {
                        int inputIndex = findInputMarkerIndex(targetGate, inputMarker);
                        targetGate.addInputConnection(currentLine, inputIndex);
                        targetGate.addInput(sourceGate);
                        sourceGate.addOutputConnection(currentLine);
                        sourceGate.addOutputGate(targetGate);
                        targetGate.evaluate();
                        targetGate.propagateStateChange();
                        sourceGate.updateOutputConnectionsColor(sourceGate.evaluate());
                        scheduleUpdate(targetGate);
                        startGate = null;
                    } else {
                        if (currentLine != null && startGate != null) {
                            this.getChildren().remove(currentLine);
                            startGate.removeOutputConnection(currentLine);
                            currentLine = null;
                        }
                        return false;
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

    public LogicGate findGateForInputMarker(Circle inputMarker) {
        for (Map.Entry<ImageView, LogicGate> entry : gateImageViews.entrySet()) {
            LogicGate gate = entry.getValue();
            if (gate.getInputMarkers().contains(inputMarker)) {
                return gate;
            }
        }
        return null;
    }

    public int findInputMarkerIndex(LogicGate gate, Circle inputMarker) {
        return gate.getInputMarkers().indexOf(inputMarker);
    }

    private void resetInteractionHandlers() {
        this.setOnMouseMoved(null);
        this.setOnMouseClicked(null);
    }

    public void removeGate(ImageView gate) {
        LogicGate logicGate = gateImageViews.get(gate);
        if (logicGate != null) {
            removeAllConnections(logicGate);
            this.getChildren().removeAll(logicGate.getInputMarkers());
            if (logicGate.getOutputMarker() != null) {
                this.getChildren().remove(logicGate.getOutputMarker());
            }
            this.getChildren().remove(gate);
            gateImageViews.remove(gate);
            gateMarkers.remove(gate);
            logicGate.inputs.forEach(inputGate -> {
                inputGate.getOutputGates().remove(logicGate);
                inputGate.evaluate();
                inputGate.propagateStateChange();
            });

            logicGate.outputGates.forEach(outputGate -> {
                outputGate.inputs.remove(logicGate);
                outputGate.evaluate();
                outputGate.propagateStateChange();
            });
            logicGate.getInputMarkers().clear();

            Action removeAction = new Action(ActionType.REMOVE,
                    Collections.singletonList((CircuitComponent) logicGate));
            logAction(removeAction, !isUndoOrRedo);
        }
        propagateUpdates();
    }

    private void removeAllConnections(LogicGate logicGate) {
        new ArrayList<>(logicGate.getOutputConnections()).forEach(line -> {
            LogicGate targetGate = lineToStartGateMap.get(line);
            if (targetGate != null) {
                targetGate.removeInput(logicGate);
                targetGate.evaluate();
                targetGate.propagateStateChange();
            }
            lineToStartGateMap.remove(line);
            this.getChildren().remove(line);
        });

        logicGate.getInputConnections().forEach(connections -> new ArrayList<>(connections).forEach(line -> {
            LogicGate sourceGate = lineToStartGateMap.get(line);
            if (sourceGate != null) {
                sourceGate.getOutputConnections().remove(line);
                sourceGate.evaluate();
                sourceGate.propagateStateChange();
            }
            lineToStartGateMap.remove(line);
            this.getChildren().remove(line);
        }));
    }

    public void removeConnection(Line connection) {
        LogicGate sourceGate = lineToStartGateMap.get(connection);
        if (sourceGate != null) {
            sourceGate.removeOutputConnection(connection);

            LogicGate targetGate = findTargetGate(connection);
            if (targetGate != null) {
                int index = targetGate.findInputConnectionIndex(connection);
                if (index != -1) {
                    targetGate.removeInputConnection(connection, index);
                    targetGate.removeInput(sourceGate);
                }

                targetGate.evaluate();
                targetGate.propagateStateChange();
                scheduleUpdate(targetGate);
            }

            this.getChildren().remove(connection);
            lineToStartGateMap.remove(connection);

            sourceGate.evaluate();
            sourceGate.propagateStateChange();
            scheduleUpdate(sourceGate);
        } else {
            System.out.println("No source gate found for the connection.");
        }
    }

    private LogicGate findTargetGate(Line connection) {
        for (LogicGate gate : gateImageViews.values()) {
            if (gate.getInputConnections().contains(connection)) {
                return gate;
            }
        }
        return null;
    }

    private void setupConnectionHandlers() {
        this.setOnMouseMoved(mouseMoveEvent -> {
            if (currentLine != null) {
                currentLine.setEndX(mouseMoveEvent.getX());
                currentLine.setEndY(mouseMoveEvent.getY());
            }
        });

        this.setOnMouseClicked(mouseClickEvent -> {
            if (currentLine != null && mouseClickEvent.getClickCount() == 1) {
                if (!finalizeConnection(mouseClickEvent.getX(), mouseClickEvent.getY(), null)) {
                    this.getChildren().remove(currentLine);
                }
                currentLine = null;
                resetInteractionHandlers();
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY && event.getTarget() instanceof Line) {
                Line targetLine = (Line) event.getTarget();
                if (openContextMenu != null) {
                    openContextMenu.hide();
                }
                ContextMenu lineContextMenu = new ContextMenu();
                MenuItem deleteLine = new MenuItem("Remove");
                deleteLine.setOnAction(e -> {
                    removeConnection(targetLine);
                    e.consume();
                });
                lineContextMenu.getItems().add(deleteLine);
                lineContextMenu.show(targetLine, event.getScreenX(), event.getScreenY());
                openContextMenu = lineContextMenu;
            }
        });

    }

    public void propagateUpdates() {
        while (!gatesToBeUpdated.isEmpty()) {
            Set<LogicGate> currentBatch = new HashSet<>(gatesToBeUpdated);
            gatesToBeUpdated.clear();
            currentBatch.forEach(LogicGate::propagateStateChange);
        }
    }

    public void scheduleUpdate(LogicGate gate) {
        gatesToBeUpdated.add(gate);
        Platform.runLater(this::propagateUpdates);
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

    private String normalizeType(String type) {
        if (type.endsWith("Gate")) {
            type = type.substring(0, type.length() - 4);
        }
        return type.toUpperCase();
    }

    public boolean isEmpty() {
        return gateImageViews.isEmpty();
    }

    public void clearCanvas() {
        for (LogicGate gate : new ArrayList<>(gateImageViews.values())) {
            removeGate(gate.getImageView());
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

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public void setLastX(double x) {
        this.lastX = x;
    }

    public void setLastY(double y) {
        this.lastY = y;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public Point2D getVirtualOrigin() {
        return virtualOrigin;
    }

    public boolean isSelecting() {
        return isSelecting;
    }

    public void setSelecting(boolean selecting) {
        isSelecting = selecting;
    }

    public Map<ImageView, LogicGate> getGateImageViews() {
        return gateImageViews;
    }

    public LogicGate getHighlightedGate() {
        return highlightedGate;
    }

    public void setHighlightedGate(LogicGate gate) {
        this.highlightedGate = gate;
    }

    public CursorMode getCurrentCursorMode() {
        return currentCursorMode;
    }

    public Rectangle getSelectionRect() {
        return selectionRect;
    }

    public ContextMenu getOpenContextMenu() {
        return openContextMenu;
    }

    public void setOpenContextMenu(ContextMenu openContextMenu) {
        this.openContextMenu = openContextMenu;
    }

    public boolean isJustSelected() {
        return justSelected;
    }

    public void setJustSelected(boolean justSelected) {
        this.justSelected = justSelected;
    }

    public Line getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(Line currentLine) {
        this.currentLine = currentLine;
    }

    public LogicGate getStartGate() {
        return startGate;
    }

    public void setStartGate(LogicGate startGate) {
        this.startGate = startGate;
    }

    public Map<Line, LogicGate> getLineToStartGateMap() {
        return lineToStartGateMap;
    }

    public Set<LogicGate> getGatesToBeUpdated() {
        return gatesToBeUpdated;
    }

}
