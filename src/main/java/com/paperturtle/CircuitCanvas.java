package com.paperturtle;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
import java.util.stream.Collectors;

import com.paperturtle.GateData.ConnectionData;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
    private Point2D virtualOrigin = new Point2D(0, 0);
    private CursorMode currentCursorMode = CursorMode.POINTER;
    private List<ClipboardData> clipboard = new ArrayList<>();

    public enum CursorMode {
        POINTER, GRABBY
    }

    public CircuitCanvas(double width, double height, ScrollPane scrollPane) {
        super();
        this.scrollPane = scrollPane;
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");
        this.setFocusTraversable(true);
        initializeSelectionMechanism();
        initializeZoomHandling();
        this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleCanvasClick);
        this.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseDragged(this::handleMouseDragged);
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
            }
        });
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

    private void handleMousePressed(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        if (currentCursorMode == CursorMode.GRABBY) {
            isSelecting = false;
        } else if (currentCursorMode == CursorMode.POINTER) {
            isSelecting = true;
            lastMouseCoordinates = new Point2D(Math.max(0, Math.min(event.getX(), getWidth())),
                    Math.max(0, Math.min(event.getY(), getHeight())));
            selectionRect.setX(lastMouseCoordinates.getX());
            selectionRect.setY(lastMouseCoordinates.getY());
            selectionRect.setWidth(0);
            selectionRect.setHeight(0);
            selectionRect.setVisible(true);
        }
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
            double x = Math.max(0, Math.min(event.getX(), getWidth()));
            double y = Math.max(0, Math.min(event.getY(), getHeight()));
            selectionRect.setWidth(Math.abs(x - lastMouseCoordinates.getX()));
            selectionRect.setHeight(Math.abs(y - lastMouseCoordinates.getY()));
            selectionRect.setX(Math.min(x, lastMouseCoordinates.getX()));
            selectionRect.setY(Math.min(y, lastMouseCoordinates.getY()));
        }

        lastX = event.getX();
        lastY = event.getY();
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
                selectGatesInRectangle();
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
        setupDragHandlers(gate.imageView, gate);
        gateImageViews.put(gate.getImageView(), gate);
        if (gate instanceof SwitchGate) {
            ((SwitchGate) gate).updateOutputConnectionsColor();
        }
    }

    public void drawTextLabel(TextLabel textLabel, double x, double y) {
        this.getChildren().add(textLabel);
        textLabel.setLayoutX(x);
        textLabel.setLayoutY(y);
        setupDragHandlersForLabel(textLabel);
    }

    private void setupDragHandlersForLabel(TextLabel textLabel) {
        textLabel.setOnMousePressed(event -> {
            double offsetX = event.getSceneX() - textLabel.getLayoutX();
            double offsetY = event.getSceneY() - textLabel.getLayoutY();
            textLabel.setUserData(new Object[] { offsetX, offsetY });

            textLabel.setOnMouseDragged(dragEvent -> {
                double newX = dragEvent.getSceneX() - offsetX;
                double newY = dragEvent.getSceneY() - offsetY;

                newX = Math.max(0, Math.min(newX, getWidth() - textLabel.getWidth()));
                newY = Math.max(0, Math.min(newY, getHeight() - textLabel.getHeight()));

                textLabel.setLayoutX(newX);
                textLabel.setLayoutY(newY);
            });
            event.consume();
        });
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
            if (event.getButton() == MouseButton.PRIMARY && imageView.getStyleClass().contains("selected")) {
                Object[] userData = (Object[]) imageView.getUserData();
                double baseX = (double) userData[0];
                double baseY = (double) userData[1];

                double deltaX = event.getSceneX() - baseX;
                double deltaY = event.getSceneY() - baseY;

                for (Map.Entry<ImageView, LogicGate> entry : gateImageViews.entrySet()) {
                    ImageView otherImageView = entry.getKey();
                    if (otherImageView.getStyleClass().contains("selected")) {
                        LogicGate otherGate = entry.getValue();

                        double newX = otherGate.getImageView().getX() + deltaX;
                        double newY = otherGate.getImageView().getY() + deltaY;

                        double clampedX = Math.max(0,
                                Math.min(newX, getWidth() - otherImageView.getBoundsInLocal().getWidth()));
                        double clampedY = Math.max(0,
                                Math.min(newY, getHeight() - otherImageView.getBoundsInLocal().getHeight()));

                        otherGate.setPosition(clampedX, clampedY);
                        otherImageView.relocate(clampedX, clampedY);
                    }
                }

                imageView.setUserData(new Object[] { event.getSceneX(), event.getSceneY(), gate });
                event.consume();
            }
        });

        imageView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double offsetX = event.getSceneX() - imageView.getLayoutX();
                double offsetY = event.getSceneY() - imageView.getLayoutY();
                if (!event.isControlDown() && !imageView.getStyleClass().contains("selected")) {
                    deselectAllGatesExcept(imageView);
                }
                imageView.getStyleClass().add("selected");
                imageView.setUserData(new Object[] { offsetX, offsetY, gate });
            }
            handleMousePressedForGate(imageView, gate, event);
        });

        imageView.setOnMouseReleased(event -> {
            if (imageView.getStyleClass().contains("selected")) {
                event.consume();
            }
        });

        this.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getTarget() instanceof Pane) {
                deselectAllGates();
            }
        });

        this.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof ImageView)) {
                if (highlightedGate != null) {
                    highlightedGate.getImageView().getStyleClass().remove("selected");
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

    private void deselectAllGatesExcept(ImageView exceptImageView) {
        gateImageViews.entrySet().stream()
                .filter(entry -> entry.getKey() != exceptImageView)
                .forEach(entry -> {
                    entry.getKey().getStyleClass().remove("selected");
                    if (entry.getValue() instanceof SwitchGate) {
                        ((SwitchGate) entry.getValue()).setSelected(false);
                    }
                });
    }

    private void handleMousePressedForGate(ImageView imageView, LogicGate gate, MouseEvent event) {
        if (highlightedGate != null && highlightedGate != gate) {
            highlightedGate.getImageView().getStyleClass().remove("selected");
        }
        highlightedGate = gate;

        if (!imageView.getStyleClass().contains("selected")) {
            imageView.getStyleClass().add("selected");
        }

        double offsetX = event.getSceneX() - imageView.getLayoutX();
        double offsetY = event.getSceneY() - imageView.getLayoutY();
        imageView.setUserData(new Object[] { offsetX, offsetY, gate });

        if (event.getButton() == MouseButton.SECONDARY) {
            showContextMenu(imageView, gate, event);
        }
    }

    private void showContextMenu(ImageView imageView, LogicGate gate, MouseEvent event) {
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

        if (gate instanceof ClockGate) {
            MenuItem editItem = new MenuItem("Edit time");
            editItem.setOnAction(e -> ((ClockGate) gate).showTimeEditDialog());
            contextMenu.getItems().addAll(deleteItem, propertiesItem, editItem);
        } else {
            contextMenu.getItems().addAll(deleteItem, propertiesItem);
        }

        contextMenu.show(imageView, event.getScreenX(), event.getScreenY());
        openContextMenu = contextMenu;
        event.consume();
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
            TableColumn<Boolean[], String> inputCol = new TableColumn<>("Input " + (i +
                    1));
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

    public void setCurrentCursorMode(CursorMode mode) {
        this.currentCursorMode = mode;
    }

}
