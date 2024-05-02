package com.paperturtle;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class TextLabel extends Group {
    private Rectangle background;
    private Text labelText;
    private double width;
    private double height;
    private boolean isAutoSize;

    public TextLabel(String label, double width, double height) {
        this.width = width;
        this.height = height;
        this.isAutoSize = width == -1 && height == -1;

        background = new Rectangle(width, height);
        background.setFill(Color.BLACK);

        labelText = new Text(label);
        labelText.setFill(Color.WHITE);
        labelText.setFont(new Font("Arial", 16));

        labelText.setLayoutX((width - labelText.getBoundsInLocal().getWidth()) / 2);
        labelText.setLayoutY((height / 2) + (labelText.getBoundsInLocal().getHeight() / 4));

        this.getChildren().addAll(background, labelText);
        setupContextMenu();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> showEditDialog());
        contextMenu.getItems().add(editItem);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
    }

    private void showEditDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Text Label");

        ButtonType saveButtonType = new ButtonType("Save");
        ButtonType cancelButtonType = new ButtonType("Cancel");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType, ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField textField = new TextField(labelText.getText());
        ColorPicker backgroundColorPicker = new ColorPicker((Color) background.getFill());
        ColorPicker textColorPicker = new ColorPicker((Color) labelText.getFill());

        RadioButton autoSizeButton = new RadioButton("Automatic size");
        RadioButton fixedSizeButton = new RadioButton("Fixed size");
        ToggleGroup sizeGroup = new ToggleGroup();
        autoSizeButton.setToggleGroup(sizeGroup);
        fixedSizeButton.setToggleGroup(sizeGroup);

        TextField widthField = new TextField(String.valueOf(width));
        TextField heightField = new TextField(String.valueOf(height));

        Label widthLabel = new Label("Width:");
        Label heightLabel = new Label("Height:");

        if (isAutoSize) {
            autoSizeButton.setSelected(true);
        } else {
            fixedSizeButton.setSelected(true);
            if (!grid.getChildren().contains(widthLabel)) {
                grid.add(widthLabel, 0, 6);
            }
            if (!grid.getChildren().contains(widthField)) {
                grid.add(widthField, 0, 7);
            }
            if (!grid.getChildren().contains(heightLabel)) {
                grid.add(heightLabel, 0, 8);
            }
            if (!grid.getChildren().contains(heightField)) {
                grid.add(heightField, 0, 9);
            }
        }

        Text previewText = new Text(labelText.getText());
        previewText.setFill(labelText.getFill());
        previewText.setFont(labelText.getFont());
        Rectangle previewBackground = new Rectangle(100, 30);
        previewBackground.setFill(background.getFill());

        StackPane previewBox = new StackPane(previewBackground, previewText);

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            previewText.setText(newVal);
        });

        backgroundColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            previewBackground.setFill(newVal);
        });

        textColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            previewText.setFill(newVal);
        });

        grid.add(textField, 0, 0);
        grid.add(backgroundColorPicker, 0, 1);
        grid.add(textColorPicker, 0, 2);
        grid.add(new Label("Size:"), 0, 3);
        grid.add(autoSizeButton, 0, 4);
        grid.add(fixedSizeButton, 0, 5);
        grid.add(previewBox, 0, 10);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> textField.requestFocus());

        sizeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == autoSizeButton) {
                grid.getChildren().removeAll(widthLabel, widthField, heightLabel, heightField);
                double newWidth = previewText.getBoundsInLocal().getWidth() + 20;
                double newHeight = previewText.getBoundsInLocal().getHeight() + 20;
                previewBackground.setWidth(newWidth);
                previewBackground.setHeight(newHeight);
                previewText.setLayoutX((newWidth - previewText.getBoundsInLocal().getWidth()) / 2);
                previewText.setLayoutY((newHeight / 2) + (previewText.getBoundsInLocal().getHeight() / 4));
                widthField.setText(String.valueOf(newWidth));
                heightField.setText(String.valueOf(newHeight));
            } else {
                if (!grid.getChildren().contains(widthLabel)) {
                    grid.add(widthLabel, 0, 6);
                }
                if (!grid.getChildren().contains(widthField)) {
                    grid.add(widthField, 0, 7);
                }
                if (!grid.getChildren().contains(heightLabel)) {
                    grid.add(heightLabel, 0, 8);
                }
                if (!grid.getChildren().contains(heightField)) {
                    grid.add(heightField, 0, 9);
                }
                try {
                    double newWidth = Double.parseDouble(widthField.getText());
                    double newHeight = Double.parseDouble(heightField.getText());
                    previewBackground.setWidth(newWidth);
                    previewBackground.setHeight(newHeight);
                    previewText.setLayoutX((newWidth - previewText.getBoundsInLocal().getWidth()) / 2);
                    previewText.setLayoutY((newHeight / 2) + (previewText.getBoundsInLocal().getHeight() / 4));
                } catch (NumberFormatException e) {
                }
            }
        });

        widthField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (fixedSizeButton.isSelected()) {
                try {
                    double newWidth = Double.parseDouble(newVal);
                    previewBackground.setWidth(newWidth);
                    previewText.setLayoutX((newWidth - previewText.getBoundsInLocal().getWidth()) / 2);
                } catch (NumberFormatException e) {
                }
            }
        });

        heightField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (fixedSizeButton.isSelected()) {
                try {
                    double newHeight = Double.parseDouble(newVal);
                    previewBackground.setHeight(newHeight);
                    previewText.setLayoutY((newHeight / 2) + (previewText.getBoundsInLocal().getHeight() / 4));
                } catch (NumberFormatException e) {
                }
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                labelText.setText(textField.getText());
                labelText.setFill(textColorPicker.getValue());
                background.setFill(backgroundColorPicker.getValue());

                if (fixedSizeButton.isSelected()) {
                    try {
                        width = Double.parseDouble(widthField.getText());
                        height = Double.parseDouble(heightField.getText());
                        isAutoSize = false;
                    } catch (NumberFormatException e) {
                    }
                } else {
                    width = labelText.getBoundsInLocal().getWidth() + 20;
                    height = labelText.getBoundsInLocal().getHeight() + 20;
                    isAutoSize = true;
                }

                background.setWidth(width);
                background.setHeight(height);

                labelText.setLayoutX((width - labelText.getBoundsInLocal().getWidth()) / 2);
                labelText.setLayoutY((height / 2) + (labelText.getBoundsInLocal().getHeight()) / 4);
            }
            return null;
        });

        dialog.setOnCloseRequest(event -> {
            dialog.setResult(null);
        });

        dialog.showAndWait();
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }
}
