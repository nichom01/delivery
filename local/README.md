# Local Development Scripts

This directory contains scripts to easily set up and run the Delivery System locally.

## Quick Start

### First Time Setup

```bash
./local/setup.sh
```

This will:
- Check prerequisites (Docker, Java 21, Node.js, Maven)
- Start the PostgreSQL database container
- Build the backend
- Install frontend dependencies
- Create necessary configuration files

### Running the System

**Option 1: Start everything at once**
```bash
./local/start-all.sh
```

**Option 2: Start components individually**

Start database:
```bash
./local/start-db.sh
```

Start backend (in one terminal):
```bash
./local/start-backend.sh
```

Start frontend (in another terminal):
```bash
./local/start-frontend.sh
```

### Stopping the System

```bash
./local/stop-all.sh
```

## Scripts Overview

- **setup.sh** - Initial setup and dependency installation
- **start-db.sh** - Start PostgreSQL Docker container
- **start-backend.sh** - Start Spring Boot backend API
- **start-frontend.sh** - Start React frontend
- **start-all.sh** - Start database, backend, and frontend
- **stop-all.sh** - Stop all running components
- **reset-db.sh** - Reset database (removes all data)

## Prerequisites

- Docker and Docker Compose
- Java 21
- Node.js 18+
- Maven 3.8+

## Troubleshooting

If scripts don't have execute permissions:
```bash
chmod +x local/*.sh
```

For Windows users, use Git Bash or WSL to run these scripts.
