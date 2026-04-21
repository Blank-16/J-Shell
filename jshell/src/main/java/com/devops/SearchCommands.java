package com.devops;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class SearchCommands {

    private SearchCommands() {}

    public static final class FindCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 2) {
                System.err.println("usage: " + usage());
                return 1;
            }

            // Argument parsing: find <pattern> [-r] OR find <dir> -name <pattern>
            File startDir = context.currentDirectory();
            String pattern;
            boolean recursive = false;

            if (args.length >= 4 && args[2].equals("-name")) {
                startDir  = new File(context.currentDirectory(), args[1]);
                pattern   = args[3];
                recursive = true;
            } else if (args.length == 3 && args[2].equals("-r")) {
                pattern   = args[1];
                recursive = true;
            } else {
                pattern = args[1];
            }

            if (!startDir.isDirectory()) {
                System.err.println("find: '" + startDir.getName() + "': No such directory");
                return 1;
            }

            var count = new AtomicInteger(0);
            findFiles(startDir, pattern, recursive, count);
            System.out.println(count.get() + " match(es) found.");
            return 0;
        }

        private void findFiles(File dir, String pattern, boolean recursive, AtomicInteger count) {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (file.getName().contains(pattern)) {
                    System.out.println(file.getAbsolutePath());
                    count.incrementAndGet();
                }
                if (file.isDirectory() && recursive) {
                    findFiles(file, pattern, true, count);
                }
            }
        }

        @Override public String name()  { return "find"; }
        @Override public String usage() { return "find <pattern> [-r] | find <dir> -name <pattern>"; }
    }

    public static final class WcCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 2) {
                System.err.println("usage: " + usage());
                return 1;
            }

            boolean linesOnly = false;
            boolean wordsOnly = false;
            boolean charsOnly = false;

            int fileArgIndex = 1;
            if (args[1].equals("-l")) { linesOnly = true; fileArgIndex = 2; }
            else if (args[1].equals("-w")) { wordsOnly = true; fileArgIndex = 2; }
            else if (args[1].equals("-c")) { charsOnly = true; fileArgIndex = 2; }

            if (fileArgIndex >= args.length) {
                System.err.println("usage: " + usage());
                return 1;
            }

            String fileName = args[fileArgIndex];
            File file = new File(context.currentDirectory(), fileName);
            if (!file.exists()) {
                System.err.println("wc: " + fileName + ": No such file");
                return 1;
            }

            long lines = 0, words = 0, chars = 0;
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines++;
                    chars += line.length();
                    if (!line.isBlank()) {
                        words += line.trim().split("\\s+").length;
                    }
                }
            } catch (IOException e) {
                System.err.println("wc: " + e.getMessage());
                return 1;
            }

            if (linesOnly)      System.out.printf("%7d %s%n", lines, fileName);
            else if (wordsOnly) System.out.printf("%7d %s%n", words, fileName);
            else if (charsOnly) System.out.printf("%7d %s%n", chars, fileName);
            else                System.out.printf("%7d %7d %7d %s%n", lines, words, chars, fileName);

            return 0;
        }

        @Override public String name()  { return "wc"; }
        @Override public String usage() { return "wc [-l|-w|-c] <file>"; }
    }

    public static final class DiffCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 3) {
                System.err.println("usage: " + usage());
                return 1;
            }

            File file1 = new File(context.currentDirectory(), args[1]);
            File file2 = new File(context.currentDirectory(), args[2]);

            if (!file1.exists()) { System.err.println("diff: '" + args[1] + "': No such file"); return 1; }
            if (!file2.exists()) { System.err.println("diff: '" + args[2] + "': No such file"); return 1; }

            try {
                List<String> lines1 = Files.readAllLines(file1.toPath());
                List<String> lines2 = Files.readAllLines(file2.toPath());

                // Myers-style output: mark each differing line position
                boolean identical = true;
                int max = Math.max(lines1.size(), lines2.size());
                for (int i = 0; i < max; i++) {
                    String l1 = i < lines1.size() ? lines1.get(i) : null;
                    String l2 = i < lines2.size() ? lines2.get(i) : null;

                    if (l1 == null) {
                        System.out.printf("> %d: %s%n", i + 1, l2);
                        identical = false;
                    } else if (l2 == null) {
                        System.out.printf("< %d: %s%n", i + 1, l1);
                        identical = false;
                    } else if (!l1.equals(l2)) {
                        System.out.printf("< %d: %s%n", i + 1, l1);
                        System.out.printf("> %d: %s%n", i + 1, l2);
                        identical = false;
                    }
                }

                if (identical) System.out.println("Files are identical.");
                return identical ? 0 : 1;
            } catch (IOException e) {
                System.err.println("diff: " + e.getMessage());
                return 1;
            }
        }

        @Override public String name()  { return "diff"; }
        @Override public String usage() { return "diff <file1> <file2>"; }
    }
}
