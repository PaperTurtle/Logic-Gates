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
    /**
     * The unique ID of the clipboard data.
     */
    public String id;

    /**
     * The type of the clipboard data.
     */
    public String type;

    /**
     * The position of the clipboard data on the canvas.
     */
    public Point2D position;

    /**
     * The state of the clipboard data (true for on, false for off).
     */
    public boolean state;

    /**
     * The list of input connections associated with the clipboard data.
     */
    public List<ConnectionData> inputs = new ArrayList<>();

    /**
     * The list of output connections associated with the clipboard data.
     */
    public List<ConnectionData> outputs = new ArrayList<>();

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
