package com.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class CircuitCanvas extends Canvas {
    private GraphicsContext gc;

    public CircuitCanvas(double width, double height) {
        super(width, height);
        initialize();
    }

    private void initialize() {
        gc = this.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawGate(LogicGate gate, double x, double y) {
        // Assume all gates are drawn as simple circles for now
        gc.setFill(Color.BLACK);
        gc.strokeOval(x, y, 30, 30);
        gc.fillText(gate.getClass().getSimpleName(), x, y + 45);
    }

    public void connectGates(LogicGate output, LogicGate input, double x1, double y1, double x2, double y2) {
        gc.setStroke(Color.BLUE);
        gc.strokeLine(x1 + 15, y1 + 15, x2 + 15, y2 + 15);
    }

    public void removeGate(double x, double y) {
        gc.clearRect(x, y, 30, 30);
    }

    public void removeConnection(double x1, double y1, double x2, double y2) {
        gc.clearRect(x1 + 15, y1 + 15, x2 + 15 - x1, y2 + 15 - y1);
    }
}
