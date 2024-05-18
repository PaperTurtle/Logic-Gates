package com.paperturtle.data;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.paperturtle.utils.CircuitComponent;

/**
 * Represents the data for a logic gate including its connections.
 * Implements the CircuitComponent interface.
 * 
 * @see CircuitComponent
 * 
 * @author Seweryn Czabanowski
 */
public class GateData implements CircuitComponent {
    public String id;
    public String type;
    public Point2D position;
    public boolean state;
    public List<ConnectionData> inputs = new ArrayList<>();
    public List<ConnectionData> outputs = new ArrayList<>();

    /**
     * Represents a connection data between gates.
     * Stores the ID of the gate and the index of the connection point.
     */
    public static class ConnectionData {
        public String gateId;
        public int pointIndex;

        /**
         * Constructs a ConnectionData with the specified gate ID and point index.
         * 
         * @param gateId     the ID of the connected gate
         * @param pointIndex the index of the connection point
         */
        public ConnectionData(String gateId, int pointIndex) {
            this.gateId = gateId;
            this.pointIndex = pointIndex;
        }
    }

    /**
     * Gets the component type.
     * 
     * @return the component type as a string
     */
    @Override
    public String getComponentType() {
        return "gate";
    }
}
