# Delivery Van Management System

A comprehensive delivery management platform designed to support end-to-end operations of goods delivery via delivery vans across a multi-depot network. The system manages the full delivery lifecycle from order receipt through to proof of delivery.

## Overview

The Delivery Van Management System is a full-stack application that provides:

- **Centralized platform** to manage vehicles, drivers, routes, and depots
- **Automated route allocation** using intelligent postcode-based routing rules
- **Order management** with manual entry and API integration
- **Manifest creation** and confirmation for delivery runs
- **Real-time tracking** and operational dashboards
- **Complete audit trail** of all configuration changes and operational activity

## Architecture

The system consists of two main components:

### Backend API (`delivery-api/`)
- **Technology**: Spring Boot 3.x, Java 21, PostgreSQL
- **Purpose**: RESTful API providing business logic, data persistence, and authentication
- **Key Features**:
  - Hierarchical postcode routing algorithm
  - JWT and API key authentication
  - Depot-scoped data access
  - Comprehensive audit logging
  - Database migrations with Flyway

### Frontend UI (`delivery-ui/`)
- **Technology**: React 19, Vite, TypeScript, Tailwind CSS
- **Purpose**: Modern web interface for depot managers and administrators
- **Key Features**:
  - Responsive design with shadcn/ui components
  - Real-time dashboard updates
  - Role-based access control
  - Dark mode support

## How It Works Together

```
┌─────────────────┐
│   React UI      │  ← User Interface (depot-ui/)
│   (Port 5173)   │
└────────┬────────┘
         │ HTTP/REST API
         │ JWT Authentication
         ▼
┌─────────────────┐
│  Spring Boot    │  ← Business Logic (delivery-api/)
│  (Port 8080)    │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│  PostgreSQL     │  ← Data Persistence
│  (Port 5432)    │
└─────────────────┘
```

### Communication Flow

1. **User Authentication**: Frontend sends login credentials to `/api/v1/auth/login`
2. **JWT Token**: Backend returns JWT token for subsequent requests
3. **API Requests**: Frontend includes JWT in `Authorization` header
4. **Data Access**: Backend enforces depot scoping based on user role
5. **Real-time Updates**: Frontend polls API endpoints for dashboard updates

### External System Integration

External systems can integrate via:
- **API Key Authentication**: Use `X-API-Key` header for order ingestion
- **RESTful Endpoints**: Standard JSON request/response format
- **API Versioning**: All endpoints under `/api/v1/`

## Project Structure

```
DeliverySystem/
├── delivery-api/          # Backend Spring Boot API
│   ├── src/main/java/     # Java source code
│   │   ├── domain/         # JPA entities
│   │   ├── repository/    # Data access layer
│   │   ├── service/        # Business logic
│   │   ├── controller/    # REST endpoints
│   │   ├── dto/           # Data transfer objects
│   │   └── security/      # Authentication & authorization
│   ├── src/main/resources/
│   │   ├── application.yml # Configuration
│   │   └── db/migration/   # Database migrations
│   └── pom.xml            # Maven dependencies
│
├── delivery-ui/           # Frontend React application
│   ├── src/
│   │   ├── components/    # React components
│   │   ├── pages/         # Page components
│   │   ├── api/           # API client & types
│   │   ├── contexts/      # React contexts
│   │   └── config/        # Configuration
│   ├── package.json       # NPM dependencies
│   └── vite.config.ts     # Vite configuration
│
└── docs/                  # Documentation
    ├── DeliverySystem-PRD.md
    └── DeliverySystem-Wireframes-v2.html
```

## Quick Start

### Prerequisites

- **Java 21** (for backend)
- **Node.js 18+** (for frontend)
- **PostgreSQL 12+** (or Docker)
- **Maven 3.8+** (for backend)
- **npm** or **yarn** (for frontend)

### 1. Clone the Repository

```bash
git clone git@github.com:nichom01/delivery.git
cd delivery
```

### 2. Set Up Backend

```bash
cd delivery-api

# Set Java 21 (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Create PostgreSQL database
createdb deliverysystem

# Update application.yml with your database credentials
# Edit src/main/resources/application.yml

# Build and run
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### 3. Set Up Frontend

```bash
cd delivery-ui

# Install dependencies
npm install

# Start development server
npm run dev
```

The UI will be available at `http://localhost:5173`

### 4. Configure API Connection

Update the frontend API configuration to point to your backend:

```typescript
// delivery-ui/src/api/client.ts
const API_BASE_URL = 'http://localhost:8080/api/v1';
```

## Key Features

### Postcode Routing
- **Hierarchical matching**: Full postcode → Sector → District → Area → Letter
- **Longest-match-wins**: Most specific rule takes precedence
- **Date-aware rules**: Effective date ranges for historical tracking
- **A-Z fallback**: Guarantees every postcode resolves to a route

### Depot Scoping
- **Data isolation**: Depot managers only see their depot's data
- **Central admin**: Full system access across all depots
- **Enforced at data layer**: Security built into service layer

### Order Management
- **Manual entry**: UI form for order creation
- **API integration**: REST endpoint for external systems
- **Duplicate detection**: Composite key (Order ID + Despatch ID)
- **Automatic routing**: Postcode resolved to route on creation

### Manifest System
- **Box-level tracking**: Individual box status (Expected, Received, Manifested, Delivered)
- **Exception handling**: Missing boxes flagged but don't block delivery
- **Confirmation workflow**: Draft → Confirmed → In Progress → Complete

### Audit Trail
- **Complete history**: All changes logged with user, timestamp, and before/after values
- **Depot-scoped**: Audit logs filtered by depot for managers
- **Append-only**: Audit records cannot be modified or deleted

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Migrations**: Flyway
- **Security**: Spring Security (JWT + API Key)
- **Build Tool**: Maven

### Frontend
- **Framework**: React 19
- **Language**: TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui (Radix UI)
- **Routing**: React Router v7
- **State Management**: React Context API

## API Endpoints

### Authentication
- `POST /api/v1/auth/login` - User login
- `GET /api/v1/auth/me` - Current user info

### Depots & Routes
- `GET /api/v1/depots` - List depots
- `GET /api/v1/depots/{id}/routes` - Routes for depot
- `GET /api/v1/routes/{id}` - Route details

### Orders
- `POST /api/v1/orders` - Create order (manual or API)

### Postcode Rules
- `GET /api/v1/postcode-rules` - List rules
- `POST /api/v1/postcode-rules` - Create rule

### Manifests
- `GET /api/v1/manifests` - List manifests
- `POST /api/v1/manifests` - Create manifest
- `PUT /api/v1/manifests/{id}/confirm` - Confirm manifest

### Dashboard
- `GET /api/v1/dashboard` - Dashboard data

See [delivery-api/README.md](delivery-api/README.md) for complete API documentation.

## Development

### Backend Development

```bash
cd delivery-api

# Run tests
mvn test

# Run with dev profile (additional logging)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Check code style
mvn checkstyle:check
```

### Frontend Development

```bash
cd delivery-ui

# Run development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## Environment Variables

### Backend (`delivery-api/src/main/resources/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/deliverysystem
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}

jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}

api:
  key: ${API_KEY:}
```

### Frontend

Create `.env` file in `delivery-ui/`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Database Schema

The system uses Flyway for database migrations. Key tables:

- **depots** - Physical dispatch locations
- **routes** - Delivery runs (belongs to depot)
- **postcode_rules** - Postcode-to-route mappings
- **orders** - Incoming delivery orders
- **boxes** - Individual delivery boxes
- **manifests** - Confirmed delivery runs
- **vehicles** - Delivery vans
- **drivers** - Delivery drivers
- **users** - System users
- **audit_events** - Audit trail

See `delivery-api/src/main/resources/db/migration/V1__Initial_schema.sql` for complete schema.

## User Roles

### Central Admin
- Full system access across all depots
- User management
- System configuration
- Complete audit log visibility

### Depot Manager
- Full access to own depot only
- Route and postcode rule management
- Vehicle and driver management
- Order entry and manifest creation
- Depot-scoped audit log

## Future Enhancements

- Mobile driver application
- Delivery stop ordering and ETA calculations
- Customer notifications and tracking
- Returns management
- Financial processing and invoicing

## Contributing

1. Create a feature branch from `main`
2. Make your changes
3. Ensure tests pass
4. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Documentation

- [Product Requirements Document](docs/DeliverySystem-PRD.md)
- [Backend API Documentation](delivery-api/README.md)
- [Frontend UI Documentation](delivery-ui/README.md)
- [Wireframes](docs/DeliverySystem-Wireframes-v2.html)

## Support

For issues and questions, please open an issue on GitHub or contact the development team.
