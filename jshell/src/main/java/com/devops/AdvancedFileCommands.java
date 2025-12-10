package com.devops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AdvancedFileCommands {

    // --- 'cp' Command: Copy file ---
    public static class CpCommand implements Command {

        @Override
        public void execute(String[] args) {
            boolean recursive = false;
            int sourceIndex = 1;
            int destIndex = 2;

            if (args.length > 1 && args[1].equals("-r")) {
                recursive = true;
                sourceIndex = 2;
                destIndex = 3;
                if (args.length < 4) {
                    System.out.println("usage: cp -r <source> <destination>");
                    return;
                }
            } else if (args.length < 3) {
                System.out.println("usage: cp <source> <destination>");
                return;
            }

            File source = new File(App.currentDirectory, args[sourceIndex]);
            File dest = new File(App.currentDirectory, args[destIndex]);

            if (!source.exists()) {
                System.out.println("cp: cannot stat '" + args[sourceIndex] + "': No such file or directory");
                return;
            }

            if (source.isDirectory() && !recursive) {
                System.out.println("cp: " + args[sourceIndex] + " is a directory (use -r to copy)");
                return;
            }

            if (source.isDirectory() && recursive) {
                try {
                    copyDirectory(source, dest);
                    System.out.println("Copied directory '" + args[sourceIndex] + "' to '" + dest.getName() + "'");
                } catch (IOException e) {
                    System.out.println("cp: error copying directory: " + e.getMessage());
                }
                return;
            }

            try {
                if (dest.isDirectory()) {
                    dest = new File(dest, source.getName());
                }

                if (dest.exists()) {
                    System.out.println("Warning: Overwriting " + dest.getName());
                }

                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied '" + args[sourceIndex] + "' to '" + dest.getName() + "'");

            } catch (IOException e) {
                System.out.println("cp: error copying file: " + e.getMessage());
            }
        }

        private void copyDirectory(File source, File dest) throws IOException {
            if (!dest.exists()) {
                dest.mkdirs();
            }

            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    File destFile = new File(dest, file.getName());
                    if (file.isDirectory()) {
                        copyDirectory(file, destFile);
                    } else {
                        Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    // --- 'mv' Command: Move or Rename file ---
    public static class MvCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 3) {
                System.out.println("usage: mv <source> <destination>");
                return;
            }

            File source = new File(App.currentDirectory, args[1]);
            File dest = new File(App.currentDirectory, args[2]);

            if (!source.exists()) {
                System.out.println("mv: cannot stat '" + args[1] + "': No such file or directory");
                return;
            }

            try {
                if (dest.isDirectory()) {
                    dest = new File(dest, source.getName());
                }

                if (source.getCanonicalPath().equals(dest.getCanonicalPath())) {
                    System.out.println("mv: '" + args[1] + "' and '" + args[2] + "' are the same file");
                    return;
                }

                if (dest.exists()) {
                    System.out.println("Warning: Overwriting " + dest.getName());
                }

                Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Moved '" + args[1] + "' to '" + dest.getName() + "'");

            } catch (IOException e) {
                System.out.println("mv: error moving file: " + e.getMessage());
            }
        }
    }
}
