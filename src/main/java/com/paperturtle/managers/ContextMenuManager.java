package com.paperturtle.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.paperturtle.components.LogicGate;
import com.paperturtle.components.inputs.ClockGate;
import com.paperturtle.components.outputs.FourBitDigitGate;
import com.paperturtle.components.outputs.Lightbulb;
import com.paperturtle.gui.CircuitCanvas;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

/**
 * The ContextMenuManager class is responsible for managing the context menu for
 * logic gates in the circuit canvas.
 * 
 * @author Seweryn Czabanowski
 */
public class ContextMenuManager {
    /**
     * The canvas on which the circuit is drawn.
     */
    private CircuitCanvas canvas;

    /**
     * The logic gate that is currently highlighted.
     */
    private LogicGate highlightedGate = null;

    /**
     * Constructs a ContextMenuManager for the specified circuit canvas.
     * 
     * @param canvas the circuit canvas to manage
     */
    public ContextMenuManager(CircuitCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Shows the context menu for the specified logic gate.
     * 
     * @param imageView the ImageView of the gate
     * @param gate      the logic gate for which the context menu is shown
     * @param event     the mouse event that triggered the context menu
     */
    public void showContextMenu(ImageView imageView, LogicGate gate, MouseEvent event) {
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

    /**
     * Shows the properties dialog for the specified logic gate.
     * 
     * @param gate the logic gate for which the properties dialog is shown
     */
    private void showPropertiesDialog(LogicGate gate) {
        if (canvas.getOpenContextMenu() != null) {
            canvas.getOpenContextMenu().hide();
            canvas.setOpenContextMenu(null);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gate Properties");
        alert.setHeaderText("Properties for " + gate.getClass().getSimpleName());

        TableView<Object[]> table = new TableView<>();
        List<Pair<Boolean[], ?>> pairList;

        if (gate instanceof FourBitDigitGate) {
            pairList = ((FourBitDigitGate) gate).getNumericTruthTableData().stream()
                    .map(pair -> new Pair<>(pair.getKey(), (Object) pair.getValue()))
                    .collect(Collectors.toList());
        } else {
            pairList = gate.getTruthTableData().stream()
                    .map(pair -> new Pair<>(pair.getKey(), (Object) pair.getValue()))
                    .collect(Collectors.toList());
        }

        List<Object[]> dataList = new ArrayList<>();
        for (Pair<Boolean[], ?> pair : pairList) {
            Object[] row = new Object[pair.getKey().length + 1];
            System.arraycopy(pair.getKey(), 0, row, 0, pair.getKey().length);
            row[pair.getKey().length] = pair.getValue();
            dataList.add(row);
        }

        ObservableList<Object[]> data = FXCollections.observableArrayList(dataList);
        table.setItems(data);

        int numInputs = pairList.isEmpty() || pairList.get(0).getKey().length == 0 ? 0
                : pairList.get(0).getKey().length;
        for (int i = 0; i < numInputs; i++) {
            TableColumn<Object[], String> inputCol = new TableColumn<>("Input " + (i + 1));
            final int index = i;
            inputCol.setCellValueFactory(param -> new SimpleStringProperty(
                    param.getValue().length > index
                            ? (param.getValue()[index] instanceof Boolean
                                    ? ((Boolean) param.getValue()[index] ? "1" : "0")
                                    : param.getValue()[index].toString())
                            : "N/A"));
            table.getColumns().add(inputCol);
            inputCol.setPrefWidth(75);
        }

        TableColumn<Object[], String> outputCol = new TableColumn<>("Output");
        outputCol.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue()[param.getValue().length - 1] instanceof Boolean
                        ? ((Boolean) param.getValue()[param.getValue().length - 1] ? "1" : "0")
                        : param.getValue()[param.getValue().length - 1].toString()));
        table.getColumns().add(outputCol);
        outputCol.setPrefWidth(75);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
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

        VBox content;

        if (!(gate instanceof FourBitDigitGate) && !(gate instanceof Lightbulb)) {
            Label spinnerLabel = new Label("Max Output Connections:");
            Spinner<Integer> maxOutputSpinner = new Spinner<>();
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10,
                    gate.getMaxOutputConnections());
            maxOutputSpinner.setValueFactory(valueFactory);

            maxOutputSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                gate.setMaxOutputConnections(newValue);
            });

            content = new VBox(10, table, spinnerLabel, maxOutputSpinner);

        } else {
            content = new VBox(10, table);
        }

        content.setPadding(new Insets(10));
        alert.getDialogPane().setContent(content);

        alert.getDialogPane().getStylesheets()
                .add(getClass().getResource("/com/paperturtle/styles.css").toExternalForm());
        table.getStylesheets().add(getClass().getResource("/com/paperturtle/styles.css").toExternalForm());
        alert.showAndWait();
    }
}
