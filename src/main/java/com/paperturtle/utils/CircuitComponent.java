package com.paperturtle.utils;

/**
 * The CircuitComponent interface is implemented by classes that represent
 * components in a digital circuit (e.g. Gates or Labels).
 */
public interface CircuitComponent {
    /**
     * Gets the component type.
     * 
     * @return the component type as a string
     */
    String getComponentType();
}