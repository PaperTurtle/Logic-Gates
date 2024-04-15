package com.example;

public class GateFactory {
    /**
     * Creates a logic gate instance based on the specified type.
     * 
     * @param type The type of gate to create ("AND", "OR", "NOT", etc.).
     * @return An instance of the specified LogicGate, or null if the type is not
     *         recognized.
     */
    public static LogicGate createGate(String type) {
        switch (type.toUpperCase()) {
            case "AND":
                return new AndGate();
            case "OR":
                return new OrGate();
            case "NOT":
                return new NotGate();
            case "NAND":
                return new NandGate();
            case "NOR":
                return new NorGate();
            case "XOR":
                return new XorGate();
            case "XNOR":
                return new XnorGate();
            default:
                return null;
        }
    }
}
