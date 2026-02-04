@echo off
setlocal EnableDelayedExpansion

REM ==========================================
REM   TRK Blockchain - Decentralized dApp
REM   Frontend-Only Launcher (No Backend Required)
REM ==========================================

set "SCRIPT_DIR=%~dp0"
set "FRONTEND_DIR=%SCRIPT_DIR%frontend\trk-blockchain"
set "CONTRACTS_DIR=%SCRIPT_DIR%contracts"

echo ==========================================
echo   TRK Blockchain - Decentralized dApp
echo ==========================================
echo.
echo   This is a FULLY DECENTRALIZED application.
echo   All data is stored on Binance Smart Chain.
echo   No backend server required!
echo.

REM Check for --local flag
set "LOCAL_MODE=false"
if "%1"=="--local" set "LOCAL_MODE=true"
if "%1"=="-l" set "LOCAL_MODE=true"

echo [1/3] Checking prerequisites...

REM Check Node
where node >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Node.js not found
    echo Please install Node.js from: https://nodejs.org/
    exit /b 1
)

echo   Node:
node -v
echo   npm:
call npm -v
echo.

REM Optional: Start local Hardhat node for development
if "%LOCAL_MODE%"=="true" (
    echo [2/3] Starting local Hardhat node...
    cd /d "%CONTRACTS_DIR%"

    if not exist "node_modules" (
        echo   Installing contract dependencies...
        call npm install --registry https://registry.npmjs.org
    )

    REM Disable telemetry prompt
    set "HARDHAT_TELEMETRY_OPTOUT=1"
    start "TRK-Hardhat" cmd /c "set HARDHAT_TELEMETRY_OPTOUT=1 && npx hardhat node"

    echo   Waiting for Hardhat node...
    timeout /t 5 /nobreak >nul
    echo   Local blockchain running on http://localhost:8545
    echo.
) else (
    echo [2/3] Skipping local blockchain ^(using BSC network^)
    echo   To use local blockchain: start.bat --local
    echo.
)

echo [3/3] Starting frontend...
cd /d "%FRONTEND_DIR%"

if not exist "node_modules" (
    echo   Installing frontend dependencies...
    call npm install --registry https://registry.npmjs.org
)

echo   Starting Angular app...
start "TRK-Frontend" cmd /c "npm start"

timeout /t 10 /nobreak >nul
echo.
echo ==========================================
echo   Decentralized App is Running!
echo ==========================================
echo.
echo   Frontend: http://localhost:4200
echo.
if "%LOCAL_MODE%"=="true" (
    echo   Local Blockchain: http://localhost:8545
    echo.
)
echo   How to use:
echo   1. Open http://localhost:4200 in your browser
echo   2. Connect your MetaMask wallet
echo   3. Make sure you're on BSC network
echo.
echo   To stop: Close the TRK-Frontend window
if "%LOCAL_MODE%"=="true" (
    echo            Close the TRK-Hardhat window
)
echo.

endlocal
