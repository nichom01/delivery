#!/bin/bash

# Start React frontend

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT/delivery-ui"

echo "⚛️  Starting frontend UI..."

# Check if node_modules exists
if [ ! -d node_modules ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    echo "VITE_API_BASE_URL=http://localhost:8080/api/v1" > .env
fi

# Start the frontend
echo "🚀 Starting Vite development server..."
echo "   UI will be available at: http://localhost:5173"
echo "   Press Ctrl+C to stop"
echo ""

npm run dev
