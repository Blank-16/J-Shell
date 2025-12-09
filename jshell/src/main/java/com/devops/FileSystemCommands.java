package com.devops;

import java.io.File;

public class FileSystemCommands {

    // --- 'ls' Command: List files in current directory ---
    public static class ListCommand implements Command {

        @Override
        public void execute(String[] args) {
            File[] files = App.currentDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String type = file.isDirectory() ? "DIR" : "FILE";
                    long size = file.length();
                    System.out.printf("[%s] %-8s %s%n", type, size + "B", file.getName());
                }
            }
        }
    }

    // --- 'pwd' Command: Print Working Directory ---
    public static class PwdCommand implements Command {

        @Override
        public void execute(String[] args) {
            System.out.println(App.currentDirectory.getAbsolutePath());
        }
    }

    // --- 'cd' Command: Change Directory ---
    public static class CdCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                // If no argument, go to user home
                App.currentDirectory = new File(System.getProperty("user.home"));
                return;
            }

            String targetPath = args[1];
            File newDir;

            if (targetPath.equals("..")) {
                // Go up one level
                newDir = App.currentDirectory.getParentFile();
                if (newDir == null) {
                    newDir = App.currentDirectory; // At root

                }
            } else {
                // Check if path is absolute or relative
                File potentialDir = new File(targetPath);
                if (potentialDir.isAbsolute()) {
                    newDir = potentialDir;
                } else {
                    newDir = new File(App.currentDirectory, targetPath);
                }
            }

            if (newDir.exists() && newDir.isDirectory()) {
                App.currentDirectory = newDir;
            } else {
                System.out.println("cd: " + targetPath + ": No such directory");
            }
        }
    }

    // --- 'mkdir' Command: Make Directory ---
    public static class MkdirCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: mkdir <directory_name>");
                return;
            }
            File newDir = new File(App.currentDirectory, args[1]);
            if (newDir.mkdir()) {
                System.out.println("Directory created: " + newDir.getName());
            } else {
                System.out.println("mkdir: cannot create directory (may already exist).");
            }
        }
    }
}
