# J-Shell

A lightweight Java-based Linux terminal emulator designed to provide Unix/Linux command-line functionality on any platform that supports Java 21.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Building the Project](#building-the-project)
- [Running J-Shell](#running-j-shell)
- [Docker Support](#docker-support)
- [Available Commands](#available-commands)
- [Command Examples](#command-examples)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Overview

J-Shell is a command-line interface (CLI) application that emulates common Linux/Unix terminal commands in a Java environment. It provides a familiar shell experience with support for file operations, text processing, network utilities, and system information commands.

The project is built using Java 21 and Maven, offering a modern, object-oriented implementation of shell functionality. It includes over 40 commands organized into logical categories for easy maintenance and extension.

For just running the application without any issues if dependencies and all other things related to developers:
    Download the J-Shell.exe file and simply run it.
    The exe file comes bundled in with a Java Runtime Environment for the application to run.

## Features

### Core Capabilities

- **File System Operations**: Navigate directories, create/delete files and folders, copy and move files
- **Text Processing**: Search, filter, sort, and manipulate text files
- **File Compression**: Create and extract ZIP archives, compress with GZIP
- **Network Utilities**: Ping hosts, download files, make HTTP requests
- **System Information**: View process details, environment variables, system specifications
- **Command History**: Track and review previously executed commands
- **Interactive Mode**: Real-time command execution with immediate feedback

### Technical Features

- Built with Java 21 (LTS)
- Maven-based build system
- Docker containerization support
- Multi-stage Docker builds for optimized image size
- Modular command architecture
- Extensible design for adding new commands
- Zero external dependencies for core functionality

## Prerequisites

### For Local Development

- Java Development Kit (JDK) 21 or higher
- Apache Maven 3.9 or higher
- Git (for version control)

### For Docker Deployment

- Docker 20.10 or higher
- Docker Compose 2.0 or higher

## Installation

### Windows Quick Start

The fastest way to get started on Windows:

1. Download or clone the repository
2. Double-click `quickstart.bat`
3. Choose Docker mode (1) or Local mode (2)
4. The script handles everything automatically

The quickstart script will:
- Detect if Docker/Java/Maven are installed
- Offer to install missing tools via Windows Package Manager (winget)
- Build the project
- Launch J-Shell

### Manual Installation

#### Clone the Repository

```bash
git clone https://github.com/yourusername/j-shell.git
cd j-shell
```

#### Verify Java Installation

```bash
java -version
# Should show Java 21 or higher

mvn -version
# Should show Maven 3.9 or higher
```

### Installing Dependencies

#### Windows (using winget)

```bash
# Install Java 21
winget install -e --id EclipseAdoptium.Temurin.21.JDK

# Install Maven
winget install -e --id Apache.Maven

# Install Docker Desktop (optional, for Docker mode)
winget install -e --id Docker.DockerDesktop
```

#### macOS (using Homebrew)

```bash
# Install Java 21
brew install openjdk@21

# Install Maven
brew install maven

# Install Docker Desktop
brew install --cask docker
```

## Building the Project

### Using Maven

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package into JAR
mvn clean package

# Skip tests during packaging
mvn clean package -DskipTests
```

The compiled JAR file will be located at `target/j-shell-1.0-SNAPSHOT.jar`

### Build Output

After a successful build, you will find:
- `target/j-shell-1.0-SNAPSHOT.jar` - Executable JAR file
- `target/classes/` - Compiled class files
- `target/test-classes/` - Compiled test classes

## Running J-Shell

### Quick Start (Windows)

The easiest way to run J-Shell on Windows is using the included batch script:

```bash
quickstart.bat
```

The script provides two options:

1. **Docker Mode**
   - Automatically installs Docker Desktop if needed
   - Builds and runs J-Shell in an isolated container
   - No need to install Java or Maven manually
   - Runs in a clean Linux environment

2. **Local Mode**
   - Automatically installs Java 21 and Maven if needed
   - Builds and runs directly on Windows
   - Better for development and debugging

The script will:
- Check if required tools are installed
- Offer to install missing dependencies via winget
- Build the project automatically
- Launch J-Shell

### From JAR File

```bash
java -jar target/j-shell-1.0-SNAPSHOT.jar
```

### From Maven

```bash
mvn exec:java -Dexec.mainClass="com.devops.App"
```

### From IDE

Run the `App.java` class directly from your IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)

### Expected Output

```
════════════════════════════════════════════════════════
  Welcome to J-Shell!                                   
  A lightweight Java-based Linux Emulator               
════════════════════════════════════════════════════════
Type 'help' for a list of commands or 'exit' to quit.

/your/current/directory > 
```

## Docker Support

### Building the Docker Image

```bash
# Build with Docker
docker build -t jshell:latest .

# Build with Docker Compose
docker-compose build
```

### Running in Docker

```bash
# Run interactively
docker run -it --rm jshell:latest

# Run with Docker Compose
docker-compose up

# Run in detached mode
docker-compose up -d

# Attach to running container
docker attach jshell-app
```

### Docker Configuration

The Docker setup includes:
- Multi-stage build (build stage with Maven, runtime stage with JRE)
- Alpine Linux base for minimal image size (approximately 250MB)
- Non-root user execution for security
- Workspace volume mounting for file operations
- Resource limits and health checks
- Custom networking configuration

See [Docker Documentation](README-Docker.md) for detailed Docker usage.

## Available Commands

### Navigation and File System

- `ls` - List directory contents
- `pwd` - Print working directory
- `cd <directory>` - Change directory
- `mkdir <directory>` - Create directory

### File Manipulation

- `touch <file>` - Create empty file or update timestamp
- `rm <file>` - Remove file or directory
- `cat <file>` - Display file contents
- `cp <source> <destination>` - Copy file (use -r for directories)
- `mv <source> <destination>` - Move or rename file

### Text Utilities

- `echo <text>` - Print text to console (supports > and >> redirection)
- `grep <pattern> <file>` - Search for pattern in file
- `wc <file>` - Count lines, words, and characters
- `head <file>` - Display first 10 lines (use -n to specify count)
- `tail <file>` - Display last 10 lines (use -n to specify count)
- `sort <file>` - Sort file contents (use -r for reverse, -n for numeric)
- `uniq <file>` - Display unique lines (use -c to count occurrences)
- `diff <file1> <file2>` - Compare two files

### Search and Find

- `find <pattern>` - Find files by name (use -r for recursive search)
- `du <path>` - Display disk usage (use -h for human-readable format)

### Compression

- `zip <output.zip> <files>` - Create ZIP archive (use -r for directories)
- `unzip <file.zip>` - Extract ZIP archive
- `gzip <file>` - Compress file with GZIP
- `gunzip <file.gz>` - Decompress GZIP file

### Network

- `ping <host>` - Ping a host (optionally specify packet count)
- `wget <url>` - Download file from URL
- `curl <url>` - Make HTTP request (use -o to save to file)
- `ifconfig` - Show network interfaces and IP addresses

### System and Process

- `ps` - Show Java process information (use -v for verbose thread info)
- `exec <command>` - Execute system command
- `env [variable]` - Show environment variables
- `uname` - Display system information

### Utility

- `checksum <file>` - Calculate file checksum (supports -md5, -sha256)
- `history` - Show command history
- `whoami` - Display current user
- `date` - Show current date and time
- `clear` - Clear the console screen
- `help` - Display available commands
- `exit` - Exit J-Shell

## Command Examples

### Basic File Operations

```bash
# Create and navigate directories
mkdir projects
cd projects
pwd

# Create and view files
touch readme.txt
echo Hello World > readme.txt
cat readme.txt

# Copy and move files
cp readme.txt backup.txt
mv backup.txt ../backup.txt
```

### Text Processing

```bash
# Search in files
grep "Hello" readme.txt

# Count words
wc readme.txt
wc -l readme.txt

# Sort and unique
sort data.txt
sort -r data.txt
uniq -c data.txt

# View file portions
head -n 5 logfile.txt
tail -n 10 logfile.txt
```

### File Compression

```bash
# Create archives
zip backup.zip file1.txt file2.txt
zip -r project.zip src/

# Extract archives
unzip backup.zip
unzip backup.zip destination/

# GZIP compression
gzip largefile.txt
gunzip largefile.txt.gz
```

### Network Operations

```bash
# Test connectivity
ping google.com
ping 8.8.8.8 5

# Download files
wget https://example.com/file.pdf
wget https://example.com/file.pdf custom-name.pdf

# HTTP requests
curl https://api.example.com
curl -o response.html https://example.com
```

### System Information

```bash
# Process information
ps
ps -v

# Environment
env
env PATH
env HOME

# System details
uname
whoami
date
```

### Advanced Usage

```bash
# File comparison
diff version1.txt version2.txt

# Find files
find "config"
find "*.txt" -r

# Disk usage
du .
du -h projects/

# Checksums
checksum important.zip
checksum -sha256 document.pdf
```

## Project Structure

```
j-shell/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── devops/
│                   ├── App.java                     # Main application entry point
│                   ├── Command.java                 # Command interface
│                   ├── CommandRegistry.java         # Command registration system
│                   ├── FileSystemCommands.java      # ls, pwd, cd, mkdir
│                   ├── FileManipulationCommands.java # touch, rm, cat
│                   ├── TextCommands.java            # echo, grep, help
│                   ├── AdvancedFileCommands.java    # cp, mv
│                   ├── SystemCommands.java          # history, whoami, date, clear
│                   ├── SearchCommands.java          # find, wc, diff
│                   ├── CompressionCommands.java     # zip, unzip, gzip, gunzip
│                   ├── NetworkCommands.java         # ping, wget, curl, ifconfig
│                   ├── ProcessCommands.java         # ps, exec, env, uname
│                   └── UtilityCommands.java         # sort, uniq, checksum, du, head, tail
├── target/                                          # Maven build output
├── quickstart.bat                                   # Windows quickstart script
├── Dockerfile                                       # Docker build configuration
├── Readme-docker.md                                 # Docker documentation for this project
├── docker-compose.yml                               # Docker Compose configuration
├── .dockerignore                                    # Docker ignore patterns
├── pom.xml                                          # Maven project configuration
└── README.md                                        # This file
```

## Development

### Adding New Commands

1. Create a new command class implementing the `Command` interface:

```java
public class MyCommand implements Command {
    @Override
    public void execute(String[] args) {
        // Implementation
    }
}
```

2. Register the command in `App.java`:

```java
registry.register("mycommand", new MyCommands.MyCommand());
```

3. Update the help text in `TextCommands.java`

### Code Organization

Commands are organized into logical groups:

- **FileSystemCommands**: Directory navigation and creation
- **FileManipulationCommands**: File CRUD operations
- **TextCommands**: Text processing and utilities
- **AdvancedFileCommands**: Complex file operations
- **SystemCommands**: System information and control
- **SearchCommands**: Search and comparison utilities
- **CompressionCommands**: Archive creation and extraction
- **NetworkCommands**: Network-related operations
- **ProcessCommands**: Process and environment management
- **UtilityCommands**: Miscellaneous utility commands

### Design Patterns Used

- **Command Pattern**: Each command is a separate class implementing the Command interface
- **Registry Pattern**: CommandRegistry manages command lookup and execution
- **Factory Pattern**: Commands are instantiated and registered at startup
- **Singleton Pattern**: App maintains single instances of current directory and history

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CommandTest
```

### Writing Tests

Tests use JUnit 5. Example:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
    @Test
    void testCommand() {
        // Test implementation
    }
}
```

## Configuration

### Memory Settings

Adjust JVM memory settings when running:

```bash
# Local execution
java -Xmx1g -Xms512m -jar target/j-shell-1.0-SNAPSHOT.jar

# Docker execution
docker run -e JAVA_OPTS="-Xmx1g -Xms512m" -it jshell:latest
```

### Docker Resource Limits

Edit `docker-compose.yml`:

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 1024M
```

## Troubleshooting

### Common Issues

**Issue: Command not found**
- Verify the command is registered in `App.java`
- Check spelling and case sensitivity
- Type `help` to see available commands

**Issue: File not found errors**
- Use absolute paths or verify current directory with `pwd`
- Check file permissions
- Ensure file exists with `ls`

**Issue: Permission denied**
- Run with appropriate user permissions
- Check file ownership and permissions
- In Docker, verify volume mount permissions

**Issue: Out of memory**
- Increase JVM heap size with `-Xmx` flag
- Check for memory leaks in custom commands
- Monitor with `ps` command

**Issue: Docker container exits immediately**
- Use `-it` flags for interactive mode
- Check logs with `docker logs jshell-app`
- Verify image built successfully

### Getting Help

- Check command usage: `<command> --help` or `<command>`
- View command history: `history`
- Display all commands: `help`
- Check Docker logs: `docker logs jshell-app`

## Performance

### Benchmarks

- Startup time: Less than 1 second
- Command execution: Instant for most operations
- File operations: Dependent on file size and system I/O
- Network operations: Dependent on network speed

### Optimization Tips

- Use Docker layer caching for faster builds
- Minimize file operations in loops
- Use appropriate buffer sizes for large files
- Consider async operations for network commands

## Security Considerations

### Current Security Measures

- Non-root user execution in Docker
- No shell injection vulnerabilities (commands are parsed, not executed in shell)
- Resource limits configured
- Minimal base image (Alpine Linux)
- No unnecessary packages or tools

### Best Practices

- Validate all user input
- Sanitize file paths to prevent directory traversal
- Use secure network connections (HTTPS) for downloads
- Limit file operation scope to designated directories
- Regular security updates to base images and dependencies

## Roadmap

### Planned Features

- Tab completion for commands and file paths
- Command aliases
- Configuration file support
- Scripting support (batch command execution)
- Plugin architecture for third-party commands
- Web-based interface
- Remote shell capabilities
- Enhanced error messages and help system

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-command`)
3. Commit your changes (`git commit -am 'Add new command'`)
4. Push to the branch (`git push origin feature/new-command`)
5. Create a Pull Request

### Contribution Guidelines

- Follow existing code style and conventions
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass before submitting
- Write clear commit messages

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by Unix/Linux shell commands
- Built with Java 21 and Maven
- Uses Docker for containerization
- Community contributions and feedback

## Contact

For questions, issues, or suggestions:

- Open an issue on GitHub
- Submit a pull request

## Version History

### Version 1.0.0 (Current)

- Initial release
- 25+ commands implemented
- Docker support
- Maven build system
- Comprehensive documentation

---

Built with Java 21 | Powered by Maven | Containerized with Docker