@echo off
setlocal EnableDelayedExpansion

REM ==========================================
REM   TRK Blockchain Application Launcher
REM   Windows Version
REM ==========================================

REM Configure JAVA_HOME - Update this path for your system
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
)

set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%backend"
set "FRONTEND_DIR=%SCRIPT_DIR%frontend\trk-blockchain"

echo ==========================================
echo   TRK Blockchain Application Launcher
echo ==========================================
echo.

echo [1/4] Checking prerequisites...

REM Check Java
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Java 21 not found at %JAVA_HOME%
    echo Please install Java 21 and set JAVA_HOME environment variable
    echo Download from: https://adoptium.net/
    exit /b 1
)

REM Check Node
where node >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Node.js not found
    echo Please install Node.js from: https://nodejs.org/
    exit /b 1
)

echo   Java:
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr /i "version"
echo   Node:
node -v
echo.

echo [2/4] Starting backend server...
cd /d "%BACKEND_DIR%"

REM Start backend in a new window
start "TRK-Backend" cmd /c "mvnw.cmd spring-boot:run"

echo   Waiting for backend to start...
set "RETRIES=0"
:wait_backend
timeout /t 2 /nobreak >nul
curl -s http://localhost:8080/api/auth/login >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo   Backend started on http://localhost:8080
    goto backend_started
)
set /a RETRIES+=1
if %RETRIES% geq 30 (
    echo ERROR: Backend failed to start within 60 seconds
    echo Check the backend window for errors
    exit /b 1
)
goto wait_backend

:backend_started
echo.

echo [3/4] Installing frontend dependencies...
cd /d "%FRONTEND_DIR%"
if not exist "node_modules" (
    echo   Installing npm packages...
    call npm install
)
echo.

echo [4/4] Starting frontend server...
start "TRK-Frontend" cmd /c "npm start"

timeout /t 5 /nobreak >nul
echo.
echo ==========================================
echo   Application is running!
echo ==========================================
echo.
echo   Frontend: http://localhost:4200
echo   Backend:  http://localhost:8080
echo   H2 Console: http://localhost:8080/h2-console
echo.
echo   To stop, run: stop.bat
echo   Or close the Backend and Frontend windows
echo.

endlocal
