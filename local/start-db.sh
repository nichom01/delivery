#!/bin/bash

# Start PostgreSQL database container

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

echo "🐘 Starting PostgreSQL database..."

# Check if docker-compose.yml exists
if [ ! -f docker-compose.yml ]; then
    echo "❌ docker-compose.yml not found in project root"
    exit 1
fi

# Start postgres service
if command -v docker-compose &> /dev/null; then
    docker-compose up -d postgres
else
    docker compose up -d postgres
fi

echo "⏳ Waiting for database to be ready..."
sleep 3

# Check if container is running
if docker ps | grep -q delivery-postgres; then
    echo "✅ Database is running on port 5432"
    echo "   Connection: postgresql://postgres:postgres@localhost:5432/deliverysystem"
else
    echo "❌ Database container failed to start"
    exit 1
fi
