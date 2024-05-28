package com.paperturtle.components;

import com.paperturtle.components.gates.AndGate;
import com.paperturtle.components.gates.BufferGate;
import com.paperturtle.components.gates.NandGate;
import com.paperturtle.components.gates.NorGate;
import com.paperturtle.components.gates.NotGate;
import com.paperturtle.components.gates.OrGate;
import com.paperturtle.components.gates.TriStateGate;
import com.paperturtle.components.gates.XnorGate;
import com.paperturtle.components.gates.XorGate;
import com.paperturtle.components.inputs.ClockGate;
import com.paperturtle.components.inputs.HighConstantGate;
import com.paperturtle.components.inputs.LowConstantGate;
import com.paperturtle.components.inputs.SwitchGate;
import com.paperturtle.components.outputs.FourBitDigitGate;
import com.paperturtle.components.outputs.Lightbulb;

/**
 * Factory class to create instances of LogicGate.
 * 
 * This class is used to create instances of different types of logic gates,
 * such as AND, OR, NOT, etc.
 * 
 * @see LogicGate
 * @see AndGate
 * @see OrGate
 * @see NotGate
 * @see BufferGate
 * @see NandGate
 * @see NorGate
 * @see XorGate
 * @see XnorGate
 * @see TriStateGate
 * @see SwitchGate
 * @see ClockGate
 * @see HighConstantGate
 * @see LowConstantGate
 * @see Lightbulb
 * @see FourBitDigitGate
 * 
 * @author Seweryn Czabanowski
 */
public class GateFactory {
    /**
     * Creates a LogicGate instance based on the specified type.
     * 
     * This method takes a string representing the type of logic gate to create. The
     * type is case-insensitive.
     * Possible values for type are: "AND", "OR", "NOT", "Buffer", "NAND", "NOR",
     * "XOR", "XNOR", "TRISTATE", "SWITCH", "CLOCK", "HIGHCONSTANT", "LOWCONSTANT",
     * "LIGHTBULB", "FOURBITDIGIT".
     * 
     * @param type The type of gate to create. This should be one of the following:
     *             "AND", "OR", "NOT", "Buffer", "NAND", "NOR",
     *             "XOR", "XNOR", "TRISTATE", "SWITCH", "CLOCK", "HIGHCONSTANT",
     *             "LOWCONSTANT", "LIGHTBULB", "FOURBITDIGIT".
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
            default:
                return null;
        }
    }
}
