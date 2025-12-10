package com.devops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProcessCommands {

    // Show system processes
    public static class PsCommand implements Command {

        @Override
        public void execute(String[] args) {
            System.out.println("Java Process Information:");
            System.out.println("========================");

            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            System.out.println("Process ID: " + runtimeBean.getPid());
            System.out.println("Uptime: " + formatUptime(runtimeBean.getUptime()));
            System.out.println("Start Time: " + new Date(runtimeBean.getStartTime()));

            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

            System.out.println("\nMemory Usage:");
            System.out.println("  Heap Used: " + formatBytes(heapUsage.getUsed()));
            System.out.println("  Heap Max: " + formatBytes(heapUsage.getMax()));

            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            System.out.println("\nThread Count: " + threadBean.getThreadCount());

            // Show all Java threads
            if (args.length > 1 && args[1].equals("-v")) {
                System.out.println("\nActive Threads:");
                long[] threadIds = threadBean.getAllThreadIds();
                for (long id : threadIds) {
                    ThreadInfo info = threadBean.getThreadInfo(id);
                    if (info != null) {
                        System.out.println("  [" + id + "] " + info.getThreadName()
                                + " - " + info.getThreadState());
                    }
                }
            }
        }

        private String formatUptime(long milliseconds) {
            long seconds = milliseconds / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
            } else if (hours > 0) {
                return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
            } else if (minutes > 0) {
                return minutes + "m " + (seconds % 60) + "s";
            } else {
                return seconds + "s";
            }
        }

        private String formatBytes(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }
            if (bytes < 1024 * 1024) {
                return String.format("%.2f KB", bytes / 1024.0);
            }
            if (bytes < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
            }
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // Execute system command
    public static class ExecCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: exec <system-command> [arguments]");
                System.out.println("Example: exec notepad.exe");
                return;
            }

            String[] command = new String[args.length - 1];
            System.arraycopy(args, 1, command, 0, args.length - 1);

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(App.currentDirectory);
                pb.redirectErrorStream(true);

                System.out.println("Executing: " + String.join(" ", command));

                Process process = pb.start();

                // Read output
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = process.waitFor();
                System.out.println("\nProcess exited with code: " + exitCode);

            } catch (IOException e) {
                System.out.println("Error executing command: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Command interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

    // Show environment variables
    public static class EnvCommand implements Command {

        @Override
        public void execute(String[] args) {
            Map<String, String> env = System.getenv();

            if (args.length > 1) {
                // Show specific variable
                String varName = args[1];
                String value = env.get(varName);
                if (value != null) {
                    System.out.println(varName + "=" + value);
                } else {
                    System.out.println("Variable not found: " + varName);
                }
            } else {
                // Show all variables
                System.out.println("Environment Variables:");
                System.out.println("=====================");

                List<String> sortedKeys = new ArrayList<>(env.keySet());
                Collections.sort(sortedKeys);

                for (String key : sortedKeys) {
                    System.out.println(key + "=" + env.get(key));
                }
            }
        }
    }

    // System information
    public static class UnameCommand implements Command {

        @Override
        public void execute(String[] args) {
            System.out.println("System Information:");
            System.out.println("==================");

            System.out.println("OS Name: " + System.getProperty("os.name"));
            System.out.println("OS Version: " + System.getProperty("os.version"));
            System.out.println("OS Architecture: " + System.getProperty("os.arch"));
            System.out.println("Java Version: " + System.getProperty("java.version"));
            System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
            System.out.println("Java Home: " + System.getProperty("java.home"));
            System.out.println("User Name: " + System.getProperty("user.name"));
            System.out.println("User Home: " + System.getProperty("user.home"));
            System.out.println("Current Directory: " + System.getProperty("user.dir"));

            Runtime runtime = Runtime.getRuntime();
            System.out.println("\nRuntime Information:");
            System.out.println("Available Processors: " + runtime.availableProcessors());
            System.out.println("Free Memory: " + formatBytes(runtime.freeMemory()));
            System.out.println("Total Memory: " + formatBytes(runtime.totalMemory()));
            System.out.println("Max Memory: " + formatBytes(runtime.maxMemory()));
        }

        private String formatBytes(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }
            if (bytes < 1024 * 1024) {
                return String.format("%.2f KB", bytes / 1024.0);
            }
            if (bytes < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
            }
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
