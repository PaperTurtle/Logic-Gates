package com.paperturtle;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class ClipboardData {
    String type;
    Point2D position;
    List<ConnectionDetail> connections;

    public static class ConnectionDetail {
        public String sourceId;
        public String targetId;
        public int sourcePointIndex;
        public int targetPointIndex;

        public ConnectionDetail(String sourceId, String targetId, int sourcePointIndex, int targetPointIndex) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.sourcePointIndex = sourcePointIndex;
            this.targetPointIndex = targetPointIndex;
        }

    }

    public ClipboardData(String type, Point2D position) {
        this.type = type;
        this.position = position;
        this.connections = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public Point2D getPosition() {
        return position;
    }

    public List<ConnectionDetail> getConnections() {
        return connections;
    }
}
