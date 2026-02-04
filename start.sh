#!/bin/bash

# ==========================================
#   TRK Blockchain - Decentralized App
#   Frontend-Only Launcher (No Backend Required)
# ==========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/frontend/trk-blockchain"
CONTRACTS_DIR="$SCRIPT_DIR/contracts"

FRONTEND_PID=""
HARDHAT_PID=""

cleanup() {
    echo ""
    echo "Shutting down..."
    if [ -n "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null
    fi
    if [ -n "$HARDHAT_PID" ]; then
        kill $HARDHAT_PID 2>/dev/null
    fi
    pkill -f "ng serve" 2>/dev/null
    pkill -f "hardhat node" 2>/dev/null
    echo "Application stopped."
    exit 0
}

trap cleanup SIGINT SIGTERM

echo "=========================================="
echo "  TRK Blockchain - Decentralized dApp"
echo "=========================================="
echo ""
echo "  This is a FULLY DECENTRALIZED application."
echo "  All data is stored on Binance Smart Chain."
echo "  No backend server required!"
echo ""

# Check for --local flag to start local Hardhat node
LOCAL_MODE=false
if [ "$1" == "--local" ] || [ "$1" == "-l" ]; then
    LOCAL_MODE=true
fi

echo "[1/3] Checking prerequisites..."
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js not found"
    echo "Install with: brew install node"
    exit 1
fi
echo "  Node: $(node -v)"
echo "  npm:  $(npm -v)"
echo ""

# Optional: Start local Hardhat node for development
if [ "$LOCAL_MODE" == true ]; then
    echo "[2/3] Starting local Hardhat node..."
    cd "$CONTRACTS_DIR"

    if [ ! -d "node_modules" ]; then
        echo "  Installing contract dependencies..."
        npm install --silent --registry https://registry.npmjs.org
    fi

    # Disable telemetry prompt for background execution
    export HARDHAT_TELEMETRY_OPTOUT=1
    npx hardhat node &
    HARDHAT_PID=$!

    echo "  Waiting for Hardhat node..."
    sleep 5
    echo "  Local blockchain running on http://localhost:8545"
    echo ""
else
    echo "[2/3] Skipping local blockchain (using BSC network)"
    echo "  To use local blockchain: ./start.sh --local"
    echo ""
fi

echo "[3/3] Starting frontend..."
cd "$FRONTEND_DIR"

if [ ! -d "node_modules" ]; then
    echo "  Installing frontend dependencies..."
    npm install --silent --registry https://registry.npmjs.org
fi

echo "  Building and starting Angular app..."
npm start &
FRONTEND_PID=$!

# Wait for frontend to be ready
echo "  Waiting for frontend to start..."
for i in {1..30}; do
    if curl -s http://localhost:4200 > /dev/null 2>&1; then
        break
    fi
    sleep 2
done

echo ""
echo "=========================================="
echo "  Decentralized App is Running!"
echo "=========================================="
echo ""
echo "  Frontend: http://localhost:4200"
echo ""
if [ "$LOCAL_MODE" == true ]; then
    echo "  Local Blockchain: http://localhost:8545"
    echo ""
fi
echo "  How to use:"
echo "  1. Open http://localhost:4200 in your browser"
echo "  2. Connect your MetaMask wallet"
echo "  3. Make sure you're on BSC network"
echo ""
echo "  Press Ctrl+C to stop"
echo ""

wait
