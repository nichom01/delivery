#!/bin/bash

# Setup script for Delivery System local development
# This script checks prerequisites and sets up the development environment

set -e

echo "🚀 Setting up Delivery System for local development..."
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check Docker
echo "Checking Docker..."
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi
if ! docker ps &> /dev/null; then
    echo -e "${RED}❌ Docker daemon is not running. Please start Docker.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker is installed and running${NC}"

# Check Docker Compose
echo "Checking Docker Compose..."
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker Compose is available${NC}"

# Check Java
echo "Checking Java..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed. Please install Java 21.${NC}"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo -e "${YELLOW}⚠️  Java version is $JAVA_VERSION. Java 21 is recommended.${NC}"
else
    echo -e "${GREEN}✅ Java $JAVA_VERSION is installed${NC}"
fi

# Set Java 21 on macOS if available
if [[ "$OSTYPE" == "darwin"* ]]; then
    if [ -f /usr/libexec/java_home ]; then
        JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")
        if [ -n "$JAVA_21_HOME" ]; then
            export JAVA_HOME="$JAVA_21_HOME"
            echo -e "${GREEN}✅ Set JAVA_HOME to Java 21${NC}"
        fi
    fi
fi

# Check Maven
echo "Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed. Please install Maven 3.8+.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Maven is installed${NC}"

# Check Node.js
echo "Checking Node.js..."
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed. Please install Node.js 18+.${NC}"
    exit 1
fi
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo -e "${YELLOW}⚠️  Node.js version is $NODE_VERSION. Node.js 18+ is recommended.${NC}"
else
    echo -e "${GREEN}✅ Node.js $(node -v) is installed${NC}"
fi

# Check npm
echo "Checking npm..."
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ npm is not installed.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ npm is installed${NC}"

echo ""
echo "📦 Installing dependencies..."

# Start database
echo ""
echo "Starting PostgreSQL database..."
cd "$(dirname "$0")/.."
if [ -f docker-compose.yml ]; then
    docker-compose up -d postgres || docker compose up -d postgres
    echo "Waiting for database to be ready..."
    sleep 5
    echo -e "${GREEN}✅ Database is running${NC}"
else
    echo -e "${YELLOW}⚠️  docker-compose.yml not found. Skipping database startup.${NC}"
fi

# Build backend
echo ""
echo "Building backend..."
cd delivery-api
if [ -f pom.xml ]; then
    mvn clean install -DskipTests
    echo -e "${GREEN}✅ Backend built successfully${NC}"
else
    echo -e "${YELLOW}⚠️  pom.xml not found. Skipping backend build.${NC}"
fi

# Install frontend dependencies
echo ""
echo "Installing frontend dependencies..."
cd ../delivery-ui
if [ -f package.json ]; then
    npm install
    echo -e "${GREEN}✅ Frontend dependencies installed${NC}"
    
    # Create .env file if it doesn't exist
    if [ ! -f .env ]; then
        echo "Creating .env file..."
        echo "VITE_API_BASE_URL=http://localhost:8080/api/v1" > .env
        echo -e "${GREEN}✅ Created .env file${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  package.json not found. Skipping frontend setup.${NC}"
fi

echo ""
echo -e "${GREEN}✨ Setup complete!${NC}"
echo ""
echo "Next steps:"
echo "  1. Start the system: ./local/start-all.sh"
echo "  2. Or start components individually:"
echo "     - Database: ./local/start-db.sh"
echo "     - Backend:  ./local/start-backend.sh"
echo "     - Frontend: ./local/start-frontend.sh"
echo ""
echo "Access the application at: http://localhost:5173"
