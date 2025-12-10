package com.devops;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TextCommands {

    // UPDATED 'echo' Command: Supports > and >> redirection ---
    public static class EchoCommand implements Command {

        @Override
        @SuppressWarnings("ConvertToStringSwitch")
        public void execute(String[] args) {
            StringBuilder output = new StringBuilder();
            String targetFile = null;
            boolean append = false;
            boolean redirect = false;

            for (int i = 1; i < args.length; i++) {
                if (args[i].equals(">")) {
                    redirect = true;
                    append = false;
                    if (i + 1 < args.length) {
                        targetFile = args[i + 1];
                    } else {
                        System.out.println("Error: No filename specified after >");
                        return;
                    }
                    break;
                } else if (args[i].equals(">>")) {
                    redirect = true;
                    append = true;
                    if (i + 1 < args.length) {
                        targetFile = args[i + 1];
                    } else {
                        System.out.println("Error: No filename specified after >>");
                        return;
                    }
                    break;
                } else {
                    if (output.length() > 0) {
                        output.append(" ");
                    }
                    output.append(args[i]);
                }
            }

            String textToWrite = output.toString();

            if (redirect) {
                if (targetFile != null) {
                    File file = new File(App.currentDirectory, targetFile);
                    try (FileWriter writer = new FileWriter(file, append)) {
                        writer.write(textToWrite + System.lineSeparator());
                        System.out.println((append ? "Appended to " : "Overwrote ") + targetFile);
                    } catch (IOException e) {
                        System.out.println("Error writing to file: " + e.getMessage());
                    }
                }
            } else {
                System.out.println(textToWrite);
            }
        }
    }

    // --- 'grep' Command (Unchanged) ---
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

    // --- 'help' Command (Unchanged) ---
    public static class HelpCommand implements Command {

        @Override
        public void execute(String[] args) {
            System.out.println("Available Commands:");
            System.out.println("  ls, pwd, cd, mkdir");
            System.out.println("  touch, rm, cat, cp, mv");
            System.out.println("  echo (supports > and >>), grep");
            System.out.println("  history, whoami, date, clear, exit");
        }
    }
}
