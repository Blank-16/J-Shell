package com.devops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressionCommands {

    // Create zip archive
    public static class ZipCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 3) {
                System.out.println("usage: zip <output.zip> <file1> [file2] ...");
                System.out.println("   or: zip -r <output.zip> <directory>");
                return;
            }

            boolean recursive = args[1].equals("-r");
            String zipName = recursive ? args[2] : args[1];
            int startIndex = recursive ? 3 : 2;

            if (!zipName.endsWith(".zip")) {
                zipName += ".zip";
            }

            try (ZipOutputStream zos = new ZipOutputStream(
                    new FileOutputStream(new File(App.currentDirectory, zipName)))) {

                for (int i = startIndex; i < args.length; i++) {
                    File file = new File(App.currentDirectory, args[i]);
                    if (file.exists()) {
                        if (file.isDirectory() && recursive) {
                            zipDirectory(file, file.getName(), zos);
                        } else if (file.isFile()) {
                            addToZip(file, file.getName(), zos);
                            System.out.println("Added: " + args[i]);
                        }
                    } else {
                        System.out.println("Warning: " + args[i] + " not found");
                    }
                }
                System.out.println("Created: " + zipName);
            } catch (IOException e) {
                System.out.println("Error creating zip: " + e.getMessage());
            }
        }

        private void addToZip(File file, String name, ZipOutputStream zos) throws IOException {
            ZipEntry entry = new ZipEntry(name);
            zos.putNextEntry(entry);

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
            zos.closeEntry();
        }

        private void zipDirectory(File directory, String baseName, ZipOutputStream zos)
                throws IOException {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String entryName = baseName + "/" + file.getName();
                    if (file.isDirectory()) {
                        zipDirectory(file, entryName, zos);
                    } else {
                        addToZip(file, entryName, zos);
                        System.out.println("Added: " + entryName);
                    }
                }
            }
        }
    }

    // Extract zip archive
    public static class UnzipCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: unzip <file.zip>");
                System.out.println("   or: unzip <file.zip> <destination>");
                return;
            }

            String zipName = args[1];
            File destDir = args.length > 2
                    ? new File(App.currentDirectory, args[2]) : App.currentDirectory;

            File zipFile = new File(App.currentDirectory, zipName);
            if (!zipFile.exists()) {
                System.out.println("unzip: " + zipName + ": No such file");
                return;
            }

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry;
                byte[] buffer = new byte[1024];

                while ((entry = zis.getNextEntry()) != null) {
                    File newFile = new File(destDir, entry.getName());

                    if (entry.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        // Create parent directories if needed
                        new File(newFile.getParent()).mkdirs();

                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        System.out.println("Extracted: " + entry.getName());
                    }
                    zis.closeEntry();
                }
                System.out.println("Extraction complete!");
            } catch (IOException e) {
                System.out.println("Error extracting zip: " + e.getMessage());
            }
        }
    }

    // Compress file using gzip
    public static class GzipCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: gzip <filename>");
                return;
            }

            String fileName = args[1];
            File inputFile = new File(App.currentDirectory, fileName);

            if (!inputFile.exists()) {
                System.out.println("gzip: " + fileName + ": No such file");
                return;
            }

            File outputFile = new File(App.currentDirectory, fileName + ".gz");

            try (FileInputStream fis = new FileInputStream(inputFile); GZIPOutputStream gzos = new GZIPOutputStream(
                    new FileOutputStream(outputFile))) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    gzos.write(buffer, 0, len);
                }

                System.out.println("Compressed: " + fileName + " -> " + fileName + ".gz");

                // Optionally delete original file
                // inputFile.delete();
            } catch (IOException e) {
                System.out.println("Error compressing file: " + e.getMessage());
            }
        }
    }

    // Decompress gzip file
    public static class GunzipCommand implements Command {

        @Override
        public void execute(String[] args) {
            if (args.length < 2) {
                System.out.println("usage: gunzip <filename.gz>");
                return;
            }

            String fileName = args[1];
            File inputFile = new File(App.currentDirectory, fileName);

            if (!inputFile.exists()) {
                System.out.println("gunzip: " + fileName + ": No such file");
                return;
            }

            if (!fileName.endsWith(".gz")) {
                System.out.println("gunzip: " + fileName + ": unknown suffix");
                return;
            }

            String outputFileName = fileName.substring(0, fileName.length() - 3);
            File outputFile = new File(App.currentDirectory, outputFileName);

            try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(inputFile)); FileOutputStream fos = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                System.out.println("Decompressed: " + fileName + " -> " + outputFileName);

                // Optionally delete compressed file
                // inputFile.delete();
            } catch (IOException e) {
                System.out.println("Error decompressing file: " + e.getMessage());
            }
        }
    }
}
