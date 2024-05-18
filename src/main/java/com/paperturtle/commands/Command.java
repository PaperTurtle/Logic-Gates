package com.paperturtle.commands;

public interface Command {
    void execute();

    void undo();
}
