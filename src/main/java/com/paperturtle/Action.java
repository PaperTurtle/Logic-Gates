package com.paperturtle;

import java.util.List;

import javafx.geometry.Point2D;

class Action {
    enum ActionType {
        ADD, MOVE, REMOVE
    }

    ActionType type;
    List<CircuitComponent> affectedComponents;
    List<Point2D> oldPositions;
    List<Point2D> newPositions;

    public Action(ActionType type, List<CircuitComponent> components) {
        this.type = type;
        this.affectedComponents = components;
        this.oldPositions = null;
        this.newPositions = null;
    }

    public Action(ActionType type, List<CircuitComponent> components, List<Point2D> oldPos, List<Point2D> newPos) {
        this.type = type;
        this.affectedComponents = components;
        this.oldPositions = oldPos;
        this.newPositions = newPos;
    }
}
