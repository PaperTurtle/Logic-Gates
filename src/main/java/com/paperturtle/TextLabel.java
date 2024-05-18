package com.paperturtle;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
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
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class TextLabel extends Group implements CircuitComponent {
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

    public void removeSelf() {
        Parent parent = this.getParent();
        if (parent instanceof CircuitCanvas) {
            ((CircuitCanvas) parent).removeTextLabel(this);
        }
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setOnAction(e -> removeSelf());
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
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textField = new TextField(labelText.getText());
        ColorPicker backgroundColorPicker = new ColorPicker((Color) background.getFill());
        ColorPicker textColorPicker = new ColorPicker((Color) labelText.getFill());

        RadioButton autoSizeButton = new RadioButton("Automatic size");
        RadioButton fixedSizeButton = new RadioButton("Fixed size");
        ToggleGroup sizeGroup = new ToggleGroup();
        autoSizeButton.setToggleGroup(sizeGroup);
        fixedSizeButton.setToggleGroup(sizeGroup);

        ComboBox<String> fontPicker = new ComboBox<>();
        fontPicker.getItems().addAll(Font.getFamilies());
        fontPicker.setValue(labelText.getFont().getFamily());

        ComboBox<String> alignmentComboBox = new ComboBox<>();
        alignmentComboBox.getItems().addAll("Left", "Center", "Right");
        alignmentComboBox.setValue("Center");

        TextField widthField = new TextField(String.valueOf(width));
        TextField heightField = new TextField(String.valueOf(height));

        ComboBox<Integer> fontSizeComboBox = new ComboBox<>();
        for (int i = 1; i <= 48; i++) {
            fontSizeComboBox.getItems().add(i);
        }
        fontSizeComboBox.setValue((int) labelText.getFont().getSize());

        Label widthLabel = new Label("Width:");
        Label heightLabel = new Label("Height:");

        CheckBox boldCheckBox = new CheckBox("Bold");
        CheckBox italicCheckBox = new CheckBox("Italic");
        CheckBox underlineCheckBox = new CheckBox("Underline");
        CheckBox strikethroughCheckBox = new CheckBox("Strikethrough");

        if (labelText.getFont().getStyle().contains("Bold")) {
            boldCheckBox.setSelected(true);
        }
        if (labelText.getFont().getStyle().contains("Italic")) {
            italicCheckBox.setSelected(true);
        }
        if (labelText.isUnderline()) {
            underlineCheckBox.setSelected(true);
        }
        if (labelText.isStrikethrough()) {
            strikethroughCheckBox.setSelected(true);
        }

        if (isAutoSize) {
            autoSizeButton.setSelected(true);
        }

        Text previewText = new Text(labelText.getText());
        previewText.setFill(labelText.getFill());
        previewText.setFont(labelText.getFont());
        previewText.setUnderline(labelText.isUnderline());
        previewText.setStrikethrough(labelText.isStrikethrough());
        Rectangle previewBackground = new Rectangle(100, 30);
        previewBackground.setFill(background.getFill());

        StackPane previewBox = new StackPane(previewBackground, previewText);

        fontSizeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Font newFont = Font.font(labelText.getFont().getFamily(),
                        boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                        italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                        newVal);
                previewText.setFont(newFont);
                if (isAutoSize) {
                    adjustPreviewSize(previewText, previewBackground);
                }
            }
        });

        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            previewText.setText(newVal);
        });

        fontPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            Font newFont = Font.font(newVal,
                    boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                    italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSizeComboBox.getValue());
            previewText.setFont(newFont);
            if (isAutoSize) {
                adjustPreviewSize(previewText, previewBackground);
            }
        });

        backgroundColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            previewBackground.setFill(newVal);
        });

        textColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            previewText.setFill(newVal);
        });

        boldCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            Font newFont = Font.font(fontPicker.getValue(),
                    newVal ? FontWeight.BOLD : FontWeight.NORMAL,
                    italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSizeComboBox.getValue());
            previewText.setFont(newFont);
        });

        italicCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            Font newFont = Font.font(fontPicker.getValue(),
                    boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                    newVal ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSizeComboBox.getValue());
            previewText.setFont(newFont);
        });

        underlineCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            previewText.setUnderline(newVal);
        });

        strikethroughCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            previewText.setStrikethrough(newVal);
        });

        grid.add(new Label("Text:"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(new Label("Background Color:"), 0, 1);
        grid.add(backgroundColorPicker, 1, 1);
        grid.add(new Label("Text Color:"), 0, 2);
        grid.add(textColorPicker, 1, 2);
        grid.add(new Label("Font:"), 0, 3);
        grid.add(fontPicker, 1, 3);
        grid.add(new Label("Font size:"), 0, 4);
        grid.add(fontSizeComboBox, 1, 4);
        grid.add(boldCheckBox, 0, 5);
        grid.add(italicCheckBox, 1, 5);
        grid.add(underlineCheckBox, 0, 6);
        grid.add(strikethroughCheckBox, 1, 6);
        grid.add(new Label("Size:"), 0, 7);
        grid.add(autoSizeButton, 1, 7);
        grid.add(fixedSizeButton, 1, 8);

        if (!isAutoSize) {
            fixedSizeButton.setSelected(true);
            grid.add(widthLabel, 0, 9);
            grid.add(widthField, 1, 9);
            grid.add(heightLabel, 0, 10);
            grid.add(heightField, 1, 10);
        }

        grid.add(new Label("Alignment:"), 0, 11);
        grid.add(alignmentComboBox, 1, 11);
        grid.add(new Label("Preview:"), 0, 12);
        grid.add(previewBox, 1, 12);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> textField.requestFocus());

        sizeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == autoSizeButton) {
                grid.getChildren().removeAll(widthLabel, widthField, heightLabel, heightField);
                double newWidth = previewText.getBoundsInLocal().getWidth() + 20;
                double newHeight = previewText.getBoundsInLocal().getHeight() + 20;
                previewBackground.setWidth(newWidth);
                previewBackground.setHeight(newHeight);
                switch (alignmentComboBox.getValue()) {
                    case "Left":
                        previewText.setLayoutX(10);
                        break;
                    case "Right":
                        previewText.setLayoutX(newWidth - previewText.getBoundsInLocal().getWidth() - 10);
                        break;
                }
                previewText.setLayoutY((newHeight / 2) + (previewText.getBoundsInLocal().getHeight() / 4));
                widthField.setText(String.valueOf(newWidth));
                heightField.setText(String.valueOf(newHeight));
            } else {
                if (!grid.getChildren().contains(widthLabel)) {
                    grid.add(widthLabel, 0, 9);
                }
                if (!grid.getChildren().contains(widthField)) {
                    grid.add(widthField, 1, 9);
                }
                if (!grid.getChildren().contains(heightLabel)) {
                    grid.add(heightLabel, 0, 10);
                }
                if (!grid.getChildren().contains(heightField)) {
                    grid.add(heightField, 1, 10);
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
                Font newFont = Font.font(fontPicker.getValue(),
                        boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                        italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                        fontSizeComboBox.getValue());
                labelText.setFont(newFont);
                labelText.setUnderline(underlineCheckBox.isSelected());
                labelText.setStrikethrough(strikethroughCheckBox.isSelected());
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

                double padding = 10;
                switch (alignmentComboBox.getValue()) {
                    case "Left":
                        labelText.setTextAlignment(TextAlignment.LEFT);
                        labelText.setLayoutX(padding);
                        break;
                    case "Center":
                        labelText.setTextAlignment(TextAlignment.CENTER);
                        labelText.setLayoutX((width - labelText.getBoundsInLocal().getWidth()) / 2);
                        break;
                    case "Right":
                        labelText.setTextAlignment(TextAlignment.RIGHT);
                        labelText.setLayoutX(width - labelText.getBoundsInLocal().getWidth() - padding);
                        break;
                }

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

    private void adjustPreviewSize(Text previewText, Rectangle previewBackground) {
        double newWidth = previewText.getBoundsInLocal().getWidth() + 20;
        double newHeight = previewText.getBoundsInLocal().getHeight() + 20;
        previewBackground.setWidth(newWidth);
        previewBackground.setHeight(newHeight);
        previewText.setLayoutX((newWidth - previewText.getBoundsInLocal().getWidth()) / 2);
        previewText.setLayoutY((newHeight / 2) + (previewText.getBoundsInLocal().getHeight() / 4));
    }

    @Override
    public String getComponentType() {
        return "textLabel";
    }

    public String getLabel() {
        return labelText.getText();
    }

    public void setLabel(String label) {
        labelText.setText(label);
    }

    public void setWidth(double width) {
        this.width = width;
        background.setWidth(width);
    }

    public void setHeight(double height) {
        this.height = height;
        background.setHeight(height);
    }

    public void setAutoSize(boolean isAutoSize) {
        this.isAutoSize = isAutoSize;
    }

    public boolean isAutoSize() {
        return isAutoSize;
    }

    public void setFontFamily(String fontFamily) {
        labelText.setFont(new Font(fontFamily, labelText.getFont().getSize()));
    }

    public String getFontFamily() {
        return labelText.getFont().getFamily();
    }

    public void setFontSize(int fontSize) {
        labelText.setFont(new Font(labelText.getFont().getFamily(), fontSize));
    }

    public int getFontSize() {
        return (int) labelText.getFont().getSize();
    }

    public void setFillColor(Color color) {
        labelText.setFill(color);
    }

    public void setFillColor(String color) {
        setFillColor(Color.web(color));
    }

    public Color getFillColor() {
        return (Color) labelText.getFill();
    }

    public void setTextAlignment(TextAlignment alignment) {
        labelText.setTextAlignment(alignment);
    }

    public TextAlignment getTextAlignment() {
        return labelText.getTextAlignment();
    }

}
