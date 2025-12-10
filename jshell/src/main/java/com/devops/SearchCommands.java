package com.devops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class SearchCommands {

    // Find files by name pattern
    public static class FindCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: find <pattern> [-r for recursive]");
                System.out.println("   or: find <directory> -name <pattern>");
                return;
            }

            boolean recursive = false;
            String pattern;
            File startDir = App.currentDirectory;

            // Check different argument patterns
            if (args.length == 2) {
                pattern = args[1];
            } else if (args.length == 3 && args[2].equals("-r")) {
                pattern = args[1];
                recursive = true;
            } else if (args.length >= 4 && args[2].equals("-name")) {
                startDir = new File(App.currentDirectory, args[1]);
                pattern = args[3];
                recursive = true;
            } else {
                pattern = args[1];
            }

            if (!startDir.exists() || !startDir.isDirectory()) {
                System.out.println("find: '" + args[1] + "': No such directory");
                return;
            }

            System.out.println("Searching for: " + pattern);
            int count = findFiles(startDir, pattern, recursive, 0);
            System.out.println("\nFound " + count + " match(es)");
        }

        private int findFiles(File directory, String pattern, boolean recursive, int count) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(pattern)) {
                        System.out.println(file.getAbsolutePath());
                        count++;
                    }
                    if (file.isDirectory() && recursive) {
                        count = findFiles(file, pattern, recursive, count);
                    }
                }
            }
            return count;
        }
    }

    // Word count command - counts lines, words, and characters
    public static class WcCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: wc <filename>");
                System.out.println("       wc -l <filename>  (lines only)");
                System.out.println("       wc -w <filename>  (words only)");
                return;
            }

            boolean linesOnly = false;
            boolean wordsOnly = false;
            String fileName;

            if (args[1].equals("-l")) {
                linesOnly = true;
                fileName = args[2];
            } else if (args[1].equals("-w")) {
                wordsOnly = true;
                fileName = args[2];
            } else {
                fileName = args[1];
            }

            File file = new File(App.currentDirectory, fileName);
            if (!file.exists()) {
                System.out.println("wc: " + fileName + ": No such file");
                return;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                long lineCount = lines.size();
                long wordCount = lines.stream()
                        .mapToLong(line -> line.trim().isEmpty() ? 0 : line.split("\\s+").length)
                        .sum();
                long charCount = lines.stream()
                        .mapToLong(String::length)
                        .sum();

                if (linesOnly) {
                    System.out.printf("%7d %s%n", lineCount, fileName);
                } else if (wordsOnly) {
                    System.out.printf("%7d %s%n", wordCount, fileName);
                } else {
                    System.out.printf("%7d %7d %7d %s%n", lineCount, wordCount, charCount, fileName);
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }

    // Diff command - compare two files
    public static class DiffCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 3) {
                System.out.println("usage: diff <file1> <file2>");
                return;
            }

            File file1 = new File(App.currentDirectory, args[1]);
            File file2 = new File(App.currentDirectory, args[2]);

            if (!file1.exists()) {
                System.out.println("diff: " + args[1] + ": No such file");
                return;
            }
            if (!file2.exists()) {
                System.out.println("diff: " + args[2] + ": No such file");
                return;
            }

            try {
                List<String> lines1 = Files.readAllLines(file1.toPath());
                List<String> lines2 = Files.readAllLines(file2.toPath());

                boolean identical = true;
                int maxLines = Math.max(lines1.size(), lines2.size());

                for (int i = 0; i < maxLines; i++) {
                    String line1 = i < lines1.size() ? lines1.get(i) : null;
                    String line2 = i < lines2.size() ? lines2.get(i) : null;

                    if (line1 == null) {
                        System.out.println("> " + (i + 1) + ": " + line2);
                        identical = false;
                    } else if (line2 == null) {
                        System.out.println("< " + (i + 1) + ": " + line1);
                        identical = false;
                    } else if (!line1.equals(line2)) {
                        System.out.println("< " + (i + 1) + ": " + line1);
                        System.out.println("> " + (i + 1) + ": " + line2);
                        identical = false;
                    }
                }

                if (identical) {
                    System.out.println("Files are identical");
                }
            } catch (IOException e) {
                System.out.println("Error comparing files: " + e.getMessage());
            }
        }
    }
}
