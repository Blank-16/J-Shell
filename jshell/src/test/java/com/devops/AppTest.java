package com.devops;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @TempDir Path tempDir;
    ShellContext ctx;

    @BeforeEach
    void setup() {
        ctx = new ShellContext(tempDir.toFile());
    }

    @Test
    void tokenizerHandlesQuotedSpaces() {
        String[] tokens = Tokenizer.tokenize("echo \"hello world\" foo");
        assertArrayEquals(new String[]{"echo", "hello world", "foo"}, tokens);
    }

    @Test
    void tokenizerHandlesSingleQuotes() {
        String[] tokens = Tokenizer.tokenize("echo 'a b c'");
        assertArrayEquals(new String[]{"echo", "a b c"}, tokens);
    }

    @Test
    void cdChangesDirectory() {
        File sub = tempDir.resolve("sub").toFile();
        sub.mkdir();
        int code = new FileSystemCommands.CdCommand().execute(ctx, new String[]{"cd", "sub"});
        assertEquals(0, code);
        assertEquals(sub.getAbsolutePath(), ctx.currentDirectory().getAbsolutePath());
    }

    @Test
    void cdToNonexistentReturnsError() {
        int code = new FileSystemCommands.CdCommand().execute(ctx, new String[]{"cd", "ghost"});
        assertEquals(1, code);
    }

    @Test
    void mkdirCreatesDirectory() {
        int code = new FileSystemCommands.MkdirCommand().execute(ctx, new String[]{"mkdir", "newdir"});
        assertEquals(0, code);
        assertTrue(tempDir.resolve("newdir").toFile().isDirectory());
    }

    @Test
    void mkdirWithParentsFlag() {
        int code = new FileSystemCommands.MkdirCommand().execute(ctx, new String[]{"mkdir", "-p", "a/b/c"});
        assertEquals(0, code);
        assertTrue(tempDir.resolve("a/b/c").toFile().isDirectory());
    }

    @Test
    void touchCreatesFile() {
        int code = new FileManipulationCommands.TouchCommand().execute(ctx, new String[]{"touch", "f.txt"});
        assertEquals(0, code);
        assertTrue(tempDir.resolve("f.txt").toFile().exists());
    }

    @Test
    void echoWritesToFile() throws IOException {
        int code = new TextCommands.EchoCommand().execute(ctx, new String[]{"echo", "hello world", ">", "out.txt"});
        assertEquals(0, code);
        String content = Files.readString(tempDir.resolve("out.txt")).trim();
        assertEquals("hello world", content);
    }

    @Test
    void echoAppendsToFile() throws IOException {
        Path p = tempDir.resolve("out.txt");
        Files.writeString(p, "line1\n");
        int code = new TextCommands.EchoCommand().execute(ctx, new String[]{"echo", "line2", ">>", "out.txt"});
        assertEquals(0, code);
        String content = Files.readString(p);
        assertTrue(content.contains("line1"));
        assertTrue(content.contains("line2"));
    }

    @Test
    void grepFindsMatchWithRegex() throws IOException {
        Path f = tempDir.resolve("data.txt");
        Files.writeString(f, "foo bar\nbaz qux\nfoo 123\n");
        int code = new TextCommands.GrepCommand().execute(ctx, new String[]{"grep", "foo\\s+\\d+", "data.txt"});
        assertEquals(0, code);
    }

    @Test
    void grepReturnsOneWhenNoMatch() throws IOException {
        Path f = tempDir.resolve("data.txt");
        Files.writeString(f, "hello world\n");
        int code = new TextCommands.GrepCommand().execute(ctx, new String[]{"grep", "xyz", "data.txt"});
        assertEquals(1, code);
    }

    @Test
    void rmRemovesFile() throws IOException {
        Path f = tempDir.resolve("del.txt");
        Files.createFile(f);
        int code = new FileManipulationCommands.RmCommand().execute(ctx, new String[]{"rm", "del.txt"});
        assertEquals(0, code);
        assertFalse(f.toFile().exists());
    }

    @Test
    void rmRecursiveRemovesDirectory() throws IOException {
        Path dir = tempDir.resolve("rmdir");
        Files.createDirectory(dir);
        Files.createFile(dir.resolve("a.txt"));
        int code = new FileManipulationCommands.RmCommand().execute(ctx, new String[]{"rm", "-r", "rmdir"});
        assertEquals(0, code);
        assertFalse(dir.toFile().exists());
    }

    @Test
    void headReadsFirstLines() throws IOException {
        Path f = tempDir.resolve("big.txt");
        Files.writeString(f, "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n");
        // Just verify no exception and returns 0
        int code = new UtilityCommands.HeadCommand().execute(ctx, new String[]{"head", "-n", "3", "big.txt"});
        assertEquals(0, code);
    }

    @Test
    void tailReadsLastLines() throws IOException {
        Path f = tempDir.resolve("big.txt");
        Files.writeString(f, "1\n2\n3\n4\n5\n");
        int code = new UtilityCommands.TailCommand().execute(ctx, new String[]{"tail", "-n", "2", "big.txt"});
        assertEquals(0, code);
    }

    @Test
    void checksumComputesMd5() throws IOException {
        Path f = tempDir.resolve("hash.txt");
        Files.writeString(f, "hello");
        int code = new UtilityCommands.ChecksumCommand().execute(ctx, new String[]{"checksum", "-md5", "hash.txt"});
        assertEquals(0, code);
    }

    @Test
    void historyRecordsCommands() {
        ctx.addHistory("ls");
        ctx.addHistory("pwd");
        assertEquals(2, ctx.history().size());
        assertEquals("ls",  ctx.history().get(0));
        assertEquals("pwd", ctx.history().get(1));
    }

    @Test
    void zipSlipIsBlocked() throws IOException {
        // Build a zip with a path traversal entry name
        Path zipPath = tempDir.resolve("evil.zip");
        try (var zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipPath.toFile()))) {
            var entry = new java.util.zip.ZipEntry("../../evil.txt");
            zos.putNextEntry(entry);
            zos.write("pwned".getBytes());
            zos.closeEntry();
        }
        int code = new CompressionCommands.UnzipCommand().execute(ctx, new String[]{"unzip", "evil.zip"});
        // Should complete (blocked, not crash) and the traversal file must NOT exist
        assertFalse(tempDir.getParent().resolve("evil.txt").toFile().exists());
    }
}
