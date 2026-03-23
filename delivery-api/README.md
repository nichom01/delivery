# Delivery System API

Backend REST API for the Delivery Van Management System built with Spring Boot 3.x, Java 21, and PostgreSQL.

## Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 12+ (or Docker for local development)
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

**Note:** If you have multiple Java versions installed, ensure Java 21 is used:
```bash
# macOS
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Linux/Windows
# Set JAVA_HOME to point to Java 21 installation
```

## Setup

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE deliverysystem;
```

Or use Docker:

```bash
docker run --name delivery-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=deliverysystem \
  -p 5432:5432 \
  -d postgres:15
```

### 2. Configuration

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/deliverysystem
    username: postgres
    password: postgres
```

Set JWT secret (required for production):

```yaml
jwt:
  secret: your-secret-key-change-in-production-min-256-bits
```

Set API key for external system authentication (optional):

```yaml
api:
  key: your-api-key-here
```

Or use environment variables:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-secret-key-change-in-production-min-256-bits
export API_KEY=your-api-key-here
```

### 3. Build and Run

**Important:** Ensure Java 21 is set as JAVA_HOME before building:

```bash
# macOS
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Verify Java version
java -version  # Should show version 21
```

Build the project:

```bash
mvn clean install
```

Run the application:

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/delivery-api-1.0.0.jar
```

Or run the JAR:

```bash
java -jar target/delivery-api-1.0.0.jar
```

The API will be available at `http://localhost:8080`

## API Endpoints

### Authentication

- `POST /api/v1/auth/login` - User login (returns JWT token)
- `GET /api/v1/auth/me` - Get current user info

### Depots

- `GET /api/v1/depots` - List all depots
- `GET /api/v1/depots/{id}/routes` - Get routes for a depot

### Routes

- `GET /api/v1/routes/{id}` - Get route details

### Orders

- `POST /api/v1/orders` - Create order (requires authentication or API key)

### Postcode Rules

- `GET /api/v1/postcode-rules` - List postcode rules (optional depotId filter)
- `POST /api/v1/postcode-rules` - Create postcode rule

### Vehicles

- `GET /api/v1/vehicles` - List vehicles (optional depotId filter)

### Drivers

- `GET /api/v1/drivers` - List drivers (optional depotId filter)

### Driver locations (mobile ingest)

- `POST /api/v1/driver-locations` - Submit one or more GPS samples for the authenticated driver user (JWT only; `ROLE_DRIVER` required)

Request body:

```json
{
  "locations": [
    {
      "latitude": 51.507351,
      "longitude": -0.127758,
      "recordedAt": "2026-03-23T10:42:26Z"
    }
  ]
}
```

- `locations`: required, non-empty, at most 100 points per request.
- `latitude` / `longitude`: required; must fall within valid WGS84 ranges.
- `recordedAt`: required ISO-8601 instant (client time when the point was captured). Server rejects samples more than 30 days in the past or more than 5 minutes in the future (clock skew).

Success response (`200`): `ApiResponse` with `data.savedCount` equal to the number of rows persisted. Each row stores `user_id` from the JWT, client `recordedAt`, and server `received_at`.

### Manifests

- `GET /api/v1/manifests` - List manifests (optional depotId and date filters)
- `POST /api/v1/manifests` - Create manifest
- `PUT /api/v1/manifests/{id}/confirm` - Confirm manifest

### Dashboard

- `GET /api/v1/dashboard` - Get dashboard data (optional depotId and date filters)

### Audit

- `GET /api/v1/audit` - Get audit log (optional depotId filter)

## Authentication

### JWT Authentication (UI Users)

1. Login via `POST /api/v1/auth/login` with username and password
2. Include the returned token in subsequent requests:
   ```
   Authorization: Bearer <token>
   ```

Driver-only endpoints (for example `POST /api/v1/driver-locations`) require a user with role `DRIVER`. When seed data is enabled, a sample driver login is `driver1` / `password` (London Central depot).

### API Key Authentication (External Systems)

Include API key in request header:
```
X-API-Key: <your-api-key>
```

## Database Migrations

The project uses Flyway for database migrations. Migrations are located in `src/main/resources/db/migration/` and run automatically on application startup.

## Development

### Running Tests

```bash
mvn test
```

### Development Profile

Use the `dev` profile for additional logging:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Code Style

The project uses standard Java code with explicit getters, setters, and constructors.

## Project Structure

```
delivery-api/
├── src/main/java/com/deliverysystem/
│   ├── config/          # Configuration classes (Security, CORS)
│   ├── domain/          # JPA entities
│   ├── repository/      # JPA repositories
│   ├── service/         # Business logic services
│   ├── controller/    # REST controllers
│   ├── dto/            # Data Transfer Objects
│   ├── security/       # Security components (JWT, API Key)
│   └── exception/      # Exception handlers
├── src/main/resources/
│   ├── application.yml # Application configuration
│   └── db/migration/   # Flyway migrations
└── pom.xml             # Maven dependencies
```

## Key Features

- **Postcode Routing**: Hierarchical longest-match-wins algorithm for route allocation
- **Audit Trail**: Complete audit logging of all configuration changes
- **Depot Scoping**: Data access scoped by depot for depot managers
- **JWT Authentication**: Stateless authentication for UI users
- **API Key Authentication**: Separate authentication for external systems
- **CORS Support**: Configured for React frontend integration

## Next Steps

- Add comprehensive unit and integration tests
- Implement dashboard aggregation logic
- Add route drilldown functionality
- Implement box receiving and reconciliation
- Add POD (Proof of Delivery) upload endpoint
- Set up API documentation with Swagger/OpenAPI

## License

MIT
