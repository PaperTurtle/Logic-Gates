package com.paperturtle;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.paperturtle.GateData.ConnectionData;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
    private Rectangle selectionRect = new Rectangle();
    private boolean isSelecting = false;

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
        initializeSelectionMechanism();

        this.setOnMouseClicked(event -> {
            if (event.getTarget() instanceof Pane || !(event.getTarget() instanceof ImageView)) {
                if (highlightedGate != null) {
                    highlightedGate.unhighlight();
                    highlightedGate = null;
                }
            }
            this.requestFocus();
        });
    }

    private void initializeSelectionMechanism() {
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.BLUE.deriveColor(0, 1.2, 1, 0.2));
        selectionRect.setVisible(false);
        this.getChildren().add(selectionRect);

        this.setOnMousePressed(event -> {
            if (currentMode == Mode.WORK) {
                lastMouseCoordinates = new Point2D(Math.max(0, Math.min(event.getX(), getWidth())),
                        Math.max(0, Math.min(event.getY(), getHeight())));
                selectionRect.setX(lastMouseCoordinates.getX());
                selectionRect.setY(lastMouseCoordinates.getY());
                selectionRect.setWidth(0);
                selectionRect.setHeight(0);
                selectionRect.setVisible(true);
                isSelecting = true;
            }
        });

        this.setOnMouseDragged(event -> {
            if (isSelecting && currentMode == Mode.WORK) {
                double x = Math.max(0, Math.min(event.getX(), getWidth()));
                double y = Math.max(0, Math.min(event.getY(), getHeight()));
                selectionRect.setWidth(Math.abs(x - lastMouseCoordinates.getX()));
                selectionRect.setHeight(Math.abs(y - lastMouseCoordinates.getY()));
                selectionRect.setX(Math.min(x, lastMouseCoordinates.getX()));
                selectionRect.setY(Math.min(y, lastMouseCoordinates.getY()));
            }
        });

        this.setOnMouseReleased(event -> {
            if (isSelecting && currentMode == Mode.WORK) {
                selectGatesInRectangle();
                selectionRect.setVisible(false);
                isSelecting = false;
            }
        });
    }

    private void selectGatesInRectangle() {
        for (Map.Entry<ImageView, LogicGate> entry : gateImageViews.entrySet()) {
            ImageView gateView = entry.getKey();
            if (gateView.getBoundsInParent().intersects(selectionRect.getBoundsInParent())) {
                if (!gateView.getStyleClass().contains("selected")) {
                    gateView.getStyleClass().add("selected");
                    currentMode = Mode.PAN;
                    this.setCursor(Cursor.OPEN_HAND);
                    updateMarkersVisibility();
                }
            } else {
                gateView.getStyleClass().remove("selected");
            }
        }
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
                default:
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
                    if (targetGate != null && sourceGate != null) {
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
        if (openContextMenu != null) {
            openContextMenu.hide();
            openContextMenu = null;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gate Properties");
        alert.setHeaderText("Properties for " + gate.getClass().getSimpleName());

        TableView<Boolean[]> table = new TableView<>();
        List<Pair<Boolean[], Boolean>> pairList = gate.getTruthTableData();
        List<Boolean[]> dataList = new ArrayList<>();
        for (Pair<Boolean[], Boolean> pair : pairList) {
            Boolean[] row = new Boolean[pair.getKey().length + 1];
            System.arraycopy(pair.getKey(), 0, row, 0, pair.getKey().length);
            row[pair.getKey().length] = pair.getValue();
            dataList.add(row);
        }

        ObservableList<Boolean[]> data = FXCollections.observableArrayList(dataList);
        table.setItems(data);

        int numInputs = pairList.isEmpty() || pairList.get(0).getKey().length == 0 ? 0
                : pairList.get(0).getKey().length;
        for (int i = 0; i < numInputs; i++) {
            TableColumn<Boolean[], String> inputCol = new TableColumn<>("Input " + (i + 1));
            final int index = i;
            inputCol.setCellValueFactory(param -> new SimpleStringProperty(
                    param.getValue().length > index ? (param.getValue()[index] ? "1" : "0") : "N/A"));
            table.getColumns().add(inputCol);
            inputCol.setPrefWidth(USE_COMPUTED_SIZE);
        }

        TableColumn<Boolean[], String> outputCol = new TableColumn<>("Output");
        outputCol.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue()[param.getValue().length - 1] ? "1" : "0"));
        table.getColumns().add(outputCol);
        outputCol.setPrefWidth(USE_COMPUTED_SIZE);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMinWidth(Region.USE_PREF_SIZE);
        table.setMinHeight(Region.USE_PREF_SIZE);

        double rowHeight = 30.0;
        double headerHeight = 27.0;

        table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(rowHeight).add(headerHeight));
        table.maxHeightProperty().bind(table.prefHeightProperty());

        table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            final TableHeaderRow header = (TableHeaderRow) table.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((o, oldVal, newVal) -> header.setReordering(false));
        });

        alert.getDialogPane().setContent(table);
        alert.getDialogPane().getStylesheets()
                .add(getClass().getResource("/com/paperturtle/styles.css").toExternalForm());
        table.getStylesheets().add(getClass().getResource("/com/paperturtle/styles.css").toExternalForm());
        alert.showAndWait();
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

    public void loadGates(List<GateData> gatesData) {
        clearCanvas();
        Map<String, LogicGate> createdGates = new HashMap<>();
        for (GateData data : gatesData) {
            String normalizedType = normalizeType(data.type);
            LogicGate gate = GateFactory.createGate(normalizedType);
            if (gate == null) {
                System.out.println("Failed to create gate for type: " + data.type);
                continue;
            }
            gate.setPosition(data.position.getX(), data.position.getY());
            gate.setId(data.id);
            createdGates.put(data.id, gate);
            drawGate(gate, data.position.getX(), data.position.getY());
        }

        for (GateData data : gatesData) {
            LogicGate sourceGate = createdGates.get(data.id);
            if (sourceGate == null)
                continue;

            for (ConnectionData output : data.outputs) {
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
                connectionLine.setStrokeWidth(2);
                connectionLine.setStroke(Color.BLACK);

                this.getChildren().add(connectionLine);
                sourceGate.addOutputConnection(connectionLine);
                targetGate.addInputConnection(connectionLine, output.pointIndex);
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

}