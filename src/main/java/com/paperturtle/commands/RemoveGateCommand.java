package com.paperturtle.commands;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.components.LogicGate;

import javafx.geometry.Point2D;

public class RemoveGateCommand implements Command {
    private CircuitCanvas canvas;
    private LogicGate gate;
    private double x;
    private double y;

    public RemoveGateCommand(CircuitCanvas canvas, LogicGate gate) {
        this.canvas = canvas;
        this.gate = gate;
        Point2D position = gate.getPosition();
        this.x = position.getX();
        this.y = position.getY();
    }

    @Override
    public void execute() {
        canvas.getGateManager().removeGate(gate.getImageView());
    }

    @Override
    public void undo() {
        canvas.drawGate(gate, x, y);
    }
}
