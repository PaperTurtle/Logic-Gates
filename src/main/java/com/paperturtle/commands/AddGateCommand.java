package com.paperturtle.commands;

import com.paperturtle.CircuitCanvas;
import com.paperturtle.components.LogicGate;

public class AddGateCommand implements Command {
    private CircuitCanvas canvas;
    private LogicGate gate;
    private double x;
    private double y;

    public AddGateCommand(CircuitCanvas canvas, LogicGate gate, double x, double y) {
        this.canvas = canvas;
        this.gate = gate;
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute() {
        canvas.drawGate(gate, x, y);
    }

    @Override
    public void undo() {
        canvas.getGateManager().removeGate(gate.getImageView());
    }
}
