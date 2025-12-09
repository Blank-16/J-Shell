package com.devops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TextCommands {

    // --- 'echo' Command: Print text to console ---
    public static class EchoCommand implements Command {

        @Override
        public void execute(String[] args) {
            for (int i = 1; i < args.length; i++) {
                System.out.print(args[i] + " ");
            }
            System.out.println();
        }
    }

    // --- 'grep' Command: Search text in file ---
    public static class GrepCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 3) {
                System.out.println("usage: grep <pattern> <filename>");
                return;
            }

            String pattern = args[1];
            String fileName = args[2];
            File file = new File(App.currentDirectory, fileName);

            if (!file.exists()) {
                System.out.println("grep: " + fileName + ": No such file");
                return;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    if (line.contains(pattern)) {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error processing file: " + e.getMessage());
            }
        }
    }

    // --- 'help' Command ---
    public static class HelpCommand implements Command {

        @Override
        public void execute(String[] args) {
            System.out.println("Available Commands:");
            System.out.println("  ls, pwd, cd, mkdir");
            System.out.println("  touch, rm, cat");
            System.out.println("  echo, grep, exit");
        }
    }
}
