package com.paperturtle.utils;

import java.util.Objects;

import javafx.scene.input.KeyCode;

public class KeyCombination {
    private final KeyCode keyCode;
    private final boolean controlDown;

    public KeyCombination(KeyCode keyCode, boolean controlDown) {
        this.keyCode = keyCode;
        this.controlDown = controlDown;
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }

    public boolean isControlDown() {
        return controlDown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KeyCombination that = (KeyCombination) o;
        return controlDown == that.controlDown && keyCode == that.keyCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCode, controlDown);
    }
}
