#!/bin/bash

# Start all components (database, backend, frontend)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🚀 Starting Delivery System..."
echo ""

# Start database
echo "1️⃣  Starting database..."
"$SCRIPT_DIR/start-db.sh"
echo ""

# Wait a bit for database to be ready
sleep 2

# Start backend in background
echo "2️⃣  Starting backend..."
cd "$PROJECT_ROOT"
"$SCRIPT_DIR/start-backend.sh" &
BACKEND_PID=$!

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 10

# Check if backend is responding
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1 || curl -s http://localhost:8080/api/v1/auth/login > /dev/null 2>&1; then
        echo "✅ Backend is running"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "⚠️  Backend may not be ready yet, but continuing..."
    fi
    sleep 1
done

echo ""
echo "3️⃣  Starting frontend..."
cd "$PROJECT_ROOT"
"$SCRIPT_DIR/start-frontend.sh" &
FRONTEND_PID=$!

echo ""
echo "✨ All components are starting!"
echo ""
echo "📊 Status:"
echo "   Database: http://localhost:5432"
echo "   Backend:  http://localhost:8080"
echo "   Frontend: http://localhost:5173"
echo ""
echo "Press Ctrl+C to stop all components"

# Trap Ctrl+C to stop all processes
trap "echo ''; echo '🛑 Stopping all components...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit" INT

# Wait for processes
wait
