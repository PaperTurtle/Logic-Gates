package com.paperturtle;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class TextLabel extends Group {
    private Rectangle background;
    private Text labelText;
    private double width;
    private double height;

    public TextLabel(String label, double width, double height) {
        this.width = width;
        this.height = height;

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
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField textField = new TextField(labelText.getText());
        ColorPicker colorPicker = new ColorPicker((Color) background.getFill());

        grid.add(textField, 0, 0);
        grid.add(colorPicker, 0, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> textField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                labelText.setText(textField.getText());
                labelText.setLayoutX((width - labelText.getBoundsInLocal().getWidth()) / 2);
                labelText.setLayoutY((height / 2) + (labelText.getBoundsInLocal().getHeight() / 4));
                background.setFill(colorPicker.getValue());
            }
            return null;
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
