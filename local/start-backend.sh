#!/bin/bash

# Start Spring Boot backend API

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT/delivery-api"

echo "🔧 Starting backend API..."

# Set Java 21 on macOS if available
if [[ "$OSTYPE" == "darwin"* ]]; then
    if [ -f /usr/libexec/java_home ]; then
        JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")
        if [ -n "$JAVA_21_HOME" ]; then
            export JAVA_HOME="$JAVA_21_HOME"
            echo "✅ Using Java 21"
        fi
    fi
fi

# Verify Java version
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed"
    exit 1
fi

# Check if database is running
if ! docker ps | grep -q delivery-postgres; then
    echo "⚠️  Database container is not running. Starting it..."
    cd "$PROJECT_ROOT"
    if [ -f docker-compose.yml ]; then
        if command -v docker-compose &> /dev/null; then
            docker-compose up -d postgres
        else
            docker compose up -d postgres
        fi
        sleep 3
    else
        echo "❌ docker-compose.yml not found. Please start the database first."
        exit 1
    fi
    cd "$PROJECT_ROOT/delivery-api"
fi

# Build if needed
if [ ! -d target ] || [ ! -f target/delivery-api-*.jar ]; then
    echo "📦 Building backend..."
    mvn clean install -DskipTests
fi

# Avoid a confusing Maven failure when another API instance already owns 8080
if command -v lsof >/dev/null 2>&1; then
    if lsof -iTCP:8080 -sTCP:LISTEN -n -P >/dev/null 2>&1; then
        echo "❌ Port 8080 is already in use (another process is listening)."
        echo "   Stop that process first, e.g.:"
        echo "     lsof -iTCP:8080 -sTCP:LISTEN -n -P"
        echo "     kill <PID>"
        echo "   Or run on another port: SERVER_PORT=8081 mvn spring-boot:run"
        exit 1
    fi
fi

# Start the backend
echo "🚀 Starting Spring Boot application..."
echo "   API will be available at: http://localhost:8080"
echo "   Press Ctrl+C to stop"
echo ""

mvn spring-boot:run
