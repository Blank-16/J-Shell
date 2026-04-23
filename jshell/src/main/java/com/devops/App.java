package com.devops;

import java.io.File;
import java.util.Scanner;

public final class App {

    public static void main(String[] args) {
        var context = new ShellContext(new File(System.getProperty("user.dir")));
        var registry = new CommandRegistry();
        registerCommands(registry);

        System.out.println("Welcome to J-Shell — type 'help' or 'exit'.");
        System.out.println();

        try (var scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(context.currentDirectory().getAbsolutePath() + " > ");

                if (!scanner.hasNextLine()) break;

                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                context.addHistory(input);

                String[] parts = Tokenizer.tokenize(input);
                String commandName = parts[0];

                if (commandName.equals("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                registry.find(commandName).ifPresentOrElse(
                    cmd -> {
                        try {
                            cmd.execute(context, parts);
                        } catch (Exception e) {
                            System.err.println(commandName + ": unexpected error: " + e.getMessage());
                        }
                    },
                    () -> System.err.println("j-shell: command not found: " + commandName)
                );
            }
        }
    }

    private static void registerCommands(CommandRegistry registry) {
        registry.register("ls",       new FileSystemCommands.ListCommand());
        registry.register("pwd",      new FileSystemCommands.PwdCommand());
        registry.register("cd",       new FileSystemCommands.CdCommand());
        registry.register("mkdir",    new FileSystemCommands.MkdirCommand());

        registry.register("touch",    new FileManipulationCommands.TouchCommand());
        registry.register("rm",       new FileManipulationCommands.RmCommand());
        registry.register("cat",      new FileManipulationCommands.CatCommand());

        registry.register("echo",     new TextCommands.EchoCommand());
        registry.register("grep",     new TextCommands.GrepCommand());
        registry.register("help",     new TextCommands.HelpCommand(registry));

        registry.register("cp",       new AdvancedFileCommands.CpCommand());
        registry.register("mv",       new AdvancedFileCommands.MvCommand());

        registry.register("history",  new SystemCommands.HistoryCommand());
        registry.register("whoami",   new SystemCommands.WhoamiCommand());
        registry.register("date",     new SystemCommands.DateCommand());
        registry.register("clear",    new SystemCommands.ClearCommand());

        registry.register("find",     new SearchCommands.FindCommand());
        registry.register("wc",       new SearchCommands.WcCommand());
        registry.register("diff",     new SearchCommands.DiffCommand());

        registry.register("zip",      new CompressionCommands.ZipCommand());
        registry.register("unzip",    new CompressionCommands.UnzipCommand());
        registry.register("gzip",     new CompressionCommands.GzipCommand());
        registry.register("gunzip",   new CompressionCommands.GunzipCommand());

        registry.register("ping",     new NetworkCommands.PingCommand());
        registry.register("wget",     new NetworkCommands.WgetCommand());
        registry.register("curl",     new NetworkCommands.CurlCommand());
        registry.register("ifconfig", new NetworkCommands.IfconfigCommand());

        registry.register("ps",       new ProcessCommands.PsCommand());
        registry.register("exec",     new ProcessCommands.ExecCommand());
        registry.register("env",      new ProcessCommands.EnvCommand());
        registry.register("uname",    new ProcessCommands.UnameCommand());

        registry.register("sort",     new UtilityCommands.SortCommand());
        registry.register("uniq",     new UtilityCommands.UniqCommand());
        registry.register("checksum", new UtilityCommands.ChecksumCommand());
        registry.register("du",       new UtilityCommands.DuCommand());
        registry.register("head",     new UtilityCommands.HeadCommand());
        registry.register("tail",     new UtilityCommands.TailCommand());
    }
}
