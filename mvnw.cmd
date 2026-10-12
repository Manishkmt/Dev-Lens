@echo off
REM DevLens Root Maven Wrapper Batch Script

if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javac.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin;%PATH%"
)

where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    mvn -f devlens\backend\pom.xml %*
    exit /b %ERRORLEVEL%
)

set "MVN_EXE=C:\Program Files\maven-mvnd-1.0.3-windows-amd64\mvn\bin\mvn.cmd"
if exist "%MVN_EXE%" (
    call "%MVN_EXE%" -f devlens\backend\pom.xml %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] Maven not found.
exit /b 1
