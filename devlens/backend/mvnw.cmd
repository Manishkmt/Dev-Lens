@echo off
REM DevLens Maven Wrapper Batch Script

REM Prefer JDK 21 (LTS) for compatibility with Spring Boot 3 & Lombok
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javac.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin;%PATH%"
)

REM Check if mvn is directly available in PATH
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

REM Fallback to installed Maven / mvnd directory
set "MVN_EXE=C:\Program Files\maven-mvnd-1.0.3-windows-amd64\mvn\bin\mvn.cmd"
if exist "%MVN_EXE%" (
    call "%MVN_EXE%" %*
    exit /b %ERRORLEVEL%
)

echo [ERROR] Maven not found.
exit /b 1
