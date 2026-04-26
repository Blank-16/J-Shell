package com.devops;

import java.io.File;
import java.io.IOException;

public final class FileSystemCommands {

    private FileSystemCommands() {}

    public static final class ListCommand implements Command {

        @Override
        public ShellContext execute(ShellContext context, String[] args) {
            File[] files = context.currentDirectory().listFiles();
            if (files == null) {
                System.err.println("ls: cannot read directory");
                return context;
            }
            for (File file : files) {
                String type = file.isDirectory() ? "DIR " : "FILE";
                System.out.printf("[%s] %-10s %s%n", type, file.length() + "B", file.getName());
            }
            return context;
        }

        @Override public String name()  { return "ls"; }
        @Override public String usage() { return "ls"; }
    }

    public static final class PwdCommand implements Command {

        @Override
        public ShellContext execute(ShellContext context, String[] args) {
            System.out.println(context.currentDirectory().getAbsolutePath());
            return context;
        }

        @Override public String name()  { return "pwd"; }
        @Override public String usage() { return "pwd"; }
    }

    public static final class CdCommand implements Command {

        @Override
        public ShellContext execute(ShellContext context, String[] args) {
            File target = switch (args.length) {
                case 1  -> new File(System.getProperty("user.home"));
                default -> resolveTarget(context.currentDirectory(), args[1]);
            };

            if (target == null || !target.exists() || !target.isDirectory()) {
                System.err.println("cd: " + (args.length > 1 ? args[1] : "") + ": No such directory");
                return context;
            }

            try {
                // withDirectory() returns a new context — no mutation
                return context.withDirectory(target.getCanonicalFile());
            } catch (IOException e) {
                System.err.println("cd: " + e.getMessage());
                return context;
            }
        }

        private File resolveTarget(File current, String path) {
            return switch (path) {
                case "~"  -> new File(System.getProperty("user.home"));
                case ".." -> {
                    File parent = current.getParentFile();
                    yield parent != null ? parent : current;
                }
                default -> {
                    File f = new File(path);
                    yield f.isAbsolute() ? f : new File(current, path);
                }
            };
        }

        @Override public String name()  { return "cd"; }
        @Override public String usage() { return "cd [directory]"; }
    }

    public static final class MkdirCommand implements Command {

        @Override
        public ShellContext execute(ShellContext context, String[] args) {
            if (args.length < 2) {
                System.err.println("usage: " + usage());
                return context;
            }
            boolean parents = args[1].equals("-p");
            String dirName  = parents ? (args.length > 2 ? args[2] : null) : args[1];

            if (dirName == null) {
                System.err.println("usage: " + usage());
                return context;
            }

            File dir = new File(context.currentDirectory(), dirName);
            boolean created = parents ? dir.mkdirs() : dir.mkdir();
            if (!created) {
                System.err.println("mkdir: cannot create '" + dirName + "': already exists or permission denied");
                return context;
            }
            System.out.println("Directory created: " + dir.getName());
            return context;
        }

        @Override public String name()  { return "mkdir"; }
        @Override public String usage() { return "mkdir [-p] <directory>"; }
    }
}
