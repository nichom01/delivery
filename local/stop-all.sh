#!/bin/bash

# Stop all components

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🛑 Stopping Delivery System components..."

# Stop frontend (Vite dev server on port 5173)
if lsof -ti:5173 > /dev/null 2>&1; then
    echo "Stopping frontend..."
    lsof -ti:5173 | xargs kill -9 2>/dev/null || true
    echo "✅ Frontend stopped"
fi

# Stop backend (Spring Boot on port 8080)
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "Stopping backend..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null || true
    echo "✅ Backend stopped"
fi

# Stop database container
cd "$PROJECT_ROOT"
if [ -f docker-compose.yml ]; then
    echo "Stopping database..."
    if command -v docker-compose &> /dev/null; then
        docker-compose down 2>/dev/null || true
    else
        docker compose down 2>/dev/null || true
    fi
    echo "✅ Database stopped"
fi

echo ""
echo "✨ All components stopped"
