package com.devops;

/**
 * Contract for all shell commands.
 * args[0] is always the command name; args[1..] are the operands.
 * Returns 0 on success, non-zero on failure (mirrors POSIX convention).
 */
public interface Command {
    int execute(ShellContext context, String[] args);

    default String name() {
        return "";
    }

    default String usage() {
        return "";
    }
}
