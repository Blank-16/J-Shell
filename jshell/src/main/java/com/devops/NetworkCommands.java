package com.devops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;

public class NetworkCommands {

    // Ping a host
    public static class PingCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: ping <host>");
                System.out.println("   or: ping <host> <count>");
                return;
            }

            String host = args[1];
            int count = args.length > 2 ? Integer.parseInt(args[2]) : 4;

            System.out.println("Pinging " + host + " with " + count + " packets...");
            System.out.println();

            int successful = 0;
            long totalTime = 0;

            for (int i = 0; i < count; i++) {
                try {
                    InetAddress inet = InetAddress.getByName(host);
                    long startTime = System.currentTimeMillis();
                    boolean reachable = inet.isReachable(5000);
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;

                    if (reachable) {
                        System.out.println("Reply from " + host + " (" + inet.getHostAddress()
                                + "): time=" + responseTime + "ms");
                        successful++;
                        totalTime += responseTime;
                    } else {
                        System.out.println("Request timed out.");
                    }

                    if (i < count - 1) {
                        Thread.sleep(1000); // Wait 1 second between pings
                    }
                } catch (UnknownHostException e) {
                    System.out.println("Ping request could not find host " + host);
                    return;
                } catch (Exception e) {
                    System.out.println("Ping failed: " + e.getMessage());
                }
            }

            System.out.println();
            System.out.println("--- " + host + " ping statistics ---");
            System.out.println(count + " packets transmitted, " + successful + " received, "
                    + ((count - successful) * 100 / count) + "% packet loss");
            if (successful > 0) {
                System.out.println("Average response time: " + (totalTime / successful) + "ms");
            }
        }
    }

    // Download file from URL
    public static class WgetCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: wget <url>");
                System.out.println("   or: wget <url> <output-filename>");
                return;
            }

            String urlString = args[1];
            String fileName = args.length > 2 ? args[2]
                    : urlString.substring(urlString.lastIndexOf('/') + 1);

            if (fileName.isEmpty()) {
                fileName = "index.html";
            }

            System.out.println("Downloading: " + urlString);
            System.out.println("Saving to: " + fileName);

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println("Error: Server returned HTTP code " + responseCode);
                    return;
                }

                long fileSize = connection.getContentLengthLong();

                try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(
                        new File(App.currentDirectory, fileName))) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytes = 0;
                    long lastPrintTime = System.currentTimeMillis();

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;

                        // Print progress every 500ms
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastPrintTime > 500) {
                            if (fileSize > 0) {
                                int progress = (int) ((totalBytes * 100) / fileSize);
                                System.out.print("\rProgress: " + progress + "% ("
                                        + formatBytes(totalBytes) + " / "
                                        + formatBytes(fileSize) + ")");
                            } else {
                                System.out.print("\rDownloaded: " + formatBytes(totalBytes));
                            }
                            lastPrintTime = currentTime;
                        }
                    }

                    System.out.println("\nDownload complete: " + fileName
                            + " (" + formatBytes(totalBytes) + ")");
                }
            } catch (MalformedURLException e) {
                System.out.println("Error: Invalid URL - " + e.getMessage());
            } catch (IOException e) {
                System.out.println("\nDownload failed: " + e.getMessage());
            }
        }

        private String formatBytes(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }
            if (bytes < 1024 * 1024) {
                return String.format("%.2f KB", bytes / 1024.0);
            }
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    // Simple HTTP request command
    public static class CurlCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: curl <url>");
                System.out.println("   or: curl -o <filename> <url>");
                return;
            }

            boolean saveToFile = args[1].equals("-o");
            String fileName = saveToFile ? args[2] : null;
            String urlString = saveToFile ? args[3] : args[1];

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);

                int responseCode = connection.getResponseCode();
                System.out.println("HTTP Response Code: " + responseCode);

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {

                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = in.readLine()) != null) {
                        response.append(line).append("\n");
                    }

                    if (saveToFile) {
                        File outputFile = new File(App.currentDirectory, fileName);
                        try (FileWriter writer = new FileWriter(outputFile)) {
                            writer.write(response.toString());
                        }
                        System.out.println("Saved to: " + fileName);
                    } else {
                        System.out.println("\n--- Response Body ---");
                        System.out.println(response.toString());
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Show network interfaces
    public static class IfconfigCommand implements Command {

        @Override
        public void execute(String[] args) {
            System.out.println("Network Interfaces:");
            System.out.println("==================");

            try {
                java.util.Enumeration<NetworkInterface> interfaces
                        = NetworkInterface.getNetworkInterfaces();

                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();

                    System.out.println("\nInterface: " + ni.getName());
                    System.out.println("  Display Name: " + ni.getDisplayName());
                    System.out.println("  Status: " + (ni.isUp() ? "UP" : "DOWN"));

                    java.util.Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        System.out.println("  IP Address: " + addr.getHostAddress());
                    }
                }
            } catch (Exception e) {
                System.out.println("Error retrieving network interfaces: " + e.getMessage());
            }
        }
    }
}
