# TriveniMgmt - Project Context for Claude

> **Auto-generated**: This file serves as the primary context entry point.
> **Last Updated**: 2026-01-24

## Quick Reference

| Component | Technology | Port |
|-----------|------------|------|
| Backend | Spring Boot 3.5.3, Java 21 | 8080 |
| Frontend | Next.js 15, React 19, MUI 5 | 3000 |
| Database | PostgreSQL 16 | 5432 |
| Auth | JWT (HttpOnly cookies) | - |

## Business Context

**TriveniMgmt** is a multi-tenant SaaS Inventory Management System targeting SMBs with 2-20 stores who have outgrown spreadsheets but don't need ERP complexity.

### Target Market
- Retail chains (fashion, electronics, general merchandise)
- Food & beverage distributors (with batch/expiry tracking)
- Wholesale businesses with multiple warehouses

### Value Proposition
- **Template-based product catalog**: Organizations define master products, stores instantiate with local pricing
- **Multi-location tracking**: Stores, warehouses, and zones within locations
- **Batch/lot tracking**: Built-in support for expiry dates and FEFO (First Expired, First Out)
- **Role-based access**: Granular permissions at organization and store levels

### Feature Scope by Level

**Organization Level (ORG_ADMIN)**:
- Store management (create, configure, deactivate stores)
- User management (invite users, assign roles)
- Product Catalog (templates, categories, brands, UoM)
- Supplier management
- Discount rules and pricing policies
- Cross-store reporting and analytics
- Billing and subscription management

**Store Level (STORE_MANAGER)**:
- Products (instantiate from templates with store pricing)
- Inventory management (stock levels, locations)
- Purchase orders (create, receive goods)
- Sales/POS operations
- Stock adjustments and transfers
- Low stock alerts and expiry warnings

## Architecture Overview

**Modular Monolith with Pragmatic Clean Architecture and CQRS**

The backend uses a modular monolith architecture where each business domain is a self-contained module following **Pragmatic Clean Architecture** principles with CQRS (Command Query Responsibility Segregation).

### Why Pragmatic (not Strict) Clean Architecture?

We keep JPA annotations (`@Entity`, `@Column`) directly in domain models rather than maintaining separate pure POJOs + JPA entities. This reduces boilerplate without sacrificing the key benefit: **module decoupling**.

The real architectural win is **UUID-based cross-module references** (no entity imports across modules), not hiding framework annotations. See [BACKEND.md](./docs/BACKEND.md) for detailed rationale.

### Key Architectural Patterns
- **CQRS**: Commands for writes, Queries for reads via CommandBus/QueryBus
- **Clean Architecture**: application → domain → infrastructure layers
- **JPA Entities**: Direct JPA entities (no separate DDD aggregates)
- **Spring Data Repositories**: Standard Spring Data JPA repositories
- **UUID Cross-Module References**: Modules reference each other by UUID only (not entity)
- **Hibernate Tenant Filtering**: Automatic multi-tenant isolation at repository level

### Module Coupling Rules
- **Intra-module**: Entity relationships allowed (e.g., Store → Organization)
- **Cross-module**: UUID references only (e.g., Category.organizationId: UUID)
- **Shared kernel**: Common base classes and interfaces in `shared/` package

## Directory Structure

```
parent-folder/
├── backend/                              # THIS REPO (Spring Boot)
│   ├── .claude/                          # Documentation (this folder)
│   │   ├── CONTEXT.md                    # This file
│   │   ├── CHANGELOG.md                  # Feature/bug changelog
│   │   └── docs/                         # Detailed documentation
│   ├── CLAUDE.md                         # Entry point for Claude
│   └── src/main/java/com/store/mgmt/
│       │
│       ├── modules/                      # Feature Modules (Clean Architecture)
│       │   ├── auth/                     # Authentication module
│       │   │   ├── application/          # Use cases
│       │   │   │   ├── command/          # Commands & Handlers
│       │   │   │   ├── query/            # Queries & Handlers
│       │   │   │   ├── dto/              # Data Transfer Objects
│       │   │   │   └── service/          # Application services
│       │   │   ├── domain/               # Domain layer
│       │   │   │   ├── model/            # JPA Entities
│       │   │   │   └── repository/       # Spring Data Repositories
│       │   │   └── infrastructure/       # Infrastructure layer
│       │   │       ├── web/              # REST Controllers
│       │   │       └── service/          # Infrastructure services
│       │   │
│       │   ├── organization/             # Organization & Store module
│       │   ├── users/                    # User management module
│       │   ├── inventory/                # Inventory management module
│       │   ├── products/                 # Product catalog module
│       │   └── globaltemplates/          # Global templates module
│       │
│       ├── shared/                       # Shared Kernel
│       │   ├── application/              # Shared interfaces
│       │   │   ├── command/              # Command, CommandHandler
│       │   │   └── query/                # Query, QueryHandler
│       │   ├── domain/                   # Shared domain
│       │   │   ├── model/                # BaseEntity, ValueObject
│       │   │   ├── exception/            # Base exceptions
│       │   │   ├── event/                # Domain events
│       │   │   └── repository/           # Repository interfaces
│       │   └── infrastructure/           # Shared infrastructure
│       │       ├── CommandBus.java       # Command dispatcher
│       │       ├── QueryBus.java         # Query dispatcher
│       │       ├── audit/                # Audit logging
│       │       ├── event/                # Event publishing
│       │       └── security/             # JWT, TenantContext
│       │
│       ├── config/                       # Application Configuration
│       │   ├── security/                 # Spring Security config
│       │   ├── DataSeeder.java           # Data seeding
│       │   └── TenantContext.java        # Legacy tenant context
│       │
│       └── seeder/                       # Standalone seeder utility
│
└── frontend/                             # SIBLING REPO (Next.js) at ../frontend
    └── src/
        ├── app/                          # Next.js App Router pages
        ├── components/                   # UI components
        ├── contexts/                     # Auth, Navigation, Notification
        ├── api/generated/                # Orval-generated API client
        └── lib/                          # Utilities, theme, hooks
```

**GitHub Repos**:
- Backend: `TriveniMgmt-Organization/TriveniMgmt`
- Frontend: `TriveniMgmt-Organization/triveni-mgmt-client`

## Key Files to Know

### Backend Critical Files

| File | Purpose |
|------|---------|
| `shared/infrastructure/CommandBus.java` | Dispatches commands to handlers |
| `shared/infrastructure/QueryBus.java` | Dispatches queries to handlers |
| `shared/domain/model/BaseEntity.java` | Base JPA entity (UUID, audit fields) |
| `shared/infrastructure/security/JWTService.java` | Token generation/validation |
| `config/security/SecurityConfig.java` | JWT, CORS, endpoint security |
| `config/DataSeeder.java` | Seeds permissions, roles, default users |
| `modules/organization/domain/model/UserOrganizationRole.java` | User-Org-Role mapping |

### Module Structure Pattern

Each module follows this pattern:
```
modules/{module-name}/
├── application/
│   ├── command/           # CreateXxxCommand.java, CreateXxxHandler.java
│   ├── query/             # GetXxxQuery.java, GetXxxHandler.java
│   ├── dto/               # XxxDTO.java, XxxResponseDTO.java
│   └── service/           # Application services, mappers
├── domain/
│   ├── model/             # JPA entities (extend BaseEntity)
│   ├── repository/        # Spring Data JPA repositories
│   ├── service/           # Domain services (business logic)
│   └── exception/         # Domain-specific exceptions
└── infrastructure/
    ├── web/               # REST controllers
    ├── persistence/       # Custom JPA implementations (if needed)
    └── service/           # Infrastructure services
```

### Frontend Critical Files
| File | Purpose |
|------|---------|
| `contexts/AuthContext.tsx` | Auth state, login/logout, permissions |
| `contexts/NavigationContext.tsx` | Scope switching (org/store level) |
| `lib/server/api-client.ts` | Axios with interceptors, token refresh |
| `components/auth/Gatekeeper.tsx` | Permission-based rendering |

## Multi-Tenancy Model

```
Organization (Tenant)
├── Users (with ORG_ADMIN or SUPER_ADMIN role)
├── Stores
│   └── Users (with STORE_MANAGER role)
├── Product Catalog (Categories, Brands, Templates)
└── Policies (Discounts, Pricing)
```

### Scope-Based Access
- **Organization Level**: SUPER_ADMIN, ORG_ADMIN can access org settings, all stores
- **Store Level**: STORE_MANAGER can only access assigned store(s)

## Authentication Flow

1. User logs in → Backend returns JWT in HttpOnly cookies
2. Frontend calls `/api/me` to get user info
3. User selects tenant (if multiple orgs) → `/api/select-tenant`
4. `X-Store-Id` header sent with store-scoped requests

## Common Patterns

### Backend - CQRS Pattern

**Commands (Write Operations):**
```java
// Command record
public record CreateBrandCommand(String name, String description) implements Command<BrandDTO> {}

// Handler
@Component
public class CreateBrandHandler implements CommandHandler<CreateBrandCommand, BrandDTO> {
    @Override
    public BrandDTO handle(CreateBrandCommand cmd) {
        // Business logic
    }
}

// Controller usage
@PostMapping
public ResponseEntity<BrandDTO> create(@RequestBody CreateBrandRequestDTO request) {
    return ResponseEntity.ok(commandBus.dispatch(new CreateBrandCommand(request.name(), request.description())));
}
```

**Queries (Read Operations):**
```java
// Query record
public record GetBrandByIdQuery(UUID id) implements Query<BrandDTO> {}

// Handler
@Component
public class GetBrandByIdHandler implements QueryHandler<GetBrandByIdQuery, BrandDTO> {
    @Override
    public BrandDTO handle(GetBrandByIdQuery query) {
        // Fetch and return
    }
}
```

### Backend - Entity Pattern
- All entities extend `BaseEntity` (UUID id, audit fields, soft delete)
- Use Lombok annotations (@Data, @Entity, @Table)
- Spring Data JPA repositories (no manual implementations needed)

### Frontend
- App Router with route groups: `(auth)`, `(dashboard)`
- React Query for server state, Context for client state
- Form handling with React Hook Form + Yup validation
- `AppFormDialog` pattern for CRUD modals

## API Versioning

- **V2 API**: `/api/v2/*` - Current, uses CQRS pattern
- All 79 V2 endpoints available

## Related Documentation

- [Backend Details](./docs/BACKEND.md) - Architecture, patterns, modules
- [Frontend Details](./docs/FRONTEND.md) - Components, hooks, state
- [API Reference](./docs/API.md) - All endpoints with auth requirements
- [Database Schema](./docs/DATABASE.md) - Entity relationships
- [Permission System](./docs/PERMISSIONS.md) - Roles and permissions
- [Changelog](./CHANGELOG.md) - Recent changes

---

## Instructions for Claude

When working on this project:

1. **Read this file first** - It provides essential context
2. **Check CHANGELOG.md** - For recent changes and decisions
3. **Reference specific docs** - Based on the task area
4. **Update CHANGELOG.md** - After completing features/fixes

### Token Efficiency Tips
- This CONTEXT.md is the minimal context needed for most tasks
- Only read detailed docs when working in that specific area
- The generated API types in `api/generated/models/` are the source of truth

### Architecture Guidelines

When adding new features:
1. Create command/query records in `application/command/` or `application/query/`
2. Create handlers that implement `CommandHandler<C,R>` or `QueryHandler<Q,R>`
3. JPA entities go in `domain/model/`, repositories in `domain/repository/`
4. Controllers go in `infrastructure/web/`
5. Use `commandBus.dispatch()` and `queryBus.dispatch()` in controllers

**Cross-Module Reference Rules:**
- **NEVER** add `@ManyToOne` or `@OneToMany` relationships to entities in other modules
- **ALWAYS** use UUID fields for cross-module references (e.g., `organizationId`, `storeId`, `userId`)
- **VALIDATE** cross-module IDs exist by calling the other module's repository
- **ADD** `@Filter` annotation to tenant-scoped entities for automatic isolation

Example for a new inventory entity:
```java
@Entity
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
public class MyNewEntity extends BaseEntity {
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;  // ✅ UUID, not Organization entity

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;    // ✅ OK - same module (inventory)
}
```
