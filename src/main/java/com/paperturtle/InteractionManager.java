package com.paperturtle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paperturtle.CircuitCanvas.CursorMode;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;

public class InteractionManager {
    private CircuitCanvas canvas;
    private Rectangle selectionRect = new Rectangle();
    private boolean isSelecting = false;
    private boolean justSelected = false;
    private LogicGate highlightedGate = null;

    public InteractionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
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

    public void setupDragHandlers(ImageView imageView, LogicGate gate) {
        imageView.setPickOnBounds(true);

        imageView.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && imageView.getStyleClass().contains("selected")) {
                Object[] userData = (Object[]) imageView.getUserData();
                double baseX = (double) userData[0];
                double baseY = (double) userData[1];

                double deltaX = event.getSceneX() - baseX;
                double deltaY = event.getSceneY() - baseY;

                for (Map.Entry<ImageView, LogicGate> entry : canvas.getGateImageViews().entrySet()) {
                    ImageView otherImageView = entry.getKey();
                    if (otherImageView.getStyleClass().contains("selected")) {
                        LogicGate otherGate = entry.getValue();

                        double newX = otherGate.getImageView().getX() + deltaX;
                        double newY = otherGate.getImageView().getY() + deltaY;

                        double clampedX = Math.max(0,
                                Math.min(newX, canvas.getWidth() - otherImageView.getBoundsInLocal().getWidth()));
                        double clampedY = Math.max(0,
                                Math.min(newY, canvas.getHeight() - otherImageView.getBoundsInLocal().getHeight()));

                        otherGate.setPosition(clampedX, clampedY);
                        otherImageView.relocate(clampedX, clampedY);
                    }
                }

                for (TextLabel textLabel : canvas.getTextLabels()) {
                    if (textLabel.getStyleClass().contains("selected")) {
                        double newX = textLabel.getLayoutX() + deltaX;
                        double newY = textLabel.getLayoutY() + deltaY;

                        double clampedX = Math.max(0, Math.min(newX, canvas.getWidth() - textLabel.getWidth()));
                        double clampedY = Math.max(0, Math.min(newY, canvas.getHeight() - textLabel.getHeight()));

                        textLabel.setLayoutX(clampedX);
                        textLabel.setLayoutY(clampedY);
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
                    deselectAllLabels();
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

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getTarget() instanceof Pane) {
                canvas.getGateManager().deselectAllGates();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof ImageView)) {
                if (highlightedGate != null) {
                    highlightedGate.getImageView().getStyleClass().remove("selected");
                    highlightedGate = null;
                }
                if (canvas.getOpenContextMenu() != null) {
                    canvas.getOpenContextMenu().hide();
                    canvas.setOpenContextMenu(null);
                }
                canvas.requestFocus();
            }
        });
    }

    public void setupDragHandlersForLabel(TextLabel textLabel) {
        textLabel.setOnMousePressed(event -> {
            double offsetX = event.getSceneX() - textLabel.getLayoutX();
            double offsetY = event.getSceneY() - textLabel.getLayoutY();
            textLabel.setUserData(new double[] { offsetX, offsetY });

            if (!event.isControlDown() && !textLabel.getStyleClass().contains("selected")) {
                deselectAllLabelsExcept(textLabel);
            }
            if (!textLabel.getStyleClass().contains("selected")) {
                textLabel.getStyleClass().add("selected");
            }

        });

        textLabel.setOnMouseDragged(dragEvent -> {
            if (dragEvent.getButton() == MouseButton.PRIMARY && textLabel.getStyleClass().contains("selected")) {
                double[] userData = (double[]) textLabel.getUserData();
                double offsetX = userData[0];
                double offsetY = userData[1];

                double newX = dragEvent.getSceneX() - offsetX;
                double newY = dragEvent.getSceneY() - offsetY;

                double deltaX = newX - textLabel.getLayoutX();
                double deltaY = newY - textLabel.getLayoutY();

                newX = Math.max(0, Math.min(newX, canvas.getWidth() - textLabel.getWidth()));
                newY = Math.max(0, Math.min(newY, canvas.getHeight() - textLabel.getHeight()));

                textLabel.setLayoutX(newX);
                textLabel.setLayoutY(newY);

                // Move all selected gates
                for (Map.Entry<ImageView, LogicGate> entry : canvas.getGateImageViews().entrySet()) {
                    ImageView otherImageView = entry.getKey();
                    if (otherImageView.getStyleClass().contains("selected")) {
                        LogicGate otherGate = entry.getValue();

                        newX = otherGate.getImageView().getX() + deltaX;
                        newY = otherGate.getImageView().getY() + deltaY;

                        double clampedX = Math.max(0, Math.min(
                                newX,
                                canvas.getWidth() - otherImageView.getBoundsInLocal().getWidth()));
                        double clampedY = Math.max(0, Math.min(
                                newY,
                                canvas.getHeight() - otherImageView.getBoundsInLocal().getHeight()));

                        otherGate.setPosition(clampedX, clampedY);
                        otherImageView.relocate(clampedX, clampedY);
                    }
                }

                // Move all selected text labels (except the current one)
                for (TextLabel otherLabel : canvas.getTextLabels()) {
                    if (otherLabel != textLabel && otherLabel.getStyleClass().contains("selected")) {
                        double newLabelX = otherLabel.getLayoutX() + deltaX;
                        double newLabelY = otherLabel.getLayoutY() + deltaY;

                        double clampedX = Math.max(0,
                                Math.min(newLabelX, canvas.getWidth() - otherLabel.getWidth()));
                        double clampedY = Math.max(0,
                                Math.min(newLabelY, canvas.getHeight() - otherLabel.getHeight()));

                        otherLabel.setLayoutX(clampedX);
                        otherLabel.setLayoutY(clampedY);
                    }
                }

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
                deselectAllLabels();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof TextLabel)) {
                if (canvas.getOpenContextMenu() != null) {
                    canvas.getOpenContextMenu().hide();
                    canvas.setOpenContextMenu(null);
                }
                canvas.requestFocus();
            }
        });
    }

    private void deselectAllLabelsExcept(TextLabel textLabel) {
        for (TextLabel label : canvas.getTextLabels()) {
            if (!label.equals(textLabel)) {
                label.getStyleClass().remove("selected");
            }
        }
    }

    public void deselectAllLabels() {
        for (TextLabel label : canvas.getTextLabels()) {
            label.getStyleClass().remove("selected");
        }
    }

    public void handleCanvasClick(MouseEvent event) {
        if (!(event.getTarget() instanceof ImageView)) {
            if (justSelected) {
                justSelected = false;
                return;
            }
            canvas.requestFocus();
            return;
        } else if (event.getTarget() instanceof ImageView) {
            ImageView clickedImageView = (ImageView) event.getTarget();
            if (highlightedGate != null
                    && !highlightedGate.getImageView().equals(clickedImageView)) {
                canvas.getGateManager().deselectAllGates();
                LogicGate clickedGate = canvas.getGateImageViews().get(clickedImageView);
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

    private void deselectAllGatesExcept(ImageView exceptImageView) {
        canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey() != exceptImageView)
                .forEach(entry -> {
                    entry.getValue().getImageView().getStyleClass().remove("selected");
                    if (entry.getValue() instanceof SwitchGate) {
                        ((SwitchGate) entry.getValue()).setSelected(false);
                    }
                });
    }

    private void showContextMenu(ImageView imageView, LogicGate gate, MouseEvent event) {
        if (canvas.getOpenContextMenu() != null) {
            canvas.getOpenContextMenu().hide();
        }
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            canvas.getGateManager().removeGate(imageView);
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
        canvas.setOpenContextMenu(contextMenu);
        event.consume();
    }

    private void showPropertiesDialog(LogicGate gate) {
        if (canvas.getOpenContextMenu() != null) {
            canvas.getOpenContextMenu().hide();
            canvas.setOpenContextMenu(null);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gate Properties");
        alert.setHeaderText("Properties for " + gate.getClass().getSimpleName());

        Spinner<Integer> maxOutputSpinner = new Spinner<>(1, 10, gate.getMaxOutputConnections());
        maxOutputSpinner.setEditable(true);
        maxOutputSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            gate.setMaxOutputConnections(newValue);
        });

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
                    param.getValue().length > index ? (param.getValue()[index] ? "true" : "false") : "N/A"));
            table.getColumns().add(inputCol);
            inputCol.setPrefWidth(75);
        }

        TableColumn<Boolean[], String> outputCol = new TableColumn<>("Output");
        outputCol.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue()[param.getValue().length - 1] ? "true" : "false"));
        table.getColumns().add(outputCol);
        outputCol.setPrefWidth(75);

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

    public void generateAndDisplayCompleteTruthTable() {
        List<LogicGate> selectedGates = canvas.getSelectedGates();

        List<SwitchGate> switchGates = new ArrayList<>();
        Lightbulb lightbulb = null;

        for (LogicGate gate : selectedGates) {
            if (gate instanceof SwitchGate) {
                switchGates.add((SwitchGate) gate);
            } else if (gate instanceof Lightbulb) {
                lightbulb = (Lightbulb) gate;
            }
        }

        if (switchGates.isEmpty() || lightbulb == null) {
            System.out.println("SwitchGates or Lightbulb not found in the selected gates.");
            return;
        }

        int numInputs = switchGates.size();
        int totalCombinations = 1 << numInputs;

        Boolean[][] truthTableInputs = new Boolean[totalCombinations][numInputs];
        Boolean[] truthTableOutputs = new Boolean[totalCombinations];

        boolean[] initialStates = new boolean[numInputs];
        for (int i = 0; i < numInputs; i++) {
            initialStates[i] = switchGates.get(i).getState();
        }

        for (int i = 0; i < totalCombinations; i++) {
            for (int j = 0; j < numInputs; j++) {
                truthTableInputs[i][j] = (i & (1 << j)) != 0;
            }
        }

        for (int i = 0; i < totalCombinations; i++) {
            for (int j = 0; j < numInputs; j++) {
                switchGates.get(j).setState(truthTableInputs[i][j]);
            }

            truthTableOutputs[i] = lightbulb.evaluate();
        }

        for (int i = 0; i < numInputs; i++) {
            switchGates.get(i).setState(initialStates[i]);
        }
        lightbulb.evaluate();

        displaySimplifiedTruthTable(truthTableInputs, truthTableOutputs);
    }

    private void displaySimplifiedTruthTable(Boolean[][] inputs, Boolean[] outputs) {
        TableView<List<String>> table = new TableView<>();
        ObservableList<List<String>> data = FXCollections.observableArrayList();

        for (int i = 0; i < inputs[0].length; i++) {
            final int colIndex = i;
            TableColumn<List<String>, String> inputColumn = new TableColumn<>("I" + (i + 1));
            inputColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
            table.getColumns().add(inputColumn);
        }

        TableColumn<List<String>, String> outputColumn = new TableColumn<>("O1");
        outputColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(inputs[0].length)));
        table.getColumns().add(outputColumn);

        for (int i = 0; i < inputs.length; i++) {
            List<String> row = new ArrayList<>();
            for (Boolean input : inputs[i]) {
                row.add(input ? "true" : "false");
            }
            row.add(outputs[i] ? "true" : "false");
            data.add(row);
        }

        table.setItems(data);

        Stage stage = new Stage();
        stage.setTitle("Simplified Truth Table");
        Scene scene = new Scene(table, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

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

    private void resetInteractionHandlers() {
        canvas.setOnMouseMoved(null);
        canvas.setOnMouseClicked(null);
    }

    public void initializeSelectionMechanism() {
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.BLUE.deriveColor(0, 1.2, 1, 0.2));
        selectionRect.setVisible(false);
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
            if (canvas.getCurrentCursorMode() == CursorMode.POINTER && isSelecting
                    && (selectionRect.getWidth() > dragThreshold
                            || selectionRect.getHeight() > dragThreshold)) {
                this.selectGatesInRectangle();
            }
            selectionRect.setVisible(false);
            justSelected = false;
            isSelecting = false;
        });
    }

    public void setupOutputInteraction(Circle outputMarker, LogicGate gate) {
        outputMarker.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && canvas.getCurrentLine() == null
                    && gate.canAddOutputConnection()) {
                Point2D outputPos = outputMarker.localToParent(outputMarker.getCenterX(), outputMarker.getCenterY());
                canvas.setCurrentLine(new Line(outputPos.getX(), outputPos.getY(), event.getX(), event.getY()));
                Color lineColor = gate.evaluate() ? Color.RED : Color.BLACK;
                canvas.getCurrentLine().setStroke(lineColor);
                canvas.getCurrentLine().setStrokeWidth(3.5);
                canvas.getChildren().add(canvas.getCurrentLine());
                canvas.getLineToStartGateMap().put(canvas.getCurrentLine(), gate);
                canvas.getConnectionManager().setStartGate(gate);
                gate.addOutputConnection(canvas.getCurrentLine());
                canvas.getInteractionManager().setupConnectionHandlers();
                event.consume();
            }
        });
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

    public void setHighlightedGate(LogicGate highlightedGate) {
        this.highlightedGate = highlightedGate;
    }
}
