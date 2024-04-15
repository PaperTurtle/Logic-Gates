package com.example;

import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class CircuitCanvas extends Pane {
    public CircuitCanvas(double width, double height) {
        super();
        this.setPrefSize(width, height);
        this.setStyle("-fx-background-color: white;");

        this.setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasImage()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // Handling drops
        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasImage()) {
                ImageView imageView = new ImageView(db.getImage());
                double offsetX = imageView.getBoundsInLocal().getWidth() / 2;
                double offsetY = imageView.getBoundsInLocal().getHeight() / 2;
                imageView.setX(event.getX() - offsetX);
                imageView.setY(event.getY() - offsetY);
                setupDraggableGate(imageView);
                this.getChildren().add(imageView);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupDraggableGate(ImageView imageView) {
        imageView.setOnMousePressed(event -> {
            imageView.toFront();
        });

        imageView.setOnDragDetected(event -> {
            Dragboard db = imageView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putImage(imageView.getImage());
            db.setContent(content);
            db.setDragView(imageView.getImage(), imageView.getBoundsInLocal().getWidth() / 2,
                    imageView.getBoundsInLocal().getHeight() / 2);
            event.consume();
        });

        imageView.setOnMouseReleased(event -> {
        });
    }

    public void drawGate(LogicGate gate, double x, double y) {
        Image image = SvgUtil.loadSvgImage(gate.getSvgFilePath());
        ImageView imageView = new ImageView(image);
        imageView.setX(x);
        imageView.setY(y);
        setupDraggableGate(imageView);
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
