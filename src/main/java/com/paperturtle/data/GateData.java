package com.paperturtle.data;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.paperturtle.utils.CircuitComponent;

public class GateData implements CircuitComponent {
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

    @Override
    public String getComponentType() {
        return "gate";
    }
}
