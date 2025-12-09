package com.devops;

/**
 * Hello world!
 *
 */

import java.io.File;
import java.util.Scanner;

public class App {

    // Tracks the current directory (session state)
    // Starts at the directory where the Java program is run
    public static File currentDirectory = new File(System.getProperty("user.dir"));

    public static void main(String[] args) {
        // Initialize the registry and load commands
        CommandRegistry registry = new CommandRegistry();
        registerCommands(registry);

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Welcome to J-Shell! A lightweight Java-based Linux Emulator.");
            System.out.println("Type 'help' for a list of commands or 'exit' to quit.");
            System.out.println("---------------------------------------------------------");
            
            boolean running = true;
            
            while (running) {
                // Display prompt: /path/to/current/dir >
                System.out.print(currentDirectory.getAbsolutePath() + " > ");
                
                // Read input
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) continue;
                
                // Split input into command and arguments
                String[] parts = input.split("\\s+");
                String commandName = parts[0];
                
                if (commandName.equals("exit")) {
                    running = false;
                    System.out.println("Exiting J-Shell...");
                } else {
                    Command cmd = registry.getCommand(commandName);
                    if (cmd != null) {
                        try {
                            cmd.execute(parts);
                        } catch (Exception e) {
                            System.out.println("Error executing command: " + e.getMessage());
                        }
                    } else {
                        System.out.println("j-shell: command not found: " + commandName);
                    }
                }
            }
        }
    }

    private static void registerCommands(CommandRegistry registry) {
        // Navigation
        registry.register("ls", new FileSystemCommands.ListCommand());
        registry.register("pwd", new FileSystemCommands.PwdCommand());
        registry.register("cd", new FileSystemCommands.CdCommand());
        
        // File Manipulation
        registry.register("mkdir", new FileSystemCommands.MkdirCommand());
        registry.register("touch", new FileManipulationCommands.TouchCommand());
        registry.register("rm", new FileManipulationCommands.RmCommand());
        registry.register("cat", new FileManipulationCommands.CatCommand());
        
        // Text Utilities
        registry.register("echo", new TextCommands.EchoCommand());
        registry.register("grep", new TextCommands.GrepCommand());
        registry.register("help", new TextCommands.HelpCommand());
    }
}