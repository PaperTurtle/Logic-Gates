package com.paperturtle.components.utilities;

import java.util.List;

import com.paperturtle.commands.RemoveLabelCommand;
import com.paperturtle.gui.CircuitCanvas;
import com.paperturtle.utils.CircuitComponent;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
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
import javafx.scene.control.ScrollPane;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

    /**
     * Updates the position of the text label within the background rectangle.
     */
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
        removeItem.setOnAction(e -> {
            if (this.getParent() instanceof CircuitCanvas parent) {
                parent.getCommandManager().executeCommand(new RemoveLabelCommand(parent, this));
            }
        });
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

        GridPane grid = createGridPane();

        TextField textField = createTextField();
        ColorPicker backgroundColorPicker = createColorPicker((Color) background.getFill());
        ColorPicker textColorPicker = createColorPicker((Color) labelText.getFill());

        configureColorPicker(backgroundColorPicker);
        configureColorPicker(textColorPicker);

        RadioButton autoSizeButton = new RadioButton("Automatic size");
        fixedSizeButton = new RadioButton("Fixed size");
        ToggleGroup sizeGroup = createToggleGroup(autoSizeButton, fixedSizeButton);

        ComboBox<String> fontPicker = createComboBox(Font.getFamilies(), labelText.getFont().getFamily());
        ComboBox<String> alignmentComboBox = createComboBox(List.of("Left", "Center", "Right"), "Center");

        widthField = createTextField(String.valueOf(width));
        heightField = createTextField(String.valueOf(height));

        ComboBox<Integer> fontSizeComboBox = createFontSizeComboBox();

        Label widthLabel = new Label("Width:");
        Label heightLabel = new Label("Height:");

        CheckBox boldCheckBox = new CheckBox("Bold");
        CheckBox italicCheckBox = new CheckBox("Italic");
        CheckBox underlineCheckBox = new CheckBox("Underline");
        CheckBox strikethroughCheckBox = new CheckBox("Strikethrough");

        setCheckBoxStates(boldCheckBox, italicCheckBox, underlineCheckBox, strikethroughCheckBox);

        if (isAutoSize)
            autoSizeButton.setSelected(true);

        Text previewText = createPreviewText();
        Rectangle previewBackground = createPreviewBackground();
        StackPane previewBox = new StackPane(previewBackground, previewText);

        ChangeListener<Object> previewChangeListener = createPreviewChangeListener(
                textField, fontPicker, boldCheckBox, italicCheckBox, underlineCheckBox,
                strikethroughCheckBox, fontSizeComboBox, previewText, previewBackground);

        addPreviewListeners(previewChangeListener, fontSizeComboBox, textField, fontPicker,
                boldCheckBox, italicCheckBox, underlineCheckBox, strikethroughCheckBox,
                widthField, heightField, backgroundColorPicker, textColorPicker, previewBackground, previewText);

        addComponentsToGrid(grid, textField, backgroundColorPicker, textColorPicker, fontPicker,
                fontSizeComboBox, boldCheckBox, italicCheckBox, underlineCheckBox,
                strikethroughCheckBox, autoSizeButton, fixedSizeButton, widthLabel,
                widthField, heightLabel, heightField, alignmentComboBox, previewBox);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        dialog.getDialogPane().setContent(scrollPane);
        Platform.runLater(textField::requestFocus);

        configureSizeToggle(sizeGroup, autoSizeButton, widthLabel, widthField, heightLabel, heightField, grid,
                previewText, previewBackground);

        dialog.setResultConverter(dialogButton -> handleSave(dialogButton, saveButtonType, textField, textColorPicker,
                backgroundColorPicker, fontPicker, boldCheckBox, italicCheckBox,
                underlineCheckBox, strikethroughCheckBox, fontSizeComboBox,
                alignmentComboBox));

        dialog.setOnCloseRequest(event -> dialog.setResult(null));
        dialog.showAndWait();
    }

    /**
     * Creates a GridPane for the edit dialog.
     * 
     * @return the GridPane for the edit dialog.
     */
    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(Region.USE_PREF_SIZE);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        return grid;
    }

    /**
     * Creates a TextField with the current label text.
     * 
     * @return the TextField with the current label text.
     */
    private TextField createTextField() {
        return new TextField(labelText.getText());
    }

    /**
     * Creates a TextField with the specified value.
     * 
     * @param value the value to set in the TextField.
     * @return the TextField with the specified value.
     */
    private TextField createTextField(String value) {
        return new TextField(value);
    }

    /**
     * Creates a ColorPicker with the specified color.
     * 
     * @param color the color to set in the ColorPicker.
     * @return the ColorPicker with the specified color.
     */
    private ColorPicker createColorPicker(Color color) {
        return new ColorPicker(color);
    }

    /**
     * Configures the ColorPicker with a minimum width and height.
     * 
     * @param colorPicker the ColorPicker to configure.
     */
    private void configureColorPicker(ColorPicker colorPicker) {
        colorPicker.setMinWidth(150);
        colorPicker.setMinHeight(30);
    }

    /**
     * Creates a ToggleGroup for the specified RadioButtons.
     * 
     * @param buttons the RadioButtons to add to the ToggleGroup.
     * 
     * @return the ToggleGroup with the specified RadioButtons.
     */
    private ToggleGroup createToggleGroup(RadioButton... buttons) {
        ToggleGroup group = new ToggleGroup();
        for (RadioButton button : buttons) {
            button.setToggleGroup(group);
        }
        return group;
    }

    /**
     * Creates a ComboBox with the specified items and value.
     * 
     * @param items the items to add to the ComboBox.
     * @param value the value to set in the ComboBox.
     * 
     * @return the ComboBox with the specified items and value.
     */
    private ComboBox<String> createComboBox(List<String> items, String value) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.setValue(value);
        return comboBox;
    }

    /**
     * Creates a ComboBox with font sizes from 1 to 48.
     * 
     * @return the ComboBox with font sizes from 1 to 48.
     */
    private ComboBox<Integer> createFontSizeComboBox() {
        ComboBox<Integer> comboBox = new ComboBox<>();
        for (int i = 1; i <= 48; i++) {
            comboBox.getItems().add(i);
        }
        comboBox.setValue((int) labelText.getFont().getSize());
        return comboBox;
    }

    /**
     * Sets the states of the CheckBoxes based on the current font style.
     * 
     * @param bold          the CheckBox for bold.
     * @param italic        the CheckBox for italic.
     * @param underline     the CheckBox for underline.
     * @param strikethrough the CheckBox for strikethrough.
     */
    private void setCheckBoxStates(CheckBox bold, CheckBox italic, CheckBox underline, CheckBox strikethrough) {
        if (labelText.getFont().getStyle().contains("Bold"))
            bold.setSelected(true);
        if (labelText.getFont().getStyle().contains("Italic"))
            italic.setSelected(true);
        if (labelText.isUnderline())
            underline.setSelected(true);
        if (labelText.isStrikethrough())
            strikethrough.setSelected(true);
    }

    /**
     * Creates a Text object with the current label text.
     * 
     * @return the Text object with the current label text.
     */
    private Text createPreviewText() {
        Text text = new Text(labelText.getText());
        text.setFill(labelText.getFill());
        text.setFont(labelText.getFont());
        text.setUnderline(labelText.isUnderline());
        text.setStrikethrough(labelText.isStrikethrough());
        return text;
    }

    /**
     * Creates a Rectangle with the current background color.
     * 
     * @return the Rectangle with the current background color.
     */
    private Rectangle createPreviewBackground() {
        Rectangle rectangle = new Rectangle(width, height);
        rectangle.setFill(background.getFill());
        return rectangle;
    }

    /**
     * Creates a ChangeListener for updating the preview text and background.
     * 
     * @param textField             the TextField for the text.
     * @param fontPicker            the ComboBox for the font.
     * @param boldCheckBox          the CheckBox for bold.
     * @param italicCheckBox        the CheckBox for italic.
     * @param underlineCheckBox     the CheckBox for underline.
     * @param strikethroughCheckBox the CheckBox for strikethrough.
     * @param fontSizeComboBox      the ComboBox for the font size.
     * @param previewText           the preview Text.
     * @param previewBackground     the preview Rectangle.
     * @return the ChangeListener for updating the preview text and background.
     */
    private ChangeListener<Object> createPreviewChangeListener(TextField textField, ComboBox<String> fontPicker,
            CheckBox boldCheckBox, CheckBox italicCheckBox, CheckBox underlineCheckBox, CheckBox strikethroughCheckBox,
            ComboBox<Integer> fontSizeComboBox, Text previewText, Rectangle previewBackground) {
        return (obs, oldVal, newVal) -> {
            previewText.setText(textField.getText());
            Font previewFont = Font.font(
                    fontPicker.getValue(),
                    boldCheckBox.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                    italicCheckBox.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                    fontSizeComboBox.getValue());
            previewText.setFont(previewFont);
            previewText.setUnderline(underlineCheckBox.isSelected());
            previewText.setStrikethrough(strikethroughCheckBox.isSelected());
            updatePreviewSize(previewText, previewBackground);
        };
    }

    /**
     * Adds listeners to the components that update the preview text and background.
     * 
     * @param previewChangeListener the ChangeListener for updating the preview text
     *                              and background.
     * @param fontSizeComboBox      the ComboBox for the font size.
     * @param textField             the TextField for the text.
     * @param fontPicker            the ComboBox for the font.
     * @param boldCheckBox          the CheckBox for bold.
     * @param italicCheckBox        the CheckBox for italic.
     * @param underlineCheckBox     the CheckBox for underline.
     * @param strikethroughCheckBox the CheckBox for strikethrough.
     * @param widthField            the TextField for the width.
     * @param heightField           the TextField for the height.
     * @param backgroundColorPicker the ColorPicker for the background color.
     * @param textColorPicker       the ColorPicker for the text color.
     * @param previewBackground     the preview Rectangle.
     * @param previewText           the preview Text.
     */
    private void addPreviewListeners(ChangeListener<Object> previewChangeListener, ComboBox<Integer> fontSizeComboBox,
            TextField textField, ComboBox<String> fontPicker, CheckBox boldCheckBox, CheckBox italicCheckBox,
            CheckBox underlineCheckBox, CheckBox strikethroughCheckBox, TextField widthField, TextField heightField,
            ColorPicker backgroundColorPicker, ColorPicker textColorPicker, Rectangle previewBackground,
            Text previewText) {
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
    }

    /**
     * Adds components to the GridPane.
     * 
     * @param grid                  the GridPane to add components to.
     * @param textField             the TextField for the text.
     * @param backgroundColorPicker the ColorPicker for the background color.
     * @param textColorPicker       the ColorPicker for the text color.
     * @param fontPicker            the ComboBox for the font.
     * @param fontSizeComboBox      the ComboBox for the font size.
     * @param boldCheckBox          the CheckBox for bold.
     * @param italicCheckBox        the CheckBox for italic.
     * @param underlineCheckBox     the CheckBox for underline.
     * @param strikethroughCheckBox the CheckBox for strikethrough.
     * @param autoSizeButton        the RadioButton for automatic size.
     * @param fixedSizeButton       the RadioButton for fixed size.
     * @param widthLabel            the Label for the width.
     * @param widthField            the TextField for the width.
     * @param heightLabel           the Label for the height.
     * @param heightField           the TextField for the height.
     * @param alignmentComboBox     the ComboBox for the alignment.
     * @param previewBox            the StackPane for the preview.
     */
    private void addComponentsToGrid(GridPane grid, TextField textField, ColorPicker backgroundColorPicker,
            ColorPicker textColorPicker, ComboBox<String> fontPicker, ComboBox<Integer> fontSizeComboBox,
            CheckBox boldCheckBox, CheckBox italicCheckBox, CheckBox underlineCheckBox, CheckBox strikethroughCheckBox,
            RadioButton autoSizeButton, RadioButton fixedSizeButton, Label widthLabel, TextField widthField,
            Label heightLabel, TextField heightField, ComboBox<String> alignmentComboBox, StackPane previewBox) {

        int row = 0;
        addRowToGrid(grid, "Text:", textField, row++);
        addRowToGrid(grid, "Background Color:", backgroundColorPicker, row++);
        addRowToGrid(grid, "Text Color:", textColorPicker, row++);
        addRowToGrid(grid, "Font:", fontPicker, row++);
        addRowToGrid(grid, "Font size:", fontSizeComboBox, row++);
        addRowToGrid(grid, "Bold:", boldCheckBox, row++);
        addRowToGrid(grid, "Italic:", italicCheckBox, row++);
        addRowToGrid(grid, "Underline:", underlineCheckBox, row++);
        addRowToGrid(grid, "Strikethrough:", strikethroughCheckBox, row++);
        addRowToGrid(grid, "Size:", autoSizeButton, row++);
        addRowToGrid(grid, "", fixedSizeButton, row++);

        if (!isAutoSize) {
            fixedSizeButton.setSelected(true);
            addRowToGrid(grid, "Width:", widthField, row++);
            addRowToGrid(grid, "Height:", heightField, row++);
        }

        addRowToGrid(grid, "Alignment:", alignmentComboBox, row++);
        addRowToGrid(grid, "Preview:", previewBox, row);
    }

    private void addRowToGrid(GridPane grid, String labelText, Node control, int row) {
        grid.add(new Label(labelText), 0, row);
        grid.add(control, 1, row);
    }

    /**
     * Configures the size toggle for the TextLabel.
     * 
     * @param sizeGroup         the ToggleGroup for the size buttons.
     * @param autoSizeButton    the RadioButton for automatic size.
     * @param widthLabel        the Label for the width.
     * @param widthField        the TextField for the width.
     * @param heightLabel       the Label for the height.
     * @param heightField       the TextField for the height.
     * @param grid              the GridPane for the edit dialog.
     * @param previewText       the preview Text.
     * @param previewBackground the preview Rectangle.
     */
    private void configureSizeToggle(ToggleGroup sizeGroup, RadioButton autoSizeButton, Label widthLabel,
            TextField widthField, Label heightLabel, TextField heightField, GridPane grid, Text previewText,
            Rectangle previewBackground) {
        sizeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == autoSizeButton) {
                grid.getChildren().removeAll(widthLabel, widthField, heightLabel, heightField);
            } else {
                addIfNotPresent(grid, widthLabel, 0, 9);
                addIfNotPresent(grid, widthField, 1, 9);
                addIfNotPresent(grid, heightLabel, 0, 10);
                addIfNotPresent(grid, heightField, 1, 10);
            }
            updatePreviewSize(previewText, previewBackground);
        });
    }

    /**
     * Adds a node to the GridPane if it is not already present.
     * 
     * @param grid the GridPane to add the node to.
     * @param node the node to add.
     * @param col  the column to add the node to.
     * @param row  the row to add the node to.
     */
    private void addIfNotPresent(GridPane grid, Node node, int col, int row) {
        if (!grid.getChildren().contains(node)) {
            grid.add(node, col, row);
        }
    }

    /**
     * Handles the save action for the TextLabel.
     * 
     * @param dialogButton          the ButtonType of the dialog.
     * @param saveButtonType        the ButtonType for saving.
     * @param textField             the TextField for the text.
     * @param textColorPicker       the ColorPicker for the text color.
     * @param backgroundColorPicker the ColorPicker for the background color.
     * @param fontPicker            the ComboBox for the font.
     * @param boldCheckBox          the CheckBox for bold.
     * @param italicCheckBox        the CheckBox for italic.
     * @param underlineCheckBox     the CheckBox for underline.
     * @param strikethroughCheckBox the CheckBox for strikethrough.
     * @param fontSizeComboBox      the ComboBox for the font size.
     * @param alignmentComboBox     the ComboBox for the alignment.
     * @return null.
     */
    private Void handleSave(ButtonType dialogButton, ButtonType saveButtonType, TextField textField,
            ColorPicker textColorPicker,
            ColorPicker backgroundColorPicker, ComboBox<String> fontPicker, CheckBox boldCheckBox,
            CheckBox italicCheckBox,
            CheckBox underlineCheckBox, CheckBox strikethroughCheckBox, ComboBox<Integer> fontSizeComboBox,
            ComboBox<String> alignmentComboBox) {
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

    public Text getLabelText() {
        return labelText;
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
     * Get background of the TextLabel.
     */
    public Rectangle getBackground() {
        return background;
    }

    /**
     * Sets the background color of the TextLabel.
     * 
     * @param color the new background color for the TextLabel.
     */
    public void setBackgroundColor(Color color) {
        background.setFill(color);
    }

    /**
     * Sets the background color of the TextLabel.
     * 
     * @param color the new background color for the TextLabel, specified as a web
     *              color string.
     */
    public void setBackgroundColor(String color) {
        setBackgroundColor(Color.web(color));
    }

    /**
     * Gets the background color of the TextLabel.
     * 
     * @return the background color of the TextLabel.
     */
    public Color getBackgroundColor() {
        return (Color) background.getFill();
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
     * Gets the font of the label text.
     */
    public Font getFont() {
        return labelText.getFont();
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

    /**
     * Gets the text of the label.
     */
    public String getText() {
        return labelText.getText();
    }

    /**
     * Sets the text of the label.
     * 
     * @param text The new text to set.
     */
    public void setText(String text) {
        labelText.setText(text);
    }
}
