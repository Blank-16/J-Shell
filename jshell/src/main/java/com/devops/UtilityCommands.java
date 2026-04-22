package com.devops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class UtilityCommands {

    private UtilityCommands() {}

    public static final class SortCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            boolean reverse = false;
            boolean numeric = false;
            int fileIdx = 1;

            for (int i = 1; i < args.length; i++) {
                switch (args[i]) {
                    case "-r"        -> { reverse = true; fileIdx = i + 1; }
                    case "-n"        -> { numeric = true; fileIdx = i + 1; }
                    case "-rn", "-nr" -> { reverse = true; numeric = true; fileIdx = i + 1; }
                    default          -> { fileIdx = i; i = args.length; } // stop at first non-flag
                }
            }

            if (fileIdx >= args.length) {
                System.err.println("usage: " + usage());
                return 1;
            }

            File file = new File(context.currentDirectory(), args[fileIdx]);
            if (!file.exists()) {
                System.err.println("sort: '" + args[fileIdx] + "': No such file");
                return 1;
            }

            try {
                List<String> lines = Files.readAllLines(file.toPath());

                if (numeric) {
                    lines.sort((a, b) -> {
                        try {
                            return Double.compare(Double.parseDouble(a.trim()), Double.parseDouble(b.trim()));
                        } catch (NumberFormatException e) {
                            return a.compareTo(b);
                        }
                    });
                } else {
                    Collections.sort(lines);
                }

                if (reverse) Collections.reverse(lines);
                lines.forEach(System.out::println);
            } catch (IOException e) {
                System.err.println("sort: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        @Override public String name()  { return "sort"; }
        @Override public String usage() { return "sort [-r] [-n] <file>"; }
    }

    public static final class UniqCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 2) {
                System.err.println("usage: " + usage());
                return 1;
            }

            boolean count = args[1].equals("-c");
            if (count && args.length < 3) {
                System.err.println("usage: " + usage());
                return 1;
            }
            String fileName = count ? args[2] : args[1];

            File file = new File(context.currentDirectory(), fileName);
            if (!file.exists()) {
                System.err.println("uniq: '" + fileName + "': No such file");
                return 1;
            }

            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                // Correct uniq semantics: collapse only adjacent duplicates
                String prev = null;
                int run = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(prev)) {
                        run++;
                    } else {
                        if (prev != null) printUniq(prev, run, count);
                        prev = line;
                        run = 1;
                    }
                }
                if (prev != null) printUniq(prev, run, count);
            } catch (IOException e) {
                System.err.println("uniq: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        private void printUniq(String line, int runCount, boolean showCount) {
            if (showCount) System.out.printf("%4d %s%n", runCount, line);
            else           System.out.println(line);
        }

        @Override public String name()  { return "uniq"; }
        @Override public String usage() { return "uniq [-c] <file>"; }
    }

    public static final class ChecksumCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 2) {
                System.err.println("usage: " + usage());
                return 1;
            }

            String algorithm = "SHA-256";
            int fileIdx = 1;

            switch (args[1]) {
                case "-md5"    -> { algorithm = "MD5";    fileIdx = 2; }
                case "-sha1"   -> { algorithm = "SHA-1";  fileIdx = 2; }
                case "-sha256" -> { algorithm = "SHA-256"; fileIdx = 2; }
            }

            if (fileIdx >= args.length) {
                System.err.println("usage: " + usage());
                return 1;
            }

            File file = new File(context.currentDirectory(), args[fileIdx]);
            if (!file.exists()) {
                System.err.println("checksum: '" + args[fileIdx] + "': No such file");
                return 1;
            }

            try {
                MessageDigest digest = MessageDigest.getInstance(algorithm);
                byte[] buffer = new byte[8192];
                int read;
                try (var fis = new FileInputStream(file)) {
                    while ((read = fis.read(buffer)) != -1) {
                        digest.update(buffer, 0, read);
                    }
                }
                byte[] hash = digest.digest();
                var hex = new StringBuilder(hash.length * 2);
                for (byte b : hash) hex.append(String.format("%02x", b));
                System.out.printf("%s  %s  %s%n", algorithm, args[fileIdx], hex);
            } catch (NoSuchAlgorithmException e) {
                System.err.println("checksum: unsupported algorithm '" + algorithm + "'");
                return 1;
            } catch (IOException e) {
                System.err.println("checksum: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        @Override public String name()  { return "checksum"; }
        @Override public String usage() { return "checksum [-md5|-sha1|-sha256] <file>"; }
    }

    public static final class DuCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            boolean human = false;
            String path = ".";

            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("-h")) human = true;
                else path = args[i];
            }

            File target = new File(context.currentDirectory(), path);
            if (!target.exists()) {
                System.err.println("du: '" + path + "': No such file or directory");
                return 1;
            }

            long size = sizeOf(target);
            System.out.printf("%s\t%s%n",
                human ? ByteFormatter.formatCompact(size) : String.valueOf(size / 1024),
                path);
            return 0;
        }

        private long sizeOf(File file) {
            if (file.isFile()) return file.length();
            File[] children = file.listFiles();
            if (children == null) return 0;
            long total = 0;
            for (File child : children) total += sizeOf(child);
            return total;
        }

        @Override public String name()  { return "du"; }
        @Override public String usage() { return "du [-h] [path]"; }
    }

    public static final class HeadCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 2) {
                System.err.println("usage: " + usage());
                return 1;
            }

            int lines = 10;
            int fileIdx = 1;

            if (args[1].equals("-n")) {
                if (args.length < 4) { System.err.println("usage: " + usage()); return 1; }
                try {
                    lines = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    System.err.println("head: invalid line count '" + args[2] + "'");
                    return 1;
                }
                fileIdx = 3;
            }

            File file = new File(context.currentDirectory(), args[fileIdx]);
            if (!file.exists()) {
                System.err.println("head: '" + args[fileIdx] + "': No such file");
                return 1;
            }

            // Stream — stops reading after n lines; never loads whole file
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                String line;
                int count = 0;
                while (count < lines && (line = reader.readLine()) != null) {
                    System.out.println(line);
                    count++;
                }
            } catch (IOException e) {
                System.err.println("head: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        @Override public String name()  { return "head"; }
        @Override public String usage() { return "head [-n count] <file>"; }
    }

    public static final class TailCommand implements Command {

        @Override
        public int execute(ShellContext context, String[] args) {
            if (args.length < 2) {
                System.err.println("usage: " + usage());
                return 1;
            }

            int lines = 10;
            int fileIdx = 1;

            if (args[1].equals("-n")) {
                if (args.length < 4) { System.err.println("usage: " + usage()); return 1; }
                try {
                    lines = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    System.err.println("tail: invalid line count '" + args[2] + "'");
                    return 1;
                }
                fileIdx = 3;
            }

            File file = new File(context.currentDirectory(), args[fileIdx]);
            if (!file.exists()) {
                System.err.println("tail: '" + args[fileIdx] + "': No such file");
                return 1;
            }

            // Ring buffer — single pass, O(n) lines kept in memory at most
            final int capacity = lines;
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                var ring = new String[capacity];
                int pos = 0, total = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    ring[pos % capacity] = line;
                    pos++;
                    total++;
                }
                int count = Math.min(total, capacity);
                int start = total > capacity ? pos % capacity : 0;
                for (int i = 0; i < count; i++) {
                    System.out.println(ring[(start + i) % capacity]);
                }
            } catch (IOException e) {
                System.err.println("tail: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        @Override public String name()  { return "tail"; }
        @Override public String usage() { return "tail [-n count] <file>"; }
    }
}
