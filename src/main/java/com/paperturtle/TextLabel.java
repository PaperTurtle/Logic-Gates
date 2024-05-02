package com.paperturtle;

import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

public class TextLabel extends Group {
    private Rectangle background;
    private Text labelText;

    public TextLabel(String label, double width, double height) {
        background = new Rectangle(width, height);
        background.setFill(Color.BLACK);  

        labelText = new Text(label);
        labelText.setFill(Color.WHITE);  
        labelText.setFont(new Font("Arial", 16));  

        labelText.setLayoutX((width - labelText.getBoundsInLocal().getWidth()) / 2);
        labelText.setLayoutY((height / 2) + (labelText.getBoundsInLocal().getHeight() / 4));

        this.getChildren().addAll(background, labelText);
    }
}
