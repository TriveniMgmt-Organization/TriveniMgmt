# TriveniMgmt Backend

Multi-tenant SaaS Inventory Management System backend built with Spring Boot 3.5.3 and Java 21.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start (Development)](#quick-start-development)
- [Environment Variables](#environment-variables)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Production Deployment](#production-deployment)
- [Architecture Overview](#architecture-overview)
- [API Documentation](#api-documentation)
- [Default Users](#default-users)

## Prerequisites

- **Java 21** (JDK)
- **PostgreSQL 16+**
- **Gradle 8.x** (wrapper included)

## Quick Start (Development)

### 1. Clone and Navigate

```bash
cd backend
```

### 2. Set Up PostgreSQL

```bash
# Using Docker (recommended)
docker run -d \
  --name trivenimgmt-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=store_db \
  -p 5432:5432 \
  postgres:16

# Or install PostgreSQL locally and create database
createdb store_db
```

### 3. Create Environment File

Create a `.env` file in the backend directory (or export these variables):

```bash
# Database
DB_HOST=jdbc:postgresql://localhost:5432/store_db
DB_USER=postgres
DB_PASSWORD=postgres

# JWT (generate a secure secret for production)
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-characters

# Frontend URL for CORS
FRONTEND_URL=http://localhost:3000
```

### 4. Run the Application

```bash
# Load environment variables and run
export $(cat .env | xargs) && ./gradlew bootRun
```

Or with inline variables:

```bash
DB_HOST=jdbc:postgresql://localhost:5432/store_db \
DB_USER=postgres \
DB_PASSWORD=postgres \
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-characters \
FRONTEND_URL=http://localhost:3000 \
./gradlew bootRun
```

The application will:
1. Start on port 8080
2. Run Liquibase migrations automatically
3. Create demo users (dev profile only)

### 5. Verify Setup

```bash
# Health check
curl http://localhost:8080/actuator/health

# Login with demo user
curl -X POST http://localhost:8080/api/v2/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@store.com","password":"admin123"}'
```

## Environment Variables

### Required

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/store_db` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | `your-secure-secret-key-min-32-chars` |
| `FRONTEND_URL` | Frontend URL for CORS | `http://localhost:3000` |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | `8080` |
| `DB_POOL_SIZE` | HikariCP max pool size | `20` |
| `JWT_EXPIRATION_MS` | Access token expiry (ms) | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token expiry (ms) | `604800000` (7 days) |
| `JWT_ISSUER` | JWT issuer claim | `http://localhost:8080` |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP username | (empty) |
| `MAIL_PASSWORD` | SMTP password | (empty) |

## Database Setup

### Automatic (Recommended)

Liquibase migrations run automatically on application startup. No manual intervention needed.

**Migrations include:**
- Schema creation (tables, indexes, constraints)
- Reference data seeding (50 permissions, 9 roles)
- Role-permission mappings

### Manual Migration (if needed)

```bash
./gradlew liquibaseUpdate \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/store_db \
  -Dliquibase.username=postgres \
  -Dliquibase.password=postgres
```

### Fresh Database Reset

```bash
# Drop and recreate database
dropdb store_db && createdb store_db

# Restart application - migrations will run automatically
./gradlew bootRun
```

## Running the Application

### Development Mode

```bash
# With dev profile (creates demo users)
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# With hot reload
./gradlew bootRun --continuous
```

### Production Mode

```bash
# Build JAR
./gradlew build

# Run with production profile
java -jar build/libs/store-mgmt-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Using Docker

```dockerfile
# Build
docker build -t trivenimgmt-backend .

# Run
docker run -p 8080:8080 \
  -e DB_HOST=jdbc:postgresql://host.docker.internal:5432/store_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e JWT_SECRET=your-secret \
  -e FRONTEND_URL=http://localhost:3000 \
  trivenimgmt-backend
```

## Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests "com.store.mgmt.modules.auth.*"
```

### Test with Coverage

```bash
./gradlew test jacocoTestReport
# Report at: build/reports/jacoco/test/html/index.html
```

### Integration Tests

Tests use H2 in-memory database by default (configured in test resources).

```bash
./gradlew integrationTest
```

## Production Deployment

### 1. Build Production JAR

```bash
./gradlew build -x test
```

### 2. Environment Configuration

Set these environment variables in your production environment:

```bash
# Required
DB_HOST=jdbc:postgresql://prod-db-host:5432/store_db
DB_USER=prod_user
DB_PASSWORD=<secure-password>
JWT_SECRET=<256-bit-secure-secret>
FRONTEND_URL=https://app.yourdomain.com

# Recommended for production
SPRING_PROFILES_ACTIVE=prod
DB_POOL_SIZE=50
JWT_EXPIRATION_MS=900000
SERVER_PORT=8080
```

### 3. Run with Production Profile

```bash
java -jar build/libs/store-mgmt-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### 4. Production Checklist

- [ ] Use strong JWT_SECRET (256-bit minimum)
- [ ] Set `spring.profiles.active=prod` (disables DevDataSeeder)
- [ ] Configure proper database credentials
- [ ] Set up HTTPS/TLS termination (nginx, load balancer)
- [ ] Configure proper CORS allowed origins
- [ ] Set up log aggregation
- [ ] Configure health check endpoints for orchestrator
- [ ] Set appropriate JVM memory limits

### Health Checks (for Kubernetes/Docker)

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

## Architecture Overview

```
src/main/java/com/store/mgmt/
├── config/                 # Application configuration
│   ├── CacheConfig.java       # Caffeine caching (permissions)
│   ├── DataSeeder.java        # Reference data validation
│   ├── DevDataSeeder.java     # Dev-only demo users
│   └── security/              # Security configuration
├── shared/                 # Shared kernel (DDD)
│   ├── domain/                # Base entities, value objects
│   ├── application/           # Command/Query handlers
│   └── infrastructure/        # Cross-cutting concerns
└── modules/                # Feature modules (bounded contexts)
    ├── auth/                  # Authentication & authorization
    ├── users/                 # User management
    ├── organization/          # Multi-tenancy (orgs, stores)
    ├── products/              # Product catalog
    ├── inventory/             # Stock management
    └── globaltemplates/       # Template system
```

### Key Patterns

- **DDD (Domain-Driven Design)**: Bounded contexts, aggregates, domain services
- **CQRS**: Separate command and query handlers
- **Multi-tenancy**: Organization-based isolation via `UserOrganizationRole`
- **RBAC**: Role-based access control with fine-grained permissions

## API Documentation

Swagger UI is available at:
- Development: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

### Key Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v2/auth/login` | POST | User login |
| `/api/v2/auth/register` | POST | User registration |
| `/api/v2/auth/refresh` | POST | Refresh access token |
| `/api/v2/users/me` | GET | Current user info |
| `/api/v2/organizations` | GET | List organizations |
| `/api/v2/stores` | GET | List stores |
| `/actuator/health` | GET | Health check |

## Default Users

**Available only in `dev` profile:**

| Email | Password | Role |
|-------|----------|------|
| admin@store.com | admin123 | SUPER_ADMIN |
| manager@store.com | manager123 | STORE_MANAGER |

These users are created by `DevDataSeeder` on first startup.

## Roles and Permissions

### Roles (9 total)

| Role | Scope | Description |
|------|-------|-------------|
| SUPER_ADMIN | Platform | Full system access |
| ORG_ADMIN | Organization | Organization owner |
| ACCOUNTANT | Organization | Financial reporting |
| STORE_MANAGER | Store | Full store access |
| SHIFT_LEAD | Store | Shift supervisor |
| CASHIER | Store | POS operations |
| INVENTORY_SPECIALIST | Store | Stock management |
| PURCHASING_AGENT | Store | Vendor/PO management |
| STAFF | Store | General read access |

### Permission Categories (50 total)

- **Organization**: ORG_READ, ORG_WRITE
- **Store**: STORE_READ, STORE_WRITE
- **Products**: PRODUCT_READ, PRODUCT_WRITE, COST_READ, MARGIN_READ
- **Users**: USER_READ, USER_WRITE, USER_INVITE, ROLE_READ, ROLE_WRITE
- **Inventory**: INVENTORY_ITEM_READ/WRITE, STOCK_ADJUST, CYCLE_COUNT, TRANSFER_CREATE/APPROVE
- **Purchase Orders**: PO_READ, PO_WRITE, PO_APPROVE, RECEIVING_CREATE
- **Sales**: SALE_READ, SALE_WRITE, VOID_SALE, REFUND_APPROVE, PRICE_OVERRIDE, DISCOUNT_APPLY
- **Cash**: CASH_DRAWER_READ/WRITE, END_OF_DAY
- **Reports**: REPORT_READ, REPORT_FINANCIAL, REPORT_INVENTORY
- **Reference Data**: CATEGORY, BRAND, SUPPLIER, LOCATION, UOM (READ/WRITE each)

## Troubleshooting

### Common Issues

**1. Database connection failed**
```
Check DB_HOST, DB_USER, DB_PASSWORD environment variables
Ensure PostgreSQL is running and accessible
```

**2. Liquibase migration failed**
```bash
# Check migration status
./gradlew liquibaseStatus

# If stuck, you may need to unlock
./gradlew liquibaseReleaseLocks
```

**3. Demo users not created**
```
Ensure SPRING_PROFILES_ACTIVE=dev is set
Check logs for DevDataSeeder output
```

**4. CORS errors**
```
Verify FRONTEND_URL matches your frontend's origin exactly
Include protocol (http:// or https://)
```

### Logs

```bash
# View application logs
./gradlew bootRun 2>&1 | tee app.log

# Increase log level
LOGGING_LEVEL_COM_STORE_MGMT=DEBUG ./gradlew bootRun
```

## License

Proprietary - TriveniMgmt Organization
