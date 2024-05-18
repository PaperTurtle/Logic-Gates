package com.paperturtle.utils;

import java.util.Objects;

import javafx.scene.input.KeyCode;

/**
 * Represents a key combination that includes a specific key code and an
 * optional control key modifier.
 * This class is used to define keyboard shortcuts and handle key events.
 * 
 * @author Seweryn Czabanowski
 */
public class KeyCombination {
    /**
     * The key code that represents the key in this key combination.
     */
    private final KeyCode keyCode;

    /**
     * A flag indicating whether the control key must be down for this key
     * combination.
     */
    private final boolean controlDown;

    /**
     * Constructs a KeyCombination with the specified key code and control key
     * modifier.
     * 
     * @param keyCode     the key code of the key combination
     * @param controlDown true if the control key is pressed, false otherwise
     */
    public KeyCombination(KeyCode keyCode, boolean controlDown) {
        this.keyCode = keyCode;
        this.controlDown = controlDown;
    }

    /**
     * Gets the key code of the key combination.
     * 
     * @return the key code of the key combination
     */
    public KeyCode getKeyCode() {
        return keyCode;
    }

    /**
     * Checks if the control key is pressed in the key combination.
     * 
     * @return true if the control key is pressed, false otherwise
     */
    public boolean isControlDown() {
        return controlDown;
    }

    /**
     * Checks if this KeyCombination is equal to another object.
     * 
     * @param o the object to compare with
     * @return true if this KeyCombination is equal to the specified object, false
     *         otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KeyCombination that = (KeyCombination) o;
        return controlDown == that.controlDown && keyCode == that.keyCode;
    }

    /**
     * Returns the hash code value for this KeyCombination.
     * 
     * @return the hash code value for this KeyCombination
     */
    @Override
    public int hashCode() {
        return Objects.hash(keyCode, controlDown);
    }
}
