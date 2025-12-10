package com.devops;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UtilityCommands {

    // Sort lines in a file
    public static class SortCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: sort <filename>");
                System.out.println("   or: sort -r <filename>  (reverse order)");
                System.out.println("   or: sort -n <filename>  (numeric sort)");
                return;
            }

            boolean reverse = false;
            boolean numeric = false;
            String fileName;

            if (args[1].equals("-r")) {
                reverse = true;
                fileName = args[2];
            } else if (args[1].equals("-n")) {
                numeric = true;
                fileName = args[2];
            } else if (args[1].equals("-rn") || args[1].equals("-nr")) {
                reverse = true;
                numeric = true;
                fileName = args[2];
            } else {
                fileName = args[1];
            }

            File file = new File(App.currentDirectory, fileName);
            if (!file.exists()) {
                System.out.println("sort: " + fileName + ": No such file");
                return;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());

                if (numeric) {
                    lines.sort((a, b) -> {
                        try {
                            return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
                        } catch (NumberFormatException e) {
                            return a.compareTo(b);
                        }
                    });
                } else {
                    Collections.sort(lines);
                }

                if (reverse) {
                    Collections.reverse(lines);
                }

                for (String line : lines) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }

    // Show unique lines
    public static class UniqCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: uniq <filename>");
                System.out.println("   or: uniq -c <filename>  (count duplicates)");
                return;
            }

            boolean count = args[1].equals("-c");
            String fileName = count ? args[2] : args[1];

            File file = new File(App.currentDirectory, fileName);
            if (!file.exists()) {
                System.out.println("uniq: " + fileName + ": No such file");
                return;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                Map<String, Integer> lineCount = new LinkedHashMap<>();

                for (String line : lines) {
                    lineCount.put(line, lineCount.getOrDefault(line, 0) + 1);
                }

                for (Map.Entry<String, Integer> entry : lineCount.entrySet()) {
                    if (count) {
                        System.out.printf("%4d %s%n", entry.getValue(), entry.getKey());
                    } else {
                        System.out.println(entry.getKey());
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }

    // Calculate file checksum (MD5, SHA-256)
    public static class ChecksumCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: checksum <filename>");
                System.out.println("   or: checksum -sha256 <filename>");
                System.out.println("   or: checksum -md5 <filename>");
                return;
            }

            String algorithm = "MD5";
            String fileName;

            if (args[1].equals("-sha256")) {
                algorithm = "SHA-256";
                fileName = args[2];
            } else if (args[1].equals("-md5")) {
                algorithm = "MD5";
                fileName = args[2];
            } else {
                fileName = args[1];
            }

            File file = new File(App.currentDirectory, fileName);
            if (!file.exists()) {
                System.out.println("checksum: " + fileName + ": No such file");
                return;
            }

            try {
                MessageDigest digest = MessageDigest.getInstance(algorithm);

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        digest.update(buffer, 0, bytesRead);
                    }
                }

                byte[] hash = digest.digest();
                StringBuilder hexString = new StringBuilder();

                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }

                System.out.println(algorithm + " (" + fileName + ") = " + hexString.toString());
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Algorithm not supported: " + algorithm);
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }

    // Display file or directory size
    public static class DuCommand implements Command {

        @Override
        public void execute(String[] args) {
            boolean humanReadable = false;
            String path = ".";

            if (args.length > 1) {
                if (args[1].equals("-h")) {
                    humanReadable = true;
                    if (args.length > 2) {
                        path = args[2];
                    }
                } else {
                    path = args[1];
                    if (args.length > 2 && args[2].equals("-h")) {
                        humanReadable = true;
                    }
                }
            }

            File file = new File(App.currentDirectory, path);
            if (!file.exists()) {
                System.out.println("du: cannot access '" + path + "': No such file or directory");
                return;
            }

            long size = calculateSize(file);

            if (humanReadable) {
                System.out.println(formatBytes(size) + "\t" + path);
            } else {
                System.out.println((size / 1024) + "\t" + path);
            }
        }

        private long calculateSize(File file) {
            if (file.isFile()) {
                return file.length();
            }

            long size = 0;
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    size += calculateSize(f);
                }
            }
            return size;
        }

        private String formatBytes(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }
            if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            }
            if (bytes < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            }
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // Show file head (first n lines)
    public static class HeadCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: head <filename>");
                System.out.println("   or: head -n <count> <filename>");
                return;
            }

            int lineCount = 10;
            String fileName;

            if (args[1].equals("-n")) {
                lineCount = Integer.parseInt(args[2]);
                fileName = args[3];
            } else {
                fileName = args[1];
            }

            File file = new File(App.currentDirectory, fileName);
            if (!file.exists()) {
                System.out.println("head: " + fileName + ": No such file");
                return;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                int limit = Math.min(lineCount, lines.size());

                for (int i = 0; i < limit; i++) {
                    System.out.println(lines.get(i));
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }

    // Show file tail (last n lines)
    public static class TailCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: tail <filename>");
                System.out.println("   or: tail -n <count> <filename>");
                return;
            }

            int lineCount = 10;
            String fileName;

            if (args[1].equals("-n")) {
                lineCount = Integer.parseInt(args[2]);
                fileName = args[3];
            } else {
                fileName = args[1];
            }

            File file = new File(App.currentDirectory, fileName);
            if (!file.exists()) {
                System.out.println("tail: " + fileName + ": No such file");
                return;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());
                int start = Math.max(0, lines.size() - lineCount);

                for (int i = start; i < lines.size(); i++) {
                    System.out.println(lines.get(i));
                }
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }
        }
    }
}
