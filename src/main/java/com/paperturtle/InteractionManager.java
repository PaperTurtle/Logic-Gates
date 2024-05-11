package com.paperturtle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.paperturtle.CircuitCanvas.CursorMode;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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
import javafx.util.Pair;

public class InteractionManager {
    private CircuitCanvas canvas;

    public InteractionManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    // Done
    public void handleMousePressed(MouseEvent event) {
        double transformedX = canvas.getScrollPane().getHvalue()
                * (canvas.getScrollPane().getContent().getBoundsInLocal().getWidth()
                        - canvas.getScrollPane().getViewportBounds().getWidth());
        double transformedY = canvas.getScrollPane().getVvalue()
                * (canvas.getScrollPane().getContent().getBoundsInLocal().getHeight()
                        - canvas.getScrollPane().getViewportBounds().getHeight());

        canvas.setLastX(event.getX() + transformedX - canvas.getVirtualOrigin().getX());
        canvas.setLastY(event.getY() + transformedY - canvas.getVirtualOrigin().getY());

        if (canvas.getCurrentCursorMode() == CursorMode.GRABBY) {
            canvas.setSelecting(false);
        } else if (canvas.getCurrentCursorMode() == CursorMode.POINTER) {
            canvas.setSelecting(true);
            canvas.setLastMouseCoordinates(new Point2D(canvas.getLastX(), canvas.getLastY()));
            canvas.getSelectionRect().setX(canvas.getLastMouseCoordinates().getX());
            canvas.getSelectionRect().setY(canvas.getLastMouseCoordinates().getY());
            canvas.getSelectionRect().setWidth(0);
            canvas.getSelectionRect().setHeight(0);
            canvas.getSelectionRect().setVisible(true);
        }
    }

    private void handleMousePressedForGate(ImageView imageView, LogicGate gate, MouseEvent event) {
        if (canvas.getHighlightedGate() != null && canvas.getHighlightedGate() != gate) {
            canvas.getHighlightedGate().getImageView().getStyleClass().remove("selected");
        }
        canvas.setHighlightedGate(gate);

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

        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getTarget() instanceof Pane) {
                deselectAllGates();
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof ImageView)) {
                if (canvas.getHighlightedGate() != null) {
                    canvas.getHighlightedGate().getImageView().getStyleClass().remove("selected");
                    canvas.setHighlightedGate(null);
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
            textLabel.setUserData(new Object[] { offsetX, offsetY });

            textLabel.setOnMouseDragged(dragEvent -> {
                double newX = dragEvent.getSceneX() - offsetX;
                double newY = dragEvent.getSceneY() - offsetY;

                newX = Math.max(0, Math.min(newX, canvas.getWidth() - textLabel.getWidth()));
                newY = Math.max(0, Math.min(newY, canvas.getHeight() - textLabel.getHeight()));

                textLabel.setLayoutX(newX);
                textLabel.setLayoutY(newY);
            });
            event.consume();
        });
    }

    public void handleCanvasClick(MouseEvent event) {
        if (!(event.getTarget() instanceof ImageView)) {
            if (canvas.isJustSelected()) {
                canvas.setJustSelected(false);
                return;
            }
            deselectAllGates();
            canvas.requestFocus();
            return;
        } else if (event.getTarget() instanceof ImageView) {
            ImageView clickedImageView = (ImageView) event.getTarget();
            if (canvas.getHighlightedGate() != null
                    && !canvas.getHighlightedGate().getImageView().equals(clickedImageView)) {
                deselectAllGates();
                LogicGate clickedGate = canvas.getGateImageViews().get(clickedImageView);
                if (clickedGate != null) {
                    canvas.setHighlightedGate(clickedGate);
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
            boolean intersects = gateView.getBoundsInParent().intersects(canvas.getSelectionRect().getBoundsInParent());
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

    private void deselectAllGates() {
        canvas.getGateImageViews().values().forEach(gate -> {
            gate.getImageView().getStyleClass().remove("selected");
            if (gate instanceof SwitchGate) {
                ((SwitchGate) gate).setSelected(false);
            }
        });
        canvas.setHighlightedGate(null);
    }

    private void deselectAllGatesExcept(ImageView exceptImageView) {
        canvas.getGateImageViews().entrySet().stream()
                .filter(entry -> entry.getKey() != exceptImageView)
                .forEach(entry -> {
                    entry.getKey().getStyleClass().remove("selected");
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
            if (canvas.getHighlightedGate() == gate) {
                canvas.setHighlightedGate(null);
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
            inputCol.setPrefWidth(Region.USE_COMPUTED_SIZE);
        }

        TableColumn<Boolean[], String> outputCol = new TableColumn<>("Output");
        outputCol.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue()[param.getValue().length - 1] ? "1" : "0"));
        table.getColumns().add(outputCol);
        outputCol.setPrefWidth(Region.USE_COMPUTED_SIZE);

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
        canvas.getSelectionRect().setStroke(Color.BLUE);
        canvas.getSelectionRect().setStrokeWidth(1);
        canvas.getSelectionRect().setFill(Color.BLUE.deriveColor(0, 1.2, 1, 0.2));
        canvas.getSelectionRect().setVisible(false);
        canvas.getChildren().add(canvas.getSelectionRect());
        final double dragThreshold = 10.0;

        canvas.setOnMousePressed(event -> {
            canvas.setLastMouseCoordinates(new Point2D(Math.max(0, Math.min(event.getX(),
                    canvas.getWidth())),
                    Math.max(0, Math.min(event.getY(), canvas.getHeight()))));
            canvas.getSelectionRect().setX(canvas.getLastMouseCoordinates().getX());
            canvas.getSelectionRect().setY(canvas.getLastMouseCoordinates().getY());
            canvas.getSelectionRect().setWidth(0);
            canvas.getSelectionRect().setHeight(0);
            canvas.getSelectionRect().setVisible(true);
            canvas.setSelecting(true);
        });

        canvas.setOnMouseDragged(event -> {
            if (canvas.isSelecting()) {
                double x = Math.max(0, Math.min(event.getX(), canvas.getWidth()));
                double y = Math.max(0, Math.min(event.getY(), canvas.getHeight()));
                canvas.getSelectionRect().setWidth(Math.abs(x - canvas.getLastMouseCoordinates().getX()));
                canvas.getSelectionRect().setHeight(Math.abs(y - canvas.getLastMouseCoordinates().getY()));
                canvas.getSelectionRect().setX(Math.min(x, canvas.getLastMouseCoordinates().getX()));
                canvas.getSelectionRect().setY(Math.min(y, canvas.getLastMouseCoordinates().getY()));
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (canvas.getCurrentCursorMode() == CursorMode.POINTER && canvas.isSelecting()
                    && (canvas.getSelectionRect().getWidth() > dragThreshold
                            || canvas.getSelectionRect().getHeight() > dragThreshold)) {
                this.selectGatesInRectangle();
            }
            canvas.getSelectionRect().setVisible(false);
            canvas.setSelecting(false);
            canvas.setJustSelected(false);
        });
    }

}
