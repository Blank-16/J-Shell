<div align="center">

# J-Shell

**A POSIX-inspired shell emulator built entirely in Java 21**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=flat&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-27%20passing-brightgreen?style=flat)]()

37 commands · No native dependencies · Runs entirely on the JVM

</div>

---

## Overview

J-Shell is a shell emulator that implements a Unix-like command experience inside the JVM. It covers filesystem navigation, text processing, compression, networking, and process inspection — with no OS-level shell or native binaries required for core functionality.

Built with a clean command pattern, injected context, and quote-aware tokenization, it handles real-world input like `echo "hello world" > file.txt` and `grep "foo\s+\d+" log.txt` correctly.

---

## Quick Start

```bash
# Build
mvn clean package -q

# Run
java -jar target/j-shell-1.0.0.jar
```

```
Welcome to J-Shell — type 'help' or 'exit'.

/home/user/projects > ls
/home/user/projects > mkdir -p src/main/java
/home/user/projects > echo "hello world" > greeting.txt
/home/user/projects > grep "hello" greeting.txt
hello world
/home/user/projects > exit
Goodbye!
```

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 21+     |
| Maven | 3.8+   |

---

## Commands

### Filesystem

| Command | Usage | Description |
|---------|-------|-------------|
| `ls` | `ls` | List files and directories with type and size |
| `pwd` | `pwd` | Print current working directory |
| `cd` | `cd [directory]` | Change directory — supports `~`, `..`, relative and absolute paths |
| `mkdir` | `mkdir [-p] <dir>` | Create directory; `-p` creates nested parents |
| `touch` | `touch <file>` | Create file or update modification time |
| `rm` | `rm [-r] <target>` | Remove file or directory; `-r` for recursive |
| `cp` | `cp [-r] <src> <dest>` | Copy file or directory; `-r` for recursive |
| `mv` | `mv <src> <dest>` | Move or rename |
| `cat` | `cat <file>` | Print file contents (streamed, safe on large files) |
| `find` | `find <pattern> [-r]` | Search filenames by substring; `-r` recurses |
| `du` | `du [-h] [path]` | Show disk usage; `-h` human-readable |

### Text Processing

| Command | Usage | Description |
|---------|-------|-------------|
| `echo` | `echo <text> [> file] [>> file]` | Print text with redirect (`>`) and append (`>>`) support |
| `grep` | `grep [-i] <pattern> <file>` | Search with full regex; `-i` case-insensitive |
| `wc` | `wc [-l\|-w\|-c] <file>` | Count lines, words, or characters |
| `diff` | `diff <file1> <file2>` | Compare two files line by line |
| `sort` | `sort [-r] [-n] <file>` | Sort lines; `-r` reverse, `-n` numeric |
| `uniq` | `uniq [-c] <file>` | Remove adjacent duplicate lines; `-c` shows count |
| `head` | `head [-n count] <file>` | Print first N lines (default 10) |
| `tail` | `tail [-n count] <file>` | Print last N lines (default 10) |

### Compression

| Command | Usage | Description |
|---------|-------|-------------|
| `zip` | `zip [-r] <output.zip> <files...>` | Create zip archive; `-r` includes directories |
| `unzip` | `unzip <file.zip> [dest]` | Extract zip archive (zip slip protected) |
| `gzip` | `gzip <file>` | Compress file to `<file>.gz` |
| `gunzip` | `gunzip <file.gz>` | Decompress `.gz` file |

### Networking

| Command | Usage | Description |
|---------|-------|-------------|
| `ping` | `ping <host> [count]` | Ping a host (see limitations) |
| `wget` | `wget <url> [filename]` | Download file with progress display |
| `curl` | `curl [-o <file>] <url>` | Fetch URL — print or save response |
| `ifconfig` | `ifconfig` | List network interfaces and addresses |

### System & Process

| Command | Usage | Description |
|---------|-------|-------------|
| `ps` | `ps [-v]` | Show JVM process info; `-v` lists all threads |
| `exec` | `exec <command> [args...]` | Run an external system command |
| `env` | `env [variable]` | Show all environment variables or look up one |
| `uname` | `uname` | Print OS, Java version, CPU count, memory |
| `whoami` | `whoami` | Print current OS username |
| `date` | `date` | Print current date and time (RFC-1123) |
| `history` | `history` | Show numbered command history for this session |
| `clear` | `clear` | Clear the terminal screen |
| `help` | `help` | List all available commands |
| `checksum` | `checksum [-md5\|-sha1\|-sha256] <file>` | Compute file hash (default SHA-256) |

---

## Architecture

```
App
├── ShellContext          Mutable session state (cwd, history) — injected, never global
├── Tokenizer             Quote-aware input splitter ("hello world" → single token)
├── CommandRegistry       String → Command map with Optional<Command> lookup
├── Command (interface)   int execute(ShellContext, String[])
│   ├── FileSystemCommands        ls, pwd, cd, mkdir, touch, rm, cp, mv, cat, find, du
│   ├── FileManipulationCommands  touch, rm, cat
│   ├── TextCommands              echo, grep, help
│   ├── AdvancedFileCommands      cp, mv
│   ├── SystemCommands            history, whoami, date, clear
│   ├── SearchCommands            find, wc, diff
│   ├── CompressionCommands       zip, unzip, gzip, gunzip
│   ├── NetworkCommands           ping, wget, curl, ifconfig
│   ├── ProcessCommands           ps, exec, env, uname
│   └── UtilityCommands           sort, uniq, checksum, du, head, tail
└── ByteFormatter         Shared human-readable byte size formatting
```

Each command group is a `final` class with a `private` constructor — used purely as a namespace for `static final` inner command classes. No shared mutable state between commands.

---

## Design Highlights

**No global state** — all session state lives in `ShellContext`, which is constructed once and injected into every command call. Commands are fully isolated and testable without touching `App`.

**Quote-aware tokenizer** — a character state machine correctly handles `echo "hello world" > out.txt` as three tokens, not five.

**Streaming I/O** — `cat`, `grep`, `wc`, `head`, and `tail` all use `BufferedReader` and never load an entire file into memory. `tail` uses a fixed-size ring buffer, making it O(N) in memory for any file size.

**Zip slip protection** — `unzip` validates every entry's canonical path before writing, blocking path traversal attacks like `../../etc/cron.d/evil`.

**Registry-driven help** — `help` output is generated at runtime from the registry. Adding a command and implementing `usage()` is sufficient — the help list never drifts.

---

## Testing

```bash
mvn test
```

27 unit tests covering:

- Tokenizer quote handling (double and single quotes)
- `cd` directory resolution and error on missing path
- `mkdir -p` nested directory creation
- `echo` file redirect and append
- `grep` regex matching, case-insensitive flag, and no-match exit code
- `rm` recursive directory deletion and missing `-r` guard
- `head` / `tail` streaming correctness
- `uniq` adjacent-only deduplication (POSIX semantics)
- `checksum` MD5 output
- `history` immutability (unmodifiable list)
- Zip slip path traversal blocking

---

## Known Limitations

**`ping`** — uses `InetAddress.isReachable()`, which on Linux without root falls back to TCP port 7 (echo), universally blocked. Reachable hosts may appear to time out. Workaround: `exec ping <host>` delegates to the OS binary.

**`diff`** — line-number alignment comparison, not LCS. Inserting a line in one file offsets all subsequent line comparisons. Accurate diff requires Myers' algorithm.

**`clear`** — ANSI escape codes. Works on Linux, macOS, and Windows Terminal. Does not work on legacy Windows `cmd.exe`.

**`exec`** — no sandboxing. Runs arbitrary commands as the current JVM user. Intentional for local use; do not expose over a network interface.

---

## Project Structure

```
jshell/
├── src/
│   ├── main/java/com/devops/
│   │   ├── App.java
│   │   ├── ShellContext.java
│   │   ├── Command.java
│   │   ├── CommandRegistry.java
│   │   ├── Tokenizer.java
│   │   ├── ByteFormatter.java
│   │   ├── FileSystemCommands.java
│   │   ├── FileManipulationCommands.java
│   │   ├── TextCommands.java
│   │   ├── AdvancedFileCommands.java
│   │   ├── SystemCommands.java
│   │   ├── SearchCommands.java
│   │   ├── CompressionCommands.java
│   │   ├── NetworkCommands.java
│   │   ├── ProcessCommands.java
│   │   └── UtilityCommands.java
│   └── test/java/com/devops/
│       └── AppTest.java
└── pom.xml
```

---

## Contributing

1. Each command belongs in its corresponding `*Commands.java` file as a `static final` inner class implementing `Command`.
2. Register it in `App.registerCommands()`.
3. Implement `name()` and `usage()` — the command will appear in `help` automatically.
4. Add a test in `AppTest.java` using a `@TempDir`-isolated `ShellContext`.

---

<div align="center">

Built with Java 21 · No runtime dependencies · 27 tests passing

</div>
