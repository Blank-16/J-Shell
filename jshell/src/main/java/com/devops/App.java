package com.devops;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    // Tracks the current directory (session state)
    // Starts at the directory where the Java program is run
    public static File currentDirectory = new File(System.getProperty("user.dir"));

    // Command history list accessible by SystemCommands
    public static List<String> commandHistory = new ArrayList<>();

    public static void main(String[] args) {
        // Initialize the registry and load commands
        CommandRegistry registry = new CommandRegistry();
        registerCommands(registry);

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println("  Welcome to J-Shell!                                   ");
            System.out.println("  A lightweight Java-based Linux Emulator               ");
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println("Type 'help' for a list of commands or 'exit' to quit.");
            System.out.println();

            boolean running = true;

            while (running) {
                // Display prompt: /path/to/current/dir >
                System.out.print(currentDirectory.getAbsolutePath() + " > ");

                // Read input
                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                // Save the command to history 
                commandHistory.add(input);

                // Split input into command and arguments
                String[] parts = input.split("\\s+");
                String commandName = parts[0];

                if (commandName.equals("exit")) {
                    running = false;
                    System.out.println("Exiting J-Shell... Goodbye!");
                } else {
                    Command cmd = registry.getCommand(commandName);
                    if (cmd != null) {
                        try {
                            cmd.execute(parts);
                        } catch (Exception e) {
                            System.out.println("Error executing command: " + e.getMessage());
                            // Optional: Print stack trace for debugging
                            // e.printStackTrace();
                        }
                    } else {
                        System.out.println("j-shell: command not found: " + commandName);
                        System.out.println("Type 'help' to see available commands.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Fatal error: " + e.getMessage());
            // e.printStackTrace();  // for debugging purposes
        }
    }

    private static void registerCommands(CommandRegistry registry) {
        // Navigation Commands
        registry.register("ls", new FileSystemCommands.ListCommand());
        registry.register("pwd", new FileSystemCommands.PwdCommand());
        registry.register("cd", new FileSystemCommands.CdCommand());

        // File Manipulation Commands
        registry.register("mkdir", new FileSystemCommands.MkdirCommand());
        registry.register("touch", new FileManipulationCommands.TouchCommand());
        registry.register("rm", new FileManipulationCommands.RmCommand());
        registry.register("cat", new FileManipulationCommands.CatCommand());

        // Text Utilities Commands
        registry.register("echo", new TextCommands.EchoCommand());
        registry.register("grep", new TextCommands.GrepCommand());
        registry.register("help", new TextCommands.HelpCommand());

        // Advanced File Operations
        registry.register("cp", new AdvancedFileCommands.CpCommand());
        registry.register("mv", new AdvancedFileCommands.MvCommand());

        // System Information Commands
        registry.register("history", new SystemCommands.HistoryCommand());
        registry.register("whoami", new SystemCommands.WhoamiCommand());
        registry.register("date", new SystemCommands.DateCommand());
        registry.register("clear", new SystemCommands.ClearCommand());
    }
}
