#!/bin/bash

export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_DIR="$SCRIPT_DIR/frontend/trk-blockchain"

BACKEND_PID=""
FRONTEND_PID=""

cleanup() {
    echo ""
    echo "Shutting down..."
    if [ -n "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null
    fi
    if [ -n "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null
    fi
    pkill -f "spring-boot:run" 2>/dev/null
    pkill -f "ng serve" 2>/dev/null
    echo "Application stopped."
    exit 0
}

trap cleanup SIGINT SIGTERM

echo "=========================================="
echo "  TRK Blockchain Application Launcher"
echo "=========================================="
echo ""

echo "[1/4] Checking prerequisites..."
if [ ! -d "$JAVA_HOME" ]; then
    echo "ERROR: Java 21 not found at $JAVA_HOME"
    echo "Install with: brew install openjdk@21"
    exit 1
fi

if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js not found"
    echo "Install with: brew install node"
    exit 1
fi

echo "  Java: $($JAVA_HOME/bin/java -version 2>&1 | head -1)"
echo "  Node: $(node -v)"
echo ""

echo "[2/4] Starting backend server..."
cd "$BACKEND_DIR"
./mvnw spring-boot:run -q &
BACKEND_PID=$!

echo "  Waiting for backend to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/auth/login > /dev/null 2>&1; then
        echo "  Backend started on http://localhost:8080"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "ERROR: Backend failed to start"
        cleanup
        exit 1
    fi
    sleep 2
done
echo ""

echo "[3/4] Installing frontend dependencies..."
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ]; then
    npm install --silent
fi
echo ""

echo "[4/4] Starting frontend server..."
npm start &
FRONTEND_PID=$!

sleep 5
echo ""
echo "=========================================="
echo "  Application is running!"
echo "=========================================="
echo ""
echo "  Frontend: http://localhost:4200"
echo "  Backend:  http://localhost:8080"
echo "  H2 Console: http://localhost:8080/h2-console"
echo ""
echo "  Press Ctrl+C to stop"
echo ""

wait
