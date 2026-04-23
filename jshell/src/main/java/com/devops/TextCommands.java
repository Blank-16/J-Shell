package com.devops;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public final class TextCommands {

    private TextCommands() {}

    public static final class EchoCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            StringBuilder output = new StringBuilder();
            String targetFile = null;
            boolean append = false;

            int i = 1;
            while (i < args.length) {
                String arg = args[i];
                if (arg.equals(">") || arg.equals(">>")) {
                    if (i + 1 >= args.length) {
                        System.err.println("echo: missing filename after " + arg);
                        return 1;
                    }
                    append = arg.equals(">>");
                    targetFile = args[i + 1];
                    break;
                }
                if (!output.isEmpty()) output.append(' ');
                output.append(arg);
                i++;
            }

            String text = output.toString();

            if (targetFile != null) {
                File file = new File(context.currentDirectory(), targetFile);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
                    writer.write(text);
                    writer.newLine();
                } catch (IOException e) {
                    System.err.println("echo: " + e.getMessage());
                    return 1;
                }
            } else {
                System.out.println(text);
            }
            return 0;
        }

        @Override public String name()  { return "echo"; }
        @Override public String usage() { return "echo <text> [> file] [>> file]"; }
    }

    public static final class GrepCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 3) {
                System.err.println("usage: " + usage());
                return 1;
            }

            boolean ignoreCase = args[1].equals("-i");
            String patternStr = ignoreCase ? args[2] : args[1];
            String fileName   = ignoreCase ? args[3] : args[2];

            Pattern pattern;
            try {
                pattern = ignoreCase
                    ? Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE)
                    : Pattern.compile(patternStr);
            } catch (PatternSyntaxException e) {
                System.err.println("grep: invalid pattern '" + patternStr + "': " + e.getDescription());
                return 1;
            }

            File file = new File(context.currentDirectory(), fileName);
            if (!file.exists()) {
                System.err.println("grep: " + fileName + ": No such file");
                return 1;
            }

            int matchCount = 0;
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (pattern.matcher(line).find()) {
                        System.out.println(line);
                        matchCount++;
                    }
                }
            } catch (IOException e) {
                System.err.println("grep: " + e.getMessage());
                return 1;
            }

            return matchCount > 0 ? 0 : 1;
        }

        @Override public String name()  { return "grep"; }
        @Override public String usage() { return "grep [-i] <pattern> <file>"; }
    }

    public static final class HelpCommand implements Command {

        private final CommandRegistry registry;

        public HelpCommand(CommandRegistry registry) {
            this.registry = registry;
        }

        @Override
        public int execute(ShellContext context, String[] args) {
            // Drive help from the registry — never drifts out of sync
            Map<String, String> usages = registry.all().entrySet().stream()
                .filter(e -> !e.getValue().usage().isEmpty())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().usage()
                ));

            System.out.println("Available commands:");
            System.out.println();
            usages.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  %-12s %s%n", e.getKey(), e.getValue()));
            System.out.println();
            System.out.println("Type 'exit' to quit.");
            return 0;
        }

        @Override public String name()  { return "help"; }
        @Override public String usage() { return "help"; }
    }
}
