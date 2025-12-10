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
                            // for debugging purposes...
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
        }
    }

    private static void registerCommands(CommandRegistry registry) {
        // ===== NAVIGATION COMMANDS =====
        registry.register("ls", new FileSystemCommands.ListCommand());
        registry.register("pwd", new FileSystemCommands.PwdCommand());
        registry.register("cd", new FileSystemCommands.CdCommand());

        // ===== FILE MANIPULATION COMMANDS =====
        registry.register("mkdir", new FileSystemCommands.MkdirCommand());
        registry.register("touch", new FileManipulationCommands.TouchCommand());
        registry.register("rm", new FileManipulationCommands.RmCommand());
        registry.register("cat", new FileManipulationCommands.CatCommand());

        // ===== TEXT UTILITIES COMMANDS =====
        registry.register("echo", new TextCommands.EchoCommand());
        registry.register("grep", new TextCommands.GrepCommand());
        registry.register("help", new TextCommands.HelpCommand());

        // ===== ADVANCED FILE OPERATIONS =====
        registry.register("cp", new AdvancedFileCommands.CpCommand());
        registry.register("mv", new AdvancedFileCommands.MvCommand());

        // ===== SYSTEM INFORMATION COMMANDS =====
        registry.register("history", new SystemCommands.HistoryCommand());
        registry.register("whoami", new SystemCommands.WhoamiCommand());
        registry.register("date", new SystemCommands.DateCommand());
        registry.register("clear", new SystemCommands.ClearCommand());

        // ===== SEARCH COMMANDS =====
        registry.register("find", new SearchCommands.FindCommand());
        registry.register("wc", new SearchCommands.WcCommand());
        registry.register("diff", new SearchCommands.DiffCommand());

        // ===== COMPRESSION COMMANDS =====
        registry.register("zip", new CompressionCommands.ZipCommand());
        registry.register("unzip", new CompressionCommands.UnzipCommand());
        registry.register("gzip", new CompressionCommands.GzipCommand());
        registry.register("gunzip", new CompressionCommands.GunzipCommand());

        // ===== NETWORK COMMANDS =====
        registry.register("ping", new NetworkCommands.PingCommand());
        registry.register("wget", new NetworkCommands.WgetCommand());
        registry.register("curl", new NetworkCommands.CurlCommand());
        registry.register("ifconfig", new NetworkCommands.IfconfigCommand());

        // ===== PROCESS COMMANDS =====
        registry.register("ps", new ProcessCommands.PsCommand());
        registry.register("exec", new ProcessCommands.ExecCommand());
        registry.register("env", new ProcessCommands.EnvCommand());
        registry.register("uname", new ProcessCommands.UnameCommand());

        // ===== UTILITY COMMANDS =====
        registry.register("sort", new UtilityCommands.SortCommand());
        registry.register("uniq", new UtilityCommands.UniqCommand());
        registry.register("checksum", new UtilityCommands.ChecksumCommand());
        registry.register("du", new UtilityCommands.DuCommand());
        registry.register("head", new UtilityCommands.HeadCommand());
        registry.register("tail", new UtilityCommands.TailCommand());
    }
}
