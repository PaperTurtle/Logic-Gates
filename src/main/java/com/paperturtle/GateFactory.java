package com.paperturtle;

/**
 * Factory class to create instances of LogicGate.
 * 
 * This class is used to create instances of different types of logic gates,
 * such as AND, OR, NOT, etc.
 * 
 * @see LogicGate
 * @author Seweryn Czabanowski
 */
public class GateFactory {
    /**
     * Creates a LogicGate instance based on the specified type.
     * 
     * This method takes a string representing the type of logic gate to create. The
     * type is case-insensitive.
     * Possible values for type are: "AND", "OR", "NOT", "Buffer", "NAND", "NOR",
     * "XOR", "XNOR".
     * 
     * @param type The type of gate to create. This should be one of the following:
     *             "AND", "OR", "NOT", "Buffer", "NAND", "NOR", "XOR", "XNOR".
     * @return An instance of the specified LogicGate. If the type is not
     *         recognized, this method returns null.
     */
    public static LogicGate createGate(String type) {
        switch (type.toUpperCase()) {
            case "AND":
                return new AndGate();
            case "OR":
                return new OrGate();
            case "NOT":
                return new NotGate();
            case "BUFFER":
                return new BufferGate();
            case "NAND":
                return new NandGate();
            case "NOR":
                return new NorGate();
            case "XOR":
                return new XorGate();
            case "XNOR":
                return new XnorGate();
            case "TRISTATE":
                return new TriStateGate();
            case "SWITCH":
                return new SwitchGate();
            case "CLOCK":
                return new ClockGate();
            case "HIGHCONSTANT":
                return new HighConstantGate();
            case "LOWCONSTANT":
                return new LowConstantGate();
            case "LIGHTBULB":
                return new Lightbulb();
            case "FOURBITDIGIT":
                return new FourBitDigitGate();
            case "ASYNC_RS_FLIPFLOP":
                return new AsynchronousRSFlipFlop();
            case "SYNC_RS_FLIPFLOP":
                return new SyncRSFlipFlop();
            case "EDGE_JK_FLIPFLOP":
                return new EdgeJKFlipFlop();
            case "SYNC_T_FLIPFLOP":
                return new SyncTFlipFlop();
            case "EDGE_D_FLIPFLOP":
                return new EdgeDFlipFlop();
            case "LEVEL_D_FLIPFLOP":
                return new LevelDFlipFlop();

            default:
                return null;
        }
    }
}
