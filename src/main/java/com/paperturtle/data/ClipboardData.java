package com.paperturtle.data;

import java.util.ArrayList;
import java.util.List;

import com.paperturtle.commands.Command;
import com.paperturtle.commands.PasteGatesCommand;

import javafx.geometry.Point2D;

/**
 * Represents the clipboard data for logic gates and their connections.
 * Used for copying and pasting gates within the circuit canvas.
 * 
 * @see Command
 * @see ConnectionData
 * @see PasteGatesCommand
 * 
 * @author Seweryn Czabanowski
 */
public class ClipboardData {
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
     * Gets the ID of the gate.
     * 
     * @return the ID of the gate
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the type of the gate.
     * 
     * @return the type of the gate
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the position of the gate.
     * 
     * @return the position of the gate
     */
    public Point2D getPosition() {
        return position;
    }

    /**
     * Gets the state of the gate.
     * 
     * @return the state of the gate
     */
    public boolean getState() {
        return state;
    }

    /**
     * Gets the list of input connections.
     * 
     * @return the list of input connections
     */
    public List<ConnectionData> getInputs() {
        return inputs;
    }

    /**
     * Gets the list of output connections.
     * 
     * @return the list of output connections
     */
    public List<ConnectionData> getOutputs() {
        return outputs;
    }

}
