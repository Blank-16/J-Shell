@echo off
setlocal

echo ===================================================
echo      J-Shell Quickstart & Environment Setup
echo ===================================================
echo.
echo [1] Run inside Docker (Recommended for users)
echo     - No need to install Java or Maven on your computer.
echo     - Runs in an isolated, clean Linux container.
echo.
echo [2] Run locally on Windows (Recommended for developers)
echo     - Requires Java 21 and Maven installed.
echo     - Runs directly on your host OS.
echo.

set /p choice="Select an option (1 or 2): "

if "%choice%"=="1" goto docker_mode
if "%choice%"=="2" goto local_mode
echo Invalid choice. Exiting.
goto :eof

:: MODE 1: DOCKER SETUP (Skips Java/Maven checks)
:docker_mode
echo.
echo [MODE] Switched to Docker Container Mode.

:: --- CHECK DOCKER ---
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [MISSING] Docker not found.
    set /p install_docker="Would you like to install Docker Desktop? (y/n): "
    if /i "%install_docker%"=="y" (
        echo Installing Docker Desktop...
        winget install -e --id Docker.DockerDesktop
        echo [IMPORTANT] Docker installation requires a system restart.
        echo Please restart your computer and run this script again.
        pause
        exit /b
    ) else (
        echo Cannot run in Docker mode without Docker installed. Exiting.
        goto :eof
    )
) else (
    echo [OK] Docker is installed.
)

:: --- CHECK DAEMON ---
echo Checking if Docker Engine is running...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker Desktop is not running. Attempting to start...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo Waiting for Docker to start (this may take up to 60s)...
    timeout /t 20 >nul
)

:: --- BUILD AND RUN ---
if not exist Dockerfile (
    echo [ERROR] Dockerfile not found in current directory!
    pause
    exit /b
)

echo.
echo Building Docker Image...
docker build -t jshell .

echo.
echo Starting J-Shell Container...
echo ---------------------------------------------------
echo Note: You are now inside the Linux container.
echo Files created here will disappear when you exit 
echo (unless you mount a volume).
echo ---------------------------------------------------
docker run -it --rm --name jshell-instance jshell
goto :end


:: MODE 2: LOCAL SETUP (Checks for Java/Maven)
:local_mode
echo.
echo [MODE] Switched to Local Windows Mode.

:: --- CHECK JAVA ---
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [MISSING] Java JDK not found.
    set /p install_java="Would you like to install Java JDK 21? (y/n): "
    if /i "%install_java%"=="y" (
        echo Installing Eclipse Temurin JDK 21...
        winget install -e --id EclipseAdoptium.Temurin.21.JDK
        echo [NOTE] You may need to restart your terminal to refresh PATH.
    )
) else (
    echo [OK] Java is installed.
)

:: --- CHECK MAVEN ---
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [MISSING] Maven not found.
    set /p install_mvn="Would you like to install Apache Maven? (y/n): "
    if /i "%install_mvn%"=="y" (
        echo Installing Apache Maven...
        winget install -e --id Apache.Maven
        echo [NOTE] You may need to restart your terminal to refresh PATH.
    )
) else (
    echo [OK] Maven is installed.
)

:: --- BUILD AND RUN ---
echo.
echo Building project locally...
call mvn clean package -DskipTests

echo.
echo Starting J-Shell on Windows...
echo ---------------------------------------------------
java -jar target/j-shell-1.0-SNAPSHOT.jar
goto :end

:end
echo.
echo Session finished.
pause