package com.paperturtle;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class ClipboardData {
    public String id;
    public String type;
    public Point2D position;
    public boolean state;
    public List<ConnectionData> inputs = new ArrayList<>();
    public List<ConnectionData> outputs = new ArrayList<>();

    public static class ConnectionData {
        public String gateId;
        public int pointIndex;

        public ConnectionData(String gateId, int pointIndex) {
            this.gateId = gateId;
            this.pointIndex = pointIndex;
        }

    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Point2D getPosition() {
        return position;
    }

    public boolean getState() {
        return state;
    }

    public List<ConnectionData> getInputs() {
        return inputs;
    }

    public List<ConnectionData> getOutputs() {
        return outputs;
    }

}
