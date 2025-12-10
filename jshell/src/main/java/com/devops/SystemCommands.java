package com.devops;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SystemCommands {

    // --- 'history' Command: Show session history ---
    public static class HistoryCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (App.commandHistory.isEmpty()) {
                System.out.println("No command history.");
                return;
            }

            int index = 1;
            for (String cmd : App.commandHistory) {
                System.out.printf("%5d  %s%n", index++, cmd);
            }
        }
    }

    // --- 'whoami' Command: Show current user ---
    public static class WhoamiCommand implements Command {

        @Override
        public void execute(String[] args) {
            String username = System.getProperty("user.name");
            if (username != null) {
                System.out.println(username);
            } else {
                System.out.println("Unknown user");
            }
        }
    }

    // --- 'date' Command: Show current system date/time ---
    public static class DateCommand implements Command {

        @Override
        public void execute(String[] args) {
            ZonedDateTime now = ZonedDateTime.now();
            System.out.println(now.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            // Or simply: System.out.println(now);
        }
    }

    // --- 'clear' Command: Clear console ---
    public static class ClearCommand implements Command {

        @Override
        public void execute(String[] args) {
            // ANSI escape codes work in most modern terminals
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }
}
