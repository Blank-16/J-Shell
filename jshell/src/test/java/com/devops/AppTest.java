package com.devops;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
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

    // ExecutionResult record

    @Test void executionResult_ok_hasZeroExitCode() {
        ExecutionResult r = ExecutionResult.ok(ctx);
        assertEquals(0, r.exitCode());
        assertTrue(r.succeeded());
        assertSame(ctx, r.context());
    }

    @Test void executionResult_fail_hasNonZeroExitCode() {
        ExecutionResult r = ExecutionResult.fail(ctx);
        assertEquals(1, r.exitCode());
        assertFalse(r.succeeded());
    }

    @Test void executionResult_misuse_hasCode2() {
        ExecutionResult r = ExecutionResult.misuse(ctx);
        assertEquals(2, r.exitCode());
        assertFalse(r.succeeded());
    }

    @Test void executionResult_of_customCode() {
        ExecutionResult r = ExecutionResult.of(ctx, 42);
        assertEquals(42, r.exitCode());
    }

    // Tokenizer

    @Test void tokenizer_doubleQuotedSpace() {
        assertArrayEquals(new String[]{"echo", "hello world", "foo"},
            Tokenizer.tokenize("echo \"hello world\" foo"));
    }

    @Test void tokenizer_singleQuotedSpace() {
        assertArrayEquals(new String[]{"echo", "a b c"},
            Tokenizer.tokenize("echo 'a b c'"));
    }

    @Test void tokenizer_unquoted() {
        assertArrayEquals(new String[]{"ls", "-la"}, Tokenizer.tokenize("ls -la"));
    }

    @Test void tokenizer_mixedQuotes() {
        assertArrayEquals(new String[]{"cmd", "hello world", "foo bar"},
            Tokenizer.tokenize("cmd \"hello world\" 'foo bar'"));
    }

    // ShellContext — immutability & invariants

    @Test void shellContext_withDirectory_returnsNewInstance() throws Exception {
        File sub = tempDir.resolve("sub").toFile();
        sub.mkdir();
        ShellContext next = ctx.withDirectory(sub.getCanonicalFile());
        assertNotSame(ctx, next);
        assertEquals(sub.getCanonicalPath(), next.currentDirectory().getCanonicalPath());
        assertEquals(tempDir.toFile().getCanonicalPath(), ctx.currentDirectory().getCanonicalPath());
    }

    @Test void shellContext_historyIsUnmodifiable() {
        ctx.addHistory("ls");
        assertThrows(UnsupportedOperationException.class, () -> ctx.history().add("injected"));
    }

    @Test void shellContext_historyRecordsOrder() {
        ctx.addHistory("ls");
        ctx.addHistory("pwd");
        assertEquals(2, ctx.history().size());
        assertEquals("ls",  ctx.history().get(0));
        assertEquals("pwd", ctx.history().get(1));
    }

    // Filesystem — exit codes and context propagation

    @Test void cd_returnsNewContextWithUpdatedDir() throws Exception {
        Path sub = tempDir.resolve("sub");
        sub.toFile().mkdir();
        ExecutionResult r = new FileSystemCommands.CdCommand().execute(ctx, new String[]{"cd", "sub"});
        assertTrue(r.succeeded());
        assertNotSame(ctx, r.context());
        assertEquals(sub.toFile().getCanonicalPath(), r.context().currentDirectory().getCanonicalPath());
    }

    @Test void cd_nonexistent_failsAndReturnsSameContext() {
        ExecutionResult r = new FileSystemCommands.CdCommand().execute(ctx, new String[]{"cd", "ghost"});
        assertFalse(r.succeeded());
        assertSame(ctx, r.context());
    }

    @Test void cd_parentDir() throws Exception {
        Path sub = tempDir.resolve("up");
        sub.toFile().mkdir();
        ShellContext inSub = ctx.withDirectory(sub.toFile().getCanonicalFile());
        ExecutionResult r  = new FileSystemCommands.CdCommand().execute(inSub, new String[]{"cd", ".."});
        assertTrue(r.succeeded());
        assertEquals(tempDir.toFile().getCanonicalPath(), r.context().currentDirectory().getCanonicalPath());
    }

    @Test void mkdir_succeeds() {
        ExecutionResult r = new FileSystemCommands.MkdirCommand().execute(ctx, new String[]{"mkdir", "newdir"});
        assertTrue(r.succeeded());
        assertTrue(tempDir.resolve("newdir").toFile().isDirectory());
    }

    @Test void mkdir_parentFlag_createsNested() {
        ExecutionResult r = new FileSystemCommands.MkdirCommand().execute(ctx, new String[]{"mkdir", "-p", "a/b/c"});
        assertTrue(r.succeeded());
        assertTrue(tempDir.resolve("a/b/c").toFile().isDirectory());
    }

    @Test void touch_createsFile() {
        ExecutionResult r = new FileManipulationCommands.TouchCommand().execute(ctx, new String[]{"touch", "t.txt"});
        assertTrue(r.succeeded());
        assertTrue(tempDir.resolve("t.txt").toFile().exists());
    }

    @Test void rm_removesFile() throws Exception {
        Files.createFile(tempDir.resolve("del.txt"));
        ExecutionResult r = new FileManipulationCommands.RmCommand().execute(ctx, new String[]{"rm", "del.txt"});
        assertTrue(r.succeeded());
        assertFalse(tempDir.resolve("del.txt").toFile().exists());
    }

    @Test void rm_recursive_removesDirectory() throws Exception {
        Path dir = tempDir.resolve("rmdir");
        Files.createDirectory(dir);
        Files.createFile(dir.resolve("inner.txt"));
        ExecutionResult r = new FileManipulationCommands.RmCommand().execute(ctx, new String[]{"rm", "-r", "rmdir"});
        assertTrue(r.succeeded());
        assertFalse(dir.toFile().exists());
    }

    @Test void rm_directoryWithoutFlag_fails() throws Exception {
        Files.createDirectory(tempDir.resolve("nodelflags"));
        ExecutionResult r = new FileManipulationCommands.RmCommand().execute(ctx, new String[]{"rm", "nodelflags"});
        assertFalse(r.succeeded());
        assertTrue(tempDir.resolve("nodelflags").toFile().exists());
    }

    @Test void rm_nonexistent_fails() {
        ExecutionResult r = new FileManipulationCommands.RmCommand().execute(ctx, new String[]{"rm", "ghost.txt"});
        assertFalse(r.succeeded());
    }

    // cp / mv

    @Test void cp_copiesFile() throws Exception {
        Files.writeString(tempDir.resolve("src.txt"), "hello");
        ExecutionResult r = new AdvancedFileCommands.CpCommand().execute(ctx, new String[]{"cp", "src.txt", "dst.txt"});
        assertTrue(r.succeeded());
        assertEquals("hello", Files.readString(tempDir.resolve("dst.txt")));
    }

    @Test void cp_recursive_copiesDirectory() throws Exception {
        Path dir = tempDir.resolve("srcdir");
        Files.createDirectory(dir);
        Files.writeString(dir.resolve("a.txt"), "content");
        ExecutionResult r = new AdvancedFileCommands.CpCommand().execute(ctx, new String[]{"cp", "-r", "srcdir", "dstdir"});
        assertTrue(r.succeeded());
        assertTrue(tempDir.resolve("dstdir/a.txt").toFile().exists());
    }

    @Test void cp_missingSource_fails() {
        ExecutionResult r = new AdvancedFileCommands.CpCommand().execute(ctx, new String[]{"cp", "ghost.txt", "dst.txt"});
        assertFalse(r.succeeded());
    }

    @Test void mv_renamesFile() throws Exception {
        Files.writeString(tempDir.resolve("old.txt"), "data");
        ExecutionResult r = new AdvancedFileCommands.MvCommand().execute(ctx, new String[]{"mv", "old.txt", "new.txt"});
        assertTrue(r.succeeded());
        assertFalse(tempDir.resolve("old.txt").toFile().exists());
        assertTrue(tempDir.resolve("new.txt").toFile().exists());
    }

    // echo

    @Test void echo_writesToFile() throws Exception {
        new TextCommands.EchoCommand().execute(ctx, new String[]{"echo", "hello world", ">", "out.txt"});
        assertEquals("hello world", Files.readString(tempDir.resolve("out.txt")).trim());
    }

    @Test void echo_appendsToFile() throws Exception {
        Files.writeString(tempDir.resolve("out.txt"), "line1\n");
        new TextCommands.EchoCommand().execute(ctx, new String[]{"echo", "line2", ">>", "out.txt"});
        String content = Files.readString(tempDir.resolve("out.txt"));
        assertTrue(content.contains("line1") && content.contains("line2"));
    }

    // grep

    @Test void grep_matchesRegex() throws Exception {
        Files.writeString(tempDir.resolve("g.txt"), "foo bar\nbaz qux\nfoo 123\n");
        ExecutionResult r = new TextCommands.GrepCommand().execute(ctx, new String[]{"grep", "foo\\s+\\d+", "g.txt"});
        assertTrue(r.succeeded());
    }

    @Test void grep_caseInsensitive() throws Exception {
        Files.writeString(tempDir.resolve("gi.txt"), "Hello World\n");
        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new TextCommands.GrepCommand().execute(ctx, new String[]{"grep", "-i", "hello", "gi.txt"});
        System.setOut(old);
        assertTrue(b.toString().contains("Hello World"));
    }

    @Test void grep_noMatch_returnsExitCode1() throws Exception {
        Files.writeString(tempDir.resolve("g2.txt"), "hello world\n");
        ExecutionResult r = new TextCommands.GrepCommand().execute(ctx, new String[]{"grep", "xyz", "g2.txt"});
        assertEquals(1, r.exitCode());
    }

    @Test void grep_invalidRegex_returnsMisuse() throws Exception {
        Files.writeString(tempDir.resolve("g3.txt"), "data\n");
        ExecutionResult r = new TextCommands.GrepCommand().execute(ctx, new String[]{"grep", "[invalid", "g3.txt"});
        assertFalse(r.succeeded());
    }

    // head / tail streaming

    @Test void head_returnsFirstNLines() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 50; i++) sb.append(i).append("\n");
        Files.writeString(tempDir.resolve("big.txt"), sb.toString());

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        ExecutionResult r = new UtilityCommands.HeadCommand().execute(ctx, new String[]{"head", "-n", "3", "big.txt"});
        System.setOut(old);

        assertTrue(r.succeeded());
        assertArrayEquals(new String[]{"1", "2", "3"}, b.toString().trim().split("\n"));
    }

    @Test void tail_returnsLastNLines() throws Exception {
        Files.writeString(tempDir.resolve("t.txt"), "1\n2\n3\n4\n5\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        ExecutionResult r = new UtilityCommands.TailCommand().execute(ctx, new String[]{"tail", "-n", "2", "t.txt"});
        System.setOut(old);

        assertTrue(r.succeeded());
        assertArrayEquals(new String[]{"4", "5"}, b.toString().trim().split("\n"));
    }

    @Test void tail_ringBufferOrderingAtCapacityBoundary() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 25; i++) sb.append(i).append("\n");
        Files.writeString(tempDir.resolve("ring.txt"), sb.toString());

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new UtilityCommands.TailCommand().execute(ctx, new String[]{"tail", "-n", "10", "ring.txt"});
        System.setOut(old);

        String[] lines = b.toString().trim().split("\n");
        assertEquals(10, lines.length);
        assertEquals("16", lines[0]);
        assertEquals("25", lines[9]);
    }

    @Test void head_invalidCount_fails() throws Exception {
        Files.writeString(tempDir.resolve("x.txt"), "data\n");
        ExecutionResult r = new UtilityCommands.HeadCommand().execute(ctx, new String[]{"head", "-n", "abc", "x.txt"});
        assertFalse(r.succeeded());
    }

    // Large file — streaming verification

    @Test void cat_largeFile_streamsWithoutOOM() throws Exception {
        Path f = tempDir.resolve("large.txt");
        try (var w = Files.newBufferedWriter(f)) {
            for (int i = 0; i < 100_000; i++) w.write("line " + i + "\n");
        }
        PrintStream old = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        ExecutionResult r = new FileManipulationCommands.CatCommand().execute(ctx, new String[]{"cat", "large.txt"});
        System.setOut(old);
        assertTrue(r.succeeded());
    }

    // uniq — POSIX correctness

    @Test void uniq_collapseAdjacentOnly() throws Exception {
        Files.writeString(tempDir.resolve("uq.txt"), "a\na\nb\na\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new UtilityCommands.UniqCommand().execute(ctx, new String[]{"uniq", "uq.txt"});
        System.setOut(old);

        assertEquals("a\nb\na", b.toString().trim());
    }

    @Test void uniq_countFlag() throws Exception {
        Files.writeString(tempDir.resolve("uqc.txt"), "x\nx\nx\ny\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new UtilityCommands.UniqCommand().execute(ctx, new String[]{"uniq", "-c", "uqc.txt"});
        System.setOut(old);

        String out = b.toString();
        assertTrue(out.contains("3") && out.contains("x"));
        assertTrue(out.contains("1") && out.contains("y"));
    }

    // sort — output assertions

    @Test void sort_alphabetical() throws Exception {
        Files.writeString(tempDir.resolve("sa.txt"), "banana\napple\ncherry\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new UtilityCommands.SortCommand().execute(ctx, new String[]{"sort", "sa.txt"});
        System.setOut(old);

        assertArrayEquals(new String[]{"apple", "banana", "cherry"}, b.toString().trim().split("\n"));
    }

    @Test void sort_numeric() throws Exception {
        Files.writeString(tempDir.resolve("sn.txt"), "10\n2\n30\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new UtilityCommands.SortCommand().execute(ctx, new String[]{"sort", "-n", "sn.txt"});
        System.setOut(old);

        assertArrayEquals(new String[]{"2", "10", "30"}, b.toString().trim().split("\n"));
    }

    @Test void sort_reverse() throws Exception {
        Files.writeString(tempDir.resolve("sr.txt"), "banana\napple\ncherry\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new UtilityCommands.SortCommand().execute(ctx, new String[]{"sort", "-r", "sr.txt"});
        System.setOut(old);

        assertArrayEquals(new String[]{"cherry", "banana", "apple"}, b.toString().trim().split("\n"));
    }

    // diff — Myers algorithm correctness

    @Test void diff_identicalFiles_exitCode0() throws Exception {
        Files.writeString(tempDir.resolve("da.txt"), "line1\nline2\nline3\n");
        Files.writeString(tempDir.resolve("db.txt"), "line1\nline2\nline3\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        ExecutionResult r = new SearchCommands.DiffCommand().execute(ctx, new String[]{"diff", "da.txt", "db.txt"});
        System.setOut(old);

        assertEquals(0, r.exitCode());
        assertTrue(b.toString().contains("identical"));
    }

    @Test void diff_filesDiffer_exitCode1() throws Exception {
        Files.writeString(tempDir.resolve("d1.txt"), "a\nb\n");
        Files.writeString(tempDir.resolve("d2.txt"), "a\nc\n");

        ExecutionResult r = new SearchCommands.DiffCommand().execute(ctx, new String[]{"diff", "d1.txt", "d2.txt"});
        assertEquals(1, r.exitCode());
    }

    @Test void diff_insertion_noOffsetOnSubsequentLines() throws Exception {
        Files.writeString(tempDir.resolve("di1.txt"), "a\nb\nc\n");
        Files.writeString(tempDir.resolve("di2.txt"), "a\nINSERTED\nb\nc\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new SearchCommands.DiffCommand().execute(ctx, new String[]{"diff", "di1.txt", "di2.txt"});
        System.setOut(old);

        String out = b.toString();
        assertTrue(out.contains("+ INSERTED"));
        assertFalse(out.contains("- b"));
        assertFalse(out.contains("- c"));
    }

    @Test void diff_deletion_noOffsetOnSubsequentLines() throws Exception {
        Files.writeString(tempDir.resolve("dd1.txt"), "a\nDELETED\nb\nc\n");
        Files.writeString(tempDir.resolve("dd2.txt"), "a\nb\nc\n");

        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        new SearchCommands.DiffCommand().execute(ctx, new String[]{"diff", "dd1.txt", "dd2.txt"});
        System.setOut(old);

        String out = b.toString();
        assertTrue(out.contains("- DELETED"));
        assertFalse(out.contains("- b"));
        assertFalse(out.contains("- c"));
    }

    // checksum

    @Test void checksum_md5_knownValue() throws Exception {
        Files.writeString(tempDir.resolve("h.txt"), "hello");
        PrintStream old = System.out;
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        System.setOut(new PrintStream(b));
        ExecutionResult r = new UtilityCommands.ChecksumCommand().execute(ctx, new String[]{"checksum", "-md5", "h.txt"});
        System.setOut(old);
        assertTrue(r.succeeded());
        assertTrue(b.toString().contains("5d41402abc4b2a76b9719d911017c592"));
    }

    // Security — zip slip

    @Test void unzip_zipSlipBlocked() throws Exception {
        Path zipPath = tempDir.resolve("evil.zip");
        try (var zos = new java.util.zip.ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            var entry = new java.util.zip.ZipEntry("../../evil.txt");
            zos.putNextEntry(entry);
            zos.write("pwned".getBytes());
            zos.closeEntry();
        }
        new CompressionCommands.UnzipCommand().execute(ctx, new String[]{"unzip", "evil.zip"});
        assertFalse(tempDir.getParent().resolve("evil.txt").toFile().exists());
    }

    // && chaining — App.dispatch()

    private CommandRegistry buildRegistry() {
        var registry = new CommandRegistry();
        registry.register("mkdir",   new FileSystemCommands.MkdirCommand());
        registry.register("cd",      new FileSystemCommands.CdCommand());
        registry.register("touch",   new FileManipulationCommands.TouchCommand());
        registry.register("echo",    new TextCommands.EchoCommand());
        registry.register("pwd",     new FileSystemCommands.PwdCommand());
        return registry;
    }

    @Test void andChain_allSucceed_allExecute() throws Exception {
        CommandRegistry registry = buildRegistry();
        ShellContext result = App.dispatch(
            "mkdir chain && cd chain && touch marker.txt",
            ctx, registry
        );
        // cd changed directory — context is updated
        assertTrue(result.currentDirectory().getAbsolutePath().endsWith("chain"));
        assertTrue(tempDir.resolve("chain/marker.txt").toFile().exists());
    }

    @Test void andChain_firstFails_secondSkipped() throws Exception {
        CommandRegistry registry = buildRegistry();
        // "cd nonexistent" fails — "mkdir shouldnotexist" must NOT run
        App.dispatch("cd nonexistent && mkdir shouldnotexist", ctx, registry);
        assertFalse(tempDir.resolve("shouldnotexist").toFile().exists());
    }

    @Test void andChain_quotedAmpersand_notSplit() throws Exception {
        // && inside a quoted string must not be treated as a chain separator
        CommandRegistry registry = buildRegistry();
        ShellContext result = App.dispatch(
            "echo \"hello && world\" > quoted.txt",
            ctx, registry
        );
        String content = Files.readString(tempDir.resolve("quoted.txt")).trim();
        assertEquals("hello && world", content);
    }

    @Test void andChain_singleCommand_executesNormally() throws Exception {
        CommandRegistry registry = buildRegistry();
        App.dispatch("mkdir single", ctx, registry);
        assertTrue(tempDir.resolve("single").toFile().exists());
    }

    // Integration — full REPL loop

    private String runRepl(String... commands) {
        String input = String.join("\n", commands) + "\nexit\n";
        InputStream oldIn  = System.in;
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;

        ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
        ByteArrayOutputStream errCapture = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        System.setOut(new PrintStream(outCapture));
        System.setErr(new PrintStream(errCapture));

        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            App.main(new String[]{});
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
            System.setErr(oldErr);
            System.setProperty("user.dir", originalDir);
        }
        return outCapture.toString();
    }

    @Test void integration_echoAndCat() throws Exception {
        String out = runRepl("echo hello integration > itest.txt", "cat itest.txt");
        assertTrue(out.contains("hello integration"));
    }

    @Test void integration_mkdirAndCd() {
        String out = runRepl("mkdir intdir", "cd intdir", "pwd");
        assertTrue(out.contains("intdir"));
    }

    @Test void integration_andChain_endToEnd() throws Exception {
        runRepl("mkdir e2e && cd e2e && echo done > result.txt");
        assertTrue(tempDir.resolve("e2e/result.txt").toFile().exists());
    }

    @Test void integration_unknownCommand_printsError() {
        PrintStream oldErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        runRepl("notacommand");
        System.setErr(oldErr);
        assertTrue(err.toString().contains("command not found"));
    }

    @Test void integration_quotedArguments_endToEnd() throws Exception {
        String out = runRepl("echo \"spaces preserved\" > q.txt", "cat q.txt");
        assertTrue(out.contains("spaces preserved"));
    }
}
