@echo off
setlocal EnableDelayedExpansion
title Cyber Crime Report and Resolution System (CCRRS)
cls
echo ====================================================================
echo        Cyber Crime Report and Resolution System (CCRRS)
echo   Connected Fullstack System: Web Frontend + Java 17 DSA Backend
echo ====================================================================

:: Locate Java compiler and runtime
set "JDK_BIN=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin"
if not exist "%JDK_BIN%\javac.exe" (
    where javac >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] JDK 17 javac.exe not found! Please check Java installation.
        pause
        exit /b 1
    ) else (
        set "JAVAC_CMD=javac"
        set "JAVA_CMD=java"
    )
) else (
    set "JAVAC_CMD=%JDK_BIN%\javac.exe"
    set "JAVA_CMD=%JDK_BIN%\java.exe"
)

echo [*] Compiling Java backend classes...
if not exist "bin" mkdir "bin"

set "SOURCES="
for /r src %%F in (*.java) do (
    set "SOURCES=!SOURCES! "%%F""
)
"%JAVAC_CMD%" -d bin !SOURCES!

if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [+] Compilation successful!
echo.
echo ====================================================================
echo Choose Run Mode:
echo   [1] Launch Fullstack Web Dashboard (Opens in your Browser at http://localhost:8080)
echo   [2] Launch Interactive Console CLI Dashboard
echo   [3] Run Automated Verification Test Suite (43 Tests)
echo ====================================================================
set /p MODE="Enter choice [1, 2, or 3] (Default is 1): "

if "%MODE%"=="2" (
    "%JAVA_CMD%" -cp bin ccrrs.Main --cli
) else if "%MODE%"=="3" (
    "%JAVA_CMD%" -cp bin ccrrs.demo.CCRRSVerifier
) else (
    echo [*] Starting CCRRS Web Server on http://localhost:8080 ...
    "%JAVA_CMD%" -cp bin ccrrs.Main
)

pause
