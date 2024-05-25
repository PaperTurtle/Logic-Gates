package com.paperturtle.components.utilities;

import com.paperturtle.gui.CircuitCanvas;
import com.paperturtle.utils.CircuitComponent;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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

/**
 * Represents a text label in a digital circuit.
 * A TextLabel displays a label with customizable properties such as font,
 * color, and size.
 * 
 * @author Seweryn Czabanowski
 */
public class TextLabel extends Group implements CircuitComponent {
    /**
     * The background rectangle of the text label.
     */
    private Rectangle background;

    /**
     * The text of the label.
     */
    private Text labelText;

    /**
     * The width of the text label.
     */
    private double width;

    /**
     * The height of the text label.
     */
    private double height;

    /**
     * A flag indicating whether the size of the text label is automatically
     * adjusted to fit the text.
     */
    private boolean isAutoSize;

    /**
     * The radio button for selecting fixed size mode.
     */
    private RadioButton fixedSizeButton;

    /**
     * The text field for entering the width of the text label in fixed size mode.
     */
    private TextField widthField;

    /**
     * The text field for entering the height of the text label in fixed size mode.
     */
    private TextField heightField;

    /**
     * Constructs a TextLabel object with specified label, width, and height.
     * 
     * @param label  the text to display.
     * @param width  the width of the label.
     * @param height the height of the label.
     */
    public TextLabel(String label, double width, double height) {
        this.width = width;
        this.height = height;
        this.isAutoSize = width == -1 && height == -1;

        background = new Rectangle(width, height);
        background.setFill(Color.BLACK);

        labelText = new Text(label);
        labelText.setFill(Color.WHITE);
        labelText.setFont(new Font("Arial", 16));

        updateTextPosition();

        this.getChildren().addAll(background, labelText);

        setupContextMenu();
    }

    public void updateTextPosition() {
        double textWidth = labelText.getBoundsInLocal().getWidth();
        double textHeight = labelText.getBoundsInLocal().getHeight();

        labelText.setLayoutX((width - textWidth) / 2);
        labelText.setLayoutY((height / 2) + (textHeight / 4));
    }

    /**
     * Removes the TextLabel from its parent.
     */
    public void removeSelf() {
        Parent parent = this.getParent();
        if (parent instanceof CircuitCanvas) {
            ((CircuitCanvas) parent).removeTextLabel(this);
        }
    }

    /**
     * Sets up the context menu for the TextLabel.
     */
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem removeItem = new MenuItem("Remove");
        removeItem.setOnAction(e -> removeSelf());
        editItem.setOnAction(e -> showEditDialog());
        contextMenu.getItems().addAll(editItem, removeItem);

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(this, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });
    }

    /**
     * Shows the edit dialog for the TextLabel.
     */
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
        fixedSizeButton = new RadioButton("Fixed size");
        ToggleGroup sizeGroup = new ToggleGroup();
        autoSizeButton.setToggleGroup(sizeGroup);
        fixedSizeButton.setToggleGroup(sizeGroup);

        ComboBox<String> fontPicker = new ComboBox<>();
        fontPicker.getItems().addAll(Font.getFamilies());
        fontPicker.setValue(labelText.getFont().getFamily());

        ComboBox<String> alignmentComboBox = new ComboBox<>();
        alignmentComboBox.getItems().addAll("Left", "Center", "Right");
        alignmentComboBox.setValue("Center");

        widthField = new TextField(String.valueOf(width));
        heightField = new TextField(String.valueOf(height));

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
        Rectangle previewBackground = new Rectangle(width, height); // Set initial width and height
        previewBackground.setFill(background.getFill());

        StackPane previewBox = new StackPane(previewBackground, previewText);

        ChangeListener<Object> previewChangeListener = (obs, oldVal, newVal) -> {
            previewText.setText(textField.getText()); // Update the preview text
            updatePreviewSize(previewText, previewBackground);
        };

        fontSizeComboBox.valueProperty().addListener(previewChangeListener);
        textField.textProperty().addListener(previewChangeListener);
        fontPicker.valueProperty().addListener(previewChangeListener);
        boldCheckBox.selectedProperty().addListener(previewChangeListener);
        italicCheckBox.selectedProperty().addListener(previewChangeListener);
        underlineCheckBox.selectedProperty().addListener(previewChangeListener);
        strikethroughCheckBox.selectedProperty().addListener(previewChangeListener);
        widthField.textProperty().addListener(previewChangeListener);
        heightField.textProperty().addListener(previewChangeListener);

        backgroundColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            previewBackground.setFill(newVal);
        });

        textColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            previewText.setFill(newVal);
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
                updatePreviewSize(previewText, previewBackground);
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
                updatePreviewSize(previewText, previewBackground);
            }
        });

        widthField.textProperty().addListener(previewChangeListener);
        heightField.textProperty().addListener(previewChangeListener);

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

    /**
     * Updates the size of the preview text and background.
     * 
     * @param previewText       the preview text to update.
     * @param previewBackground the preview background to update.
     */
    private void updatePreviewSize(Text previewText, Rectangle previewBackground) {
        double textWidth = previewText.getBoundsInLocal().getWidth() + 20;
        double textHeight = previewText.getBoundsInLocal().getHeight() + 20;

        if (fixedSizeButton.isSelected()) {
            try {
                double currentWidth = Double.parseDouble(widthField.getText());
                double currentHeight = Double.parseDouble(heightField.getText());

                if (textWidth > currentWidth) {
                    currentWidth = textWidth;
                    widthField.setText(String.valueOf(currentWidth));
                }

                textWidth = Math.max(textWidth, currentWidth);
                textHeight = Math.max(textHeight, currentHeight);
            } catch (NumberFormatException e) {
            }
        }

        previewBackground.setWidth(textWidth);
        previewBackground.setHeight(textHeight);
        previewText.setLayoutX((textWidth - previewText.getBoundsInLocal().getWidth()) / 2);
        previewText.setLayoutY((textHeight / 2) + (previewText.getBoundsInLocal().getHeight() / 4));
    }

    /**
     * Returns the height of the TextLabel.
     * 
     * @return the height of the TextLabel.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the width of the TextLabel.
     * 
     * @return the width of the TextLabel.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the label of the TextLabel.
     * 
     * @return the label of the TextLabel.
     */
    @Override
    public String getComponentType() {
        return "textLabel";
    }

    /**
     * Returns the label of the TextLabel.
     * 
     * @return the label of the TextLabel.
     */
    public String getLabel() {
        return labelText.getText();
    }

    /**
     * Sets the label of the TextLabel.
     * 
     * @param label the new label for the TextLabel.
     */
    public void setLabel(String label) {
        labelText.setText(label);
    }

    /**
     * Sets the width of the TextLabel.
     * 
     * @param width the new width for the TextLabel.
     */
    public void setWidth(double width) {
        this.width = width;
        background.setWidth(width);
    }

    /**
     * Sets the height of the TextLabel.
     * 
     * @param height the new height for the TextLabel.
     */
    public void setHeight(double height) {
        this.height = height;
        background.setHeight(height);
    }

    /**
     * Sets the auto size property of the TextLabel.
     * 
     * @param isAutoSize the new auto size value for the TextLabel.
     */
    public void setAutoSize(boolean isAutoSize) {
        this.isAutoSize = isAutoSize;
    }

    /**
     * Returns the auto size property of the TextLabel.
     * 
     * @return the auto size property of the TextLabel.
     */
    public boolean isAutoSize() {
        return isAutoSize;
    }

    /**
     * Sets the font family of the TextLabel.
     * 
     * @param fontFamily the new font family for the TextLabel.
     */
    public void setFontFamily(String fontFamily) {
        labelText.setFont(new Font(fontFamily, labelText.getFont().getSize()));
    }

    /**
     * Returns the font family of the TextLabel.
     * 
     * @return the font family of the TextLabel.
     */
    public String getFontFamily() {
        return labelText.getFont().getFamily();
    }

    /**
     * Sets the font size of the TextLabel.
     * 
     * @param fontSize the new font size for the TextLabel.
     */
    public void setFontSize(int fontSize) {
        labelText.setFont(new Font(labelText.getFont().getFamily(), fontSize));
    }

    /**
     * Returns the font size of the TextLabel.
     * 
     * @return the font size of the TextLabel.
     */
    public int getFontSize() {
        return (int) labelText.getFont().getSize();
    }

    /**
     * Sets the fill color of the TextLabel.
     * 
     * @param color the new fill color for the TextLabel.
     */
    public void setFillColor(Color color) {
        labelText.setFill(color);
    }

    /**
     * Sets the fill color of the TextLabel.
     * 
     * @param color the new fill color for the TextLabel, specified as a web color
     *              string.
     */
    public void setFillColor(String color) {
        setFillColor(Color.web(color));
    }

    /**
     * Returns the fill color of the TextLabel.
     * 
     * @return the fill color of the TextLabel.
     */
    public Color getFillColor() {
        return (Color) labelText.getFill();
    }

    /**
     * Sets the text alignment of the TextLabel.
     * 
     * @param alignment the new text alignment for the TextLabel.
     */
    public void setTextAlignment(TextAlignment alignment) {
        labelText.setTextAlignment(alignment);
    }

    /**
     * Returns the text alignment of the TextLabel.
     * 
     * @return the text alignment of the TextLabel.
     */
    public TextAlignment getTextAlignment() {
        return labelText.getTextAlignment();
    }

    /**
     * Returns the font weight of the label text.
     * 
     * @return FontWeight.BOLD if the font style contains "Bold", FontWeight.NORMAL
     *         otherwise.
     */
    public FontWeight getFontWeight() {
        return labelText.getFont().getStyle().contains("Bold") ? FontWeight.BOLD : FontWeight.NORMAL;
    }

    /**
     * Returns the font posture of the label text.
     * 
     * @return FontPosture.ITALIC if the font style contains "Italic",
     *         FontPosture.REGULAR otherwise.
     */
    public FontPosture getFontPosture() {
        return labelText.getFont().getStyle().contains("Italic") ? FontPosture.ITALIC : FontPosture.REGULAR;
    }

    /**
     * Checks if the label text is underlined.
     * 
     * @return true if the label text is underlined, false otherwise.
     */
    public boolean isUnderline() {
        return labelText.isUnderline();
    }

    /**
     * Checks if the label text is strikethrough.
     * 
     * @return true if the label text is strikethrough, false otherwise.
     */
    public boolean isStrikethrough() {
        return labelText.isStrikethrough();
    }

    /**
     * Sets the font of the label text.
     * 
     * @param font The new font to set.
     */
    public void setFont(Font font) {
        labelText.setFont(font);
    }

    /**
     * Sets the underline property of the label text.
     * 
     * @param isUnderline If true, the label text is underlined. If false, the
     *                    underline is removed.
     */
    public void setUnderline(boolean isUnderline) {
        labelText.setUnderline(isUnderline);
    }

    /**
     * Sets the strikethrough property of the label text.
     * 
     * @param isStrikethrough If true, the label text is strikethrough. If false,
     *                        the strikethrough is removed.
     */
    public void setStrikethrough(boolean isStrikethrough) {
        labelText.setStrikethrough(isStrikethrough);
    }
}
