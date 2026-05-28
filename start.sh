#!/bin/bash

# Configuration
BACKEND_PORT=8080
FRONTEND_PORT=3000

echo "=== Chess Swiss Management System Launcher ==="

# 1. Check if ports are already in use
if lsof -i :$BACKEND_PORT -t >/dev/null || lsof -i :$FRONTEND_PORT -t >/dev/null; then
  echo "⚠️  Port $BACKEND_PORT or $FRONTEND_PORT is already in use."
  echo "Please ensure these ports are free before starting."
  exit 1
fi

# 2. Prepare backend
echo "📦 Preparing backend..."
chmod +x backend/gradlew

# 3. Prepare frontend
if [ ! -d "frontend/node_modules" ]; then
  echo "📦 Installing frontend dependencies (node_modules not found)..."
  (cd frontend && npm install)
fi

# 4. Graceful Cleanup Function on Ctrl+C (SIGINT / SIGTERM)
cleanup() {
  echo -e "\n🧹 Stopping backend and frontend processes..."
  
  if [ ! -z "$BACKEND_PID" ]; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
  if [ ! -z "$FRONTEND_PID" ]; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi
  
  # Port-based fallback to clean up any lingering/orphaned child processes
  for PORT in $BACKEND_PORT $FRONTEND_PORT; do
    PIDS=$(lsof -t -i :$PORT)
    if [ ! -z "$PIDS" ]; then
      kill -9 $PIDS 2>/dev/null || true
    fi
  done
  
  echo "✅ All processes stopped."
  exit 0
}

# Register the cleanup trap
trap cleanup INT TERM

# 5. Start Backend (Streams to console)
echo "🚀 Starting Backend (Spring Boot)..."
(cd backend && ./gradlew bootRun) &
BACKEND_PID=$!

# Brief pause to let Spring Boot initialize output
sleep 2

# 6. Start Frontend (Streams to console)
echo "🚀 Starting Frontend (Vite)..."
(cd frontend && npm run dev) &
FRONTEND_PID=$!

echo "------------------------------------------------"
echo "🎉 Services are starting up!"
echo "👉 Open http://localhost:3000 in your browser."
echo "🛑 Press Ctrl+C in this terminal to stop both servers."
echo "------------------------------------------------"

# Keep script running to forward logs and wait for processes
wait
