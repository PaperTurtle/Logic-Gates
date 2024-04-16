package com.example;

import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class CircuitCanvas extends Pane {
    public CircuitCanvas(double width, double height) {
        super();
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");
    }

    public void drawGate(LogicGate gate, double x, double y) {
        Image image = SvgUtil.loadSvgImage(gate.getSvgFilePath());
        ImageView imageView = new ImageView(image);
        imageView.setX(x);
        imageView.setY(y);
        this.getChildren().add(imageView);
    }

    public void connectGates(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1 + 15, y1 + 15, x2 + 15, y2 + 15);
        line.setStroke(Color.BLUE);
        this.getChildren().add(line);
    }

    public void removeGate(ImageView gate) {
        this.getChildren().remove(gate);
    }

    public void removeConnection(Line connection) {
        this.getChildren().remove(connection);
    }
}
