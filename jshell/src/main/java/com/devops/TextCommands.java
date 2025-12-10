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

    //'help' Command
    public static class HelpCommand implements Command {

        @Override
        public void execute(String[] args) {

            System.out.println("NAVIGATION & FILE SYSTEM:");
            System.out.println("  ls              - List directory contents");
            System.out.println("  pwd             - Print working directory");
            System.out.println("  cd <dir>        - Change directory");
            System.out.println("  mkdir <dir>     - Create directory");
            System.out.println();

            System.out.println("FILE MANIPULATION:");
            System.out.println("  touch <file>    - Create empty file");
            System.out.println("  rm <file>       - Remove file/directory");
            System.out.println("  cat <file>      - Display file contents");
            System.out.println("  cp <src> <dst>  - Copy file (use -r for directories)");
            System.out.println("  mv <src> <dst>  - Move/rename file");
            System.out.println();

            System.out.println("TEXT UTILITIES:");
            System.out.println("  echo <text>     - Print text (supports > and >>)");
            System.out.println("  grep <pattern> <file> - Search for pattern");
            System.out.println("  wc <file>       - Count lines, words, characters");
            System.out.println("  head <file>     - Show first 10 lines");
            System.out.println("  tail <file>     - Show last 10 lines");
            System.out.println("  sort <file>     - Sort file contents");
            System.out.println("  uniq <file>     - Show unique lines");
            System.out.println("  diff <f1> <f2>  - Compare two files");
            System.out.println();

            System.out.println("SEARCH & FIND:");
            System.out.println("  find <pattern>  - Find files by name");
            System.out.println("  du <path>       - Show disk usage");
            System.out.println();

            System.out.println("COMPRESSION:");
            System.out.println("  zip <out.zip> <files>  - Create zip archive");
            System.out.println("  unzip <file.zip>       - Extract zip archive");
            System.out.println("  gzip <file>            - Compress with gzip");
            System.out.println("  gunzip <file.gz>       - Decompress gzip file");
            System.out.println();

            System.out.println("NETWORK:");
            System.out.println("  ping <host>     - Ping a host");
            System.out.println("  wget <url>      - Download file from URL");
            System.out.println("  curl <url>      - Make HTTP request");
            System.out.println("  ifconfig        - Show network interfaces");
            System.out.println();

            System.out.println("SYSTEM & PROCESS:");
            System.out.println("  ps              - Show process information");
            System.out.println("  exec <cmd>      - Execute system command");
            System.out.println("  env [var]       - Show environment variables");
            System.out.println("  uname           - Show system information");
            System.out.println("  history         - Show command history");
            System.out.println("  whoami          - Show current user");
            System.out.println("  date            - Show current date/time");
            System.out.println("  clear           - Clear screen");
            System.out.println();

            System.out.println("UTILITIES:");
            System.out.println("  checksum <file> - Calculate file checksum");
            System.out.println();

            System.out.println("TIPS:");
            System.out.println("  - Use 'exit' to quit J-Shell");
            System.out.println("  - Many commands support flags (e.g., -r, -n, -h)");
            System.out.println("  - Use > to redirect output to file");
            System.out.println("  - Use >> to append output to file");
            System.out.println();
        }
    }
}
