#!/bin/bash

# Reset database (removes all data)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

echo "🔄 Resetting database..."

# Check if docker-compose.yml exists
if [ ! -f docker-compose.yml ]; then
    echo "❌ docker-compose.yml not found"
    exit 1
fi

# Stop and remove container and volumes
if command -v docker-compose &> /dev/null; then
    docker-compose down -v
else
    docker compose down -v
fi

echo "✅ Database reset complete"
echo ""
echo "To start fresh:"
echo "  ./local/start-db.sh"
echo "  ./local/start-backend.sh  (will recreate schema via Flyway)"
