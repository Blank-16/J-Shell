package com.devops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileManipulationCommands {

    // --- 'touch' Command: Create empty file ---
    public static class TouchCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: touch <filename>");
                return;
            }
            File file = new File(App.currentDirectory, args[1]);
            try {
                if (file.createNewFile()) {
                    System.out.println("File created: " + file.getName());
                } else {
                    file.setLastModified(System.currentTimeMillis());
                }
            } catch (IOException e) {
                System.out.println("Error creating file: " + e.getMessage());
            }
        }
    }

    // --- 'rm' Command: Remove file ---
    public static class RmCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: rm <filename>");
                return;
            }
            File file = new File(App.currentDirectory, args[1]);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Removed " + args[1]);
                } else {
                    System.out.println("Error: Could not delete " + args[1]);
                }
            } else {
                System.out.println("rm: cannot remove '" + args[1] + "': No such file");
            }
        }
    }

    // --- 'cat' Command: Concatenate/View file ---
    public static class CatCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: cat <filename>");
                return;
            }
            File file = new File(App.currentDirectory, args[1]);
            if (!file.exists()) {
                System.out.println("cat: " + args[1] + ": No such file");
                return;
            }
            try {
                // Java NIO for easy reading
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }
}
