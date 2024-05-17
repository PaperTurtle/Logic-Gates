package com.paperturtle;

import javafx.scene.shape.Line;

public class RemoveConnectionCommand implements Command {
    private CircuitCanvas canvas;
    private LogicGate sourceGate;
    private LogicGate targetGate;
    private Line connectionLine;
    private int targetInputIndex;

    public RemoveConnectionCommand(CircuitCanvas canvas, LogicGate sourceGate, LogicGate targetGate,
            Line connectionLine, int targetInputIndex) {
        this.canvas = canvas;
        this.sourceGate = sourceGate;
        this.targetGate = targetGate;
        this.connectionLine = connectionLine;
        this.targetInputIndex = targetInputIndex;
    }

    @Override
    public void execute() {
        sourceGate.removeOutputConnection(connectionLine);
        targetGate.removeInputConnection(connectionLine, targetInputIndex);
        targetGate.removeInput(sourceGate);

        canvas.getChildren().remove(connectionLine);
        canvas.getLineToStartGateMap().remove(connectionLine);

        targetGate.evaluate();
        targetGate.propagateStateChange();
        sourceGate.setMaxOutputConnections(sourceGate.getMaxOutputConnections() + 1);

        canvas.scheduleUpdate(targetGate);
        canvas.scheduleUpdate(sourceGate);
    }

    @Override
    public void undo() {
        sourceGate.addOutputConnection(connectionLine);
        sourceGate.addOutputGate(targetGate);
        targetGate.addInputConnection(connectionLine, targetInputIndex);
        targetGate.addInput(sourceGate);

        canvas.getChildren().add(connectionLine);
        canvas.getLineToStartGateMap().put(connectionLine, sourceGate);

        targetGate.evaluate();
        targetGate.propagateStateChange();
        sourceGate.updateOutputConnectionsColor(sourceGate.evaluate());

        targetGate.getInputMarkers().forEach(marker -> marker.toFront());
        sourceGate.getOutputMarker().toFront();

        canvas.scheduleUpdate(targetGate);
    }
}
