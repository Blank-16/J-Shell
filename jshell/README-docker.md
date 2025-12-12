# J-Shell Docker Setup

## 📁 Project Structure

```
j-shell/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── devops/
                    ├── App.java
                    ├── Command.java
                    ├── CommandRegistry.java
                    ├── FileSystemCommands.java
                    ├── FileManipulationCommands.java
                    ├── TextCommands.java
                    ├── AdvancedFileCommands.java
                    ├── SystemCommands.java
                    ├── SearchCommands.java
                    ├── CompressionCommands.java
                    ├── NetworkCommands.java
                    ├── ProcessCommands.java
                    └── UtilityCommands.java
.gitignore
README.md
quickStart.bat
```

##  Quick Start

### Option 1: Using Docker directly

**Build the image:**
```bash
docker build -t jshell:latest .
```

**Run the container (interactive mode):**
```bash
docker run -it --rm jshell:latest
```

**Run with mounted workspace:**
```Cmd
docker run -it --rm -v %cd%/workspace:/app/workspace jshell:latest
```
```PowerShell
docker run -it --rm -v ${PWD}/workspace:/app/workspace jshell:latest
```

### Option 2: Using Docker Compose

**Build and run:**
```bash
docker-compose up --build
```

**Run in detached mode:**
```bash
docker-compose up -d
```

**Attach to running container:**
```bash
docker attach jshell-app
```

**Stop the container:**
```bash
docker-compose down
```

##  Build Locally First (Without Docker)

If you want to test locally before Docker:

```bash
# Compile
mvn clean compile

# Package
mvn clean package

# Run
java -jar target/j-shell-1.0.0.jar
```

##  Docker Commands Reference

### Build Commands
```bash
# Build with custom tag
docker build -t jshell:v1.0 .

# Build with no cache
docker build --no-cache -t jshell:latest .
```

### Run Commands
```bash
# Interactive mode
docker run -it jshell:latest

# With workspace directory
docker run -it -v $(pwd)/workspace:/app/workspace jshell:latest

# With custom memory settings
docker run -it -e JAVA_OPTS="-Xmx1g" jshell:latest

# Run and remove container after exit
docker run -it --rm jshell:latest
```

### Management Commands
```bash
# List images
docker images | grep jshell

# Remove image
docker rmi jshell:latest

# List running containers
docker ps

# Stop container
docker stop jshell-app

# Remove stopped containers
docker container prune
```

##  Troubleshooting

### Issue: Container exits immediately
**Solution:** Make sure you use `-it` flags for interactive mode
```bash
docker run -it jshell:latest
```

### Issue: Build fails - dependencies not found
**Solution:** Clear Maven cache and rebuild
```bash
docker build --no-cache -t jshell:latest .
```

### Issue: Can't access files in container
**Solution:** Mount a volume
```bash
docker run -it -v $(pwd)/workspace:/app/workspace jshell:latest
```

### Issue: Permission denied
**Solution:** Run with user privileges or adjust permissions
```bash
# On Linux/Mac
docker run -it --user $(id -u):$(id -g) jshell:latest
```

## Image Size Optimization

The Dockerfile uses multi-stage build to minimize image size:
- **Build stage:** Uses full Maven + JDK (~800MB)
- **Runtime stage:** Uses slim JRE (~200MB)
- **Final image:** ~250MB

To check image size:
```bash
docker images jshell:latest
```

## Networking (Optional)

For adding a web interface later (if added): 

```yaml
# In docker-compose.yml
ports:
  - "8080:8080"
```

Then access at: `http://localhost:8080`

## Security Best Practices

The Dockerfile includes:
- ✅ Non-root user (`jshell`)
- ✅ Multi-stage build (smaller attack surface)
- ✅ Alpine base image (minimal packages)
- ✅ No unnecessary tools in runtime

##  Notes

- The container runs with a non-root user for security
- Workspace directory (`/app/workspace`) is available for file operations
- Network commands (ping, wget, curl) work within the container
- System commands (exec) are limited to what's available in Alpine Linux

##  Next Steps

1. Create the `workspace/` directory for shared files
2. Build and test the Docker image
3. Try the commands inside the container
4. Customize the Dockerfile for your needs

##  Tips

- Use `.dockerignore` to speed up builds
- Mount volumes for persistent data
- Use `docker-compose` for easier management
- Tag images with version numbers for production