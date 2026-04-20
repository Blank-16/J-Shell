package com.devops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class AdvancedFileCommands {

    private AdvancedFileCommands() {}

    public static final class CpCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            boolean recursive = args.length > 1 && args[1].equals("-r");
            int srcIdx  = recursive ? 2 : 1;
            int destIdx = recursive ? 3 : 2;

            if (args.length < destIdx + 1) {
                System.err.println("usage: " + usage());
                return 1;
            }

            File source = new File(context.currentDirectory(), args[srcIdx]);
            File dest   = new File(context.currentDirectory(), args[destIdx]);

            if (!source.exists()) {
                System.err.println("cp: '" + args[srcIdx] + "': No such file or directory");
                return 1;
            }
            if (source.isDirectory() && !recursive) {
                System.err.println("cp: '" + args[srcIdx] + "' is a directory (use -r)");
                return 1;
            }

            try {
                if (source.isDirectory()) {
                    copyDirectory(source, dest.isDirectory() ? new File(dest, source.getName()) : dest);
                } else {
                    File finalDest = dest.isDirectory() ? new File(dest, source.getName()) : dest;
                    Files.copy(source.toPath(), finalDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                System.err.println("cp: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        private void copyDirectory(File src, File dest) throws IOException {
            dest.mkdirs();
            File[] files = src.listFiles();
            if (files == null) return;
            for (File file : files) {
                File destChild = new File(dest, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destChild);
                } else {
                    Files.copy(file.toPath(), destChild.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        @Override public String name()  { return "cp"; }
        @Override public String usage() { return "cp [-r] <source> <destination>"; }
    }

    public static final class MvCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 3) {
                System.err.println("usage: " + usage());
                return 1;
            }

            File source = new File(context.currentDirectory(), args[1]);
            File dest   = new File(context.currentDirectory(), args[2]);

            if (!source.exists()) {
                System.err.println("mv: '" + args[1] + "': No such file or directory");
                return 1;
            }

            try {
                File finalDest = dest.isDirectory() ? new File(dest, source.getName()) : dest;
                if (source.getCanonicalPath().equals(finalDest.getCanonicalPath())) {
                    System.err.println("mv: '" + args[1] + "' and '" + args[2] + "' are the same file");
                    return 1;
                }
                Files.move(source.toPath(), finalDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("mv: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        @Override public String name()  { return "mv"; }
        @Override public String usage() { return "mv <source> <destination>"; }
    }
}
