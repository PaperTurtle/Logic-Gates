package com.paperturtle;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class ClipboardData {
    String type;
    Point2D position;

    public ClipboardData(String type, Point2D position) {
        this.type = type;
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public Point2D getPosition() {
        return position;
    }
}
