package com.devops;

public interface Command {

    // args[0] is the command name, args[1]... are parameters
    void execute(String[] args);
}
