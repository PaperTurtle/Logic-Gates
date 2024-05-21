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
    /**
     * The unique ID of the gate data.
     */
    public String id;

    /**
     * The type of the gate data.
     */
    public String type;

    /**
     * The position of the gate data on the canvas.
     */
    public Point2D position;

    /**
     * The state of the gate data (true for on, false for off).
     */
    public boolean state;

    /**
     * The list of input connections associated with the gate data.
     */
    public List<ConnectionData> inputs = new ArrayList<>();

    /**
     * The list of output connections associated with the gate data.
     */
    public List<ConnectionData> outputs = new ArrayList<>();

    /**
     * The maximum number of output connections the gate can have.
     */
    public int maxOutputConnections;

    /**
     * Represents a connection data between gates.
     * Stores the ID of the gate and the index of the connection point.
     */
    public static class ConnectionData {
        /**
         * The ID of the connected gate.
         */
        public String gateId;

        /**
         * The index of the connection point.
         */
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
