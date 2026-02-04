@echo off
REM ==========================================
REM   TRK Blockchain Application Stopper
REM   Windows Version
REM ==========================================

echo ==========================================
echo   Stopping TRK Blockchain Application
echo ==========================================
echo.

echo Stopping frontend (Angular/Node)...
taskkill /F /FI "WINDOWTITLE eq TRK-Frontend*" >nul 2>&1
taskkill /F /IM "node.exe" /FI "WINDOWTITLE eq TRK-Frontend*" >nul 2>&1

REM Kill any ng serve processes
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":4200" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
echo   Frontend stopped.

echo Stopping backend (Spring Boot/Java)...
taskkill /F /FI "WINDOWTITLE eq TRK-Backend*" >nul 2>&1

REM Kill any process on port 8080
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
)
echo   Backend stopped.

echo.
echo ==========================================
echo   Application stopped successfully!
echo ==========================================
