# Backend Architecture

> **Path**: `src/main/java/com/store/mgmt/` (this repo)
> **Last Updated**: 2026-01-24

## Architecture Overview

The backend follows a **Modular Monolith** architecture with **Clean Architecture** principles and **CQRS** (Command Query Responsibility Segregation) pattern.

### Why Pragmatic Clean Architecture?

This project uses **Pragmatic Clean Architecture** rather than strict Clean Architecture. Here's the key difference and rationale:

| Aspect | Strict Clean Architecture | Pragmatic (Our Approach) |
|--------|---------------------------|--------------------------|
| Domain Models | Pure POJOs, no framework annotations | JPA entities with `@Entity`, `@Column` |
| Persistence | Separate persistence entities + mappers | Domain entities ARE the JPA entities |
| Code Volume | ~2x more files (domain + persistence layers) | Fewer files, less boilerplate |
| Framework Coupling | Zero framework dependencies in domain | JPA annotations in domain |
| Testability | Domain tests need no Spring | Domain tests may need JPA context |

**Why we chose Pragmatic:**

1. **Reduced Complexity**: Strict architecture requires separate domain POJOs, JPA entities, and mappers between them. For a modular monolith (not microservices), this doubles the code without proportional benefit.

2. **Faster Development**: No need to maintain parallel hierarchies (Domain `User` vs JPA `UserEntity`) and keep them in sync.

3. **Spring Boot Ecosystem**: The project is committed to Spring Boot. The "what if we switch frameworks" scenario is unlikely enough that the abstraction cost isn't justified.

4. **Module Boundaries Are The Real Win**: The critical architectural benefit comes from **module decoupling via UUID references**, not from hiding JPA from domain models. A `Category` with `@Entity` annotation but no cross-module entity dependencies is cleaner than a pure POJO `Category` that imports `Organization` entity.

5. **Pragmatic Precedent**: Many successful Spring Boot applications (including Spring's own samples) use JPA entities directly in the domain layer.

**What we DO enforce strictly:**
- ✅ No cross-module entity relationships (UUID references only)
- ✅ CQRS separation (Commands/Queries with dedicated handlers)
- ✅ Layered architecture (application → domain → infrastructure)
- ✅ Automatic tenant isolation (Hibernate filters)

**What we DON'T enforce:**
- ❌ Pure domain models without JPA annotations
- ❌ Separate persistence layer with entity mappers
- ❌ Repository interfaces in domain with implementations in infrastructure

### Key Principles
- **Modular**: Each business domain is a self-contained module
- **Clean Architecture**: Dependency rule flows inward (infrastructure → application → domain)
- **CQRS**: Separate command (write) and query (read) paths
- **JPA Entities**: Direct JPA entities with Spring Data repositories (no DDD aggregates)
- **UUID Cross-Module References**: Modules reference each other by UUID only, not by entity
- **Hibernate Tenant Filtering**: Automatic multi-tenant isolation at repository level

## Package Structure

```
com.store.mgmt/
├── modules/                    # Feature Modules
│   ├── auth/                   # Authentication & Authorization
│   ├── organization/           # Organization & Store management
│   ├── users/                  # User, Role, Permission
│   ├── inventory/              # Stock, Transactions, PO, Sales
│   ├── products/               # Product Templates & Variants
│   └── globaltemplates/        # Industry templates
│
├── shared/                     # Shared Kernel
│   ├── application/            # Command/Query interfaces
│   ├── domain/                 # BaseEntity, exceptions
│   └── infrastructure/         # CommandBus, QueryBus, security
│
├── config/                     # Application Configuration
│   ├── security/               # Spring Security, JWT filter
│   ├── DataSeeder.java         # Default data seeding
│   └── TenantContext.java      # Multi-tenant context
│
└── seeder/                     # Standalone CLI seeder
```

## Module Structure

Each module follows Clean Architecture layers:

```
modules/{module-name}/
├── application/                # Application Layer (Use Cases)
│   ├── command/                # Commands & Handlers (writes)
│   │   ├── CreateXxxCommand.java
│   │   └── CreateXxxHandler.java
│   ├── query/                  # Queries & Handlers (reads)
│   │   ├── GetXxxQuery.java
│   │   └── GetXxxHandler.java
│   ├── dto/                    # Data Transfer Objects
│   │   ├── XxxDTO.java
│   │   ├── XxxRequestDTO.java
│   │   └── XxxResponseDTO.java
│   └── service/                # Application services, mappers
│
├── domain/                     # Domain Layer
│   ├── model/                  # JPA Entities
│   ├── repository/             # Spring Data JPA Repositories
│   ├── service/                # Domain services (business logic)
│   ├── event/                  # Domain events (optional)
│   └── exception/              # Domain-specific exceptions
│
└── infrastructure/             # Infrastructure Layer
    ├── web/                    # REST Controllers
    ├── persistence/            # Custom JPA implementations
    └── service/                # Infrastructure services
```

## CQRS Pattern

### Command (Write Operations)

```java
// 1. Define Command record
public record CreateBrandCommand(
    String name,
    String description,
    UUID organizationId
) implements Command<BrandDTO> {}

// 2. Implement Handler
@Component
public class CreateBrandHandler implements CommandHandler<CreateBrandCommand, BrandDTO> {

    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public BrandDTO handle(CreateBrandCommand cmd) {
        Brand brand = new Brand();
        brand.setName(cmd.name());
        brand.setDescription(cmd.description());
        // ... set other fields

        Brand saved = brandRepository.save(brand);
        return BrandMapper.toDTO(saved);
    }
}

// 3. Use in Controller
@RestController
@RequestMapping("/api/v2/inventory/brands")
public class BrandController {

    private final CommandBus commandBus;

    @PostMapping
    public ResponseEntity<BrandDTO> create(@RequestBody CreateBrandRequestDTO request) {
        CreateBrandCommand cmd = new CreateBrandCommand(
            request.name(),
            request.description(),
            TenantContext.current().organizationId()
        );
        return ResponseEntity.ok(commandBus.dispatch(cmd));
    }
}
```

### Query (Read Operations)

```java
// 1. Define Query record
public record GetBrandByIdQuery(UUID id) implements Query<BrandDTO> {}

// 2. Implement Handler
@Component
public class GetBrandByIdHandler implements QueryHandler<GetBrandByIdQuery, BrandDTO> {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public BrandDTO handle(GetBrandByIdQuery query) {
        Brand brand = brandRepository.findById(query.id())
            .orElseThrow(() -> new ResourceNotFoundException("Brand", query.id()));
        return BrandMapper.toDTO(brand);
    }
}

// 3. Use in Controller
@GetMapping("/{id}")
public ResponseEntity<BrandDTO> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(queryBus.dispatch(new GetBrandByIdQuery(id)));
}
```

## Cross-Module Reference Pattern

**IMPORTANT**: Modules must NOT have JPA entity relationships across module boundaries.

### Why UUID References?
- **Module Independence**: Each module can be tested, deployed, or extracted independently
- **No Circular Dependencies**: Prevents cascading refactoring across modules
- **Cleaner Architecture**: Domain models don't know about other modules' internals

### Pattern Example

```java
// ❌ WRONG - Cross-module entity reference
@ManyToOne
@JoinColumn(name = "organization_id")
private Organization organization;  // From organization module

// ✅ CORRECT - UUID reference
@Column(name = "organization_id", nullable = false)
private UUID organizationId;  // Just the ID, no entity dependency
```

### Which Relationships Are Allowed?

| Relationship Type | Allowed? | Example |
|-------------------|----------|---------|
| Intra-module entity | ✅ Yes | Store → Organization (both in organization module) |
| Cross-module entity | ❌ No | Category → Organization |
| Cross-module UUID | ✅ Yes | Category.organizationId (UUID) |

### Inventory Module Entity References

All inventory entities use UUID references for cross-module relationships:

| Entity | Field | Type | Notes |
|--------|-------|------|-------|
| Category | organizationId | UUID | References organization module |
| ProductTemplate | organizationId | UUID | References organization module |
| ProductVariant | organizationId | UUID | References organization module |
| Supplier | organizationId | UUID | References organization module |
| InventoryLocation | storeId | UUID | References organization module |
| DamageLoss | organizationId, storeId, userId | UUID | Multiple cross-module refs |
| PurchaseOrder | organizationId, userId | UUID | Multiple cross-module refs |
| Sale | storeId, userId | UUID | Multiple cross-module refs |

### Fetching Cross-Module Data

When you need full entity data from another module, fetch it separately:

```java
// In a handler that needs Organization data
@Component
public class CreateCategoryHandler implements CommandHandler<CreateCategoryCommand, CategoryDTO> {

    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;  // Cross-module repo

    @Override
    public CategoryDTO handle(CreateCategoryCommand cmd) {
        // Validate organization exists
        if (!organizationRepository.existsById(cmd.organizationId())) {
            throw new EntityNotFoundException("Organization not found");
        }

        Category category = new Category();
        category.setOrganizationId(cmd.organizationId());  // Store UUID only
        // ...
    }
}
```

## Hibernate Tenant Filter

Multi-tenant isolation is enforced at the repository level using Hibernate filters.

### How It Works

1. **Filter Definition** (in `package-info.java`):
```java
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "organizationId", type = UUID.class)
)
package com.store.mgmt.modules.inventory.domain.model;
```

2. **Filter on Entities**:
```java
@Entity
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
public class Category extends BaseEntity {
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
}
```

3. **Filter Enabled by Aspect**:
```java
@Aspect
@Component
public class TenantFilterAspect {
    @Around("execution(* com.store.mgmt.modules.inventory.domain.repository.*.*(..))")
    public Object enableTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        if (orgId != null) {
            tenantFilter.enableFilter(orgId);
        }
        return joinPoint.proceed();
    }
}
```

### Tenant-Filtered Entities

All inventory entities with `organizationId` have the tenant filter:
- Category, ProductTemplate, ProductVariant, Supplier
- UnitOfMeasure, TaxRule, Discount
- PurchaseOrder, DamageLoss

### Testing Tenant Isolation

1. Login as user from Organization A
2. Try to access data from Organization B via API
3. Verify 404/empty response (not 200 with other tenant's data)

## Core Components

### BaseEntity (`shared/domain/model/BaseEntity.java`)

All JPA entities extend this:
```java
@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    private LocalDateTime deletedAt;  // Soft delete

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

### CommandBus & QueryBus (`shared/infrastructure/`)

```java
// CommandBus dispatches commands to handlers
@Component
public class CommandBus {
    public <R> R dispatch(Command<R> command) {
        // Finds handler by command type and executes
    }
}

// QueryBus dispatches queries to handlers
@Component
public class QueryBus {
    public <R> R dispatch(Query<R> query) {
        // Finds handler by query type and executes
    }
}
```

### TenantContext (`shared/infrastructure/security/TenantContext.java`)

Multi-tenant context for the current request:
```java
public record TenantContext(
    UUID organizationId,
    UUID storeId,
    UUID userId,
    String username
) {
    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    public static TenantContext current() { ... }
    public static void set(TenantContext context) { ... }
    public static void clear() { ... }
}
```

## Module Details

### Auth Module (`modules/auth/`)

**Entities:**
- `RefreshToken` - JWT refresh token storage

**Commands:**
- `LoginCommand` → `AuthResponseDTO`
- `RegisterCommand` → `AuthResponseDTO`
- `RefreshTokenCommand` → `AuthResponseDTO`
- `LogoutCommand` → `Void`
- `ValidateTokenCommand` → `AuthUserDTO`

**Queries:**
- `GetCurrentUserQuery` → `AuthUserDTO`

### Organization Module (`modules/organization/`)

**Entities:**
- `Organization` - Top-level tenant
- `Store` - Stores within organization
- `UserOrganizationRole` - User-Org-Role-Store mapping
- `UserAssignment` - User assignments
- `Invitation` - User invitations
- `StoreStatus` (enum) - ACTIVE, INACTIVE, CLOSED

**Key Repositories:**
- `OrganizationRepository.findByIdWithStores()`
- `StoreRepository.findByOrganizationId()`
- `UserOrganizationRoleRepository.existsByUserIdAndOrganizationId()`

### Users Module (`modules/users/`)

**Entities:**
- `User` - System users
- `Role` - Roles (SUPER_ADMIN, ORG_ADMIN, STORE_MANAGER, etc.)
- `Permission` - Granular permissions
- `RoleType` (enum) - Role type constants

**Key Repositories:**
- `UserRepository.findByEmail()`
- `UserRepository.findByEmailWithRolesAndPermissions()`
- `RoleRepository.findByName()`
- `RoleRepository.findAllWithPermissions()`

### Inventory Module (`modules/inventory/`)

**Entities:**
- `Brand`, `Category`, `Supplier`, `UnitOfMeasure`
- `InventoryLocation`, `UoMConversion`
- `ProductTemplate`, `ProductVariant`
- `InventoryItem`, `StockLevel`, `BatchLot`
- `StockTransaction`, `DamageLoss`
- `PurchaseOrder`, `PurchaseOrderItem`
- `Sale`, `SaleItem`, `Discount`
- `TaxRule`, `StockTransfer`

**Domain Services:**
- `StockAllocationService` - FIFO stock allocation
- `SalePricingService` - Discount calculation
- `PurchaseOrderReceiptService` - PO receipt processing

### Products Module (`modules/products/`)

Uses DDD-style value objects for some domain concepts:
- `ProductTemplateId`, `ProductVariantId`, `OrganizationId`
- `Sku`, `Barcode`, `Money`, `ProductAttributes`

Maps to inventory module entities via `JpaProductTemplateRepository` and `JpaProductVariantRepository`.

### GlobalTemplates Module (`modules/globaltemplates/`)

**Entities:**
- `GlobalTemplate` - Industry template definitions
- `GlobalTemplateItem` - Items within templates

**Domain Services:**
- `TemplateCopyService` - Copies template items to organization
- `GlobalTemplateManagementService` - Template CRUD operations

## Shared Infrastructure

### Audit Logging (`shared/infrastructure/audit/`)

```java
@Service
public class AuditLogServiceImpl implements AuditLogService {
    public AuditLogBuilder builder() { ... }
}

// Usage
auditLogService.builder()
    .action("CREATE_BRAND")
    .entityId(brand.getId())
    .message("Brand created: " + brand.getName())
    .log();
```

### Exception Handling (`shared/domain/exception/`)

- `ResourceNotFoundException` - 404 responses
- `ValidationException` - Validation failures
- `AuthorizationException` - Access denied
- `DomainException` - Base domain exception
- `DuplicateEntityException` - Unique constraint violations

## Configuration

### Application Properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/triveni_mgmt_db
spring.jpa.hibernate.ddl-auto=validate

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=900000        # 15 minutes
jwt.refresh-expiration-ms=604800000  # 7 days

# Server
server.port=8080
server.shutdown=graceful
```

### Security Configuration

- JWT authentication with HttpOnly cookies
- CORS configured for frontend URL
- Method-level security with `@PreAuthorize`
- Rate limiting (configurable)

## API Endpoints

All V2 endpoints follow the pattern: `/api/v2/{module}/{resource}`

### Auth (`/api/v2/auth`)
- POST `/login` - Login
- POST `/register` - Register
- POST `/refresh` - Refresh token
- POST `/logout` - Logout
- GET `/me` - Get current user

### Inventory (`/api/v2/inventory`)
- `/brands` - Brand CRUD
- `/categories` - Category CRUD
- `/suppliers` - Supplier CRUD
- `/locations` - Location CRUD
- `/units-of-measure` - UoM CRUD
- `/uom-conversions` - UoM conversion CRUD
- `/items` - Inventory item management
- `/stock` - Stock checks
- `/batch-lots` - Batch/lot management
- `/transactions` - Stock transactions
- `/damage-loss` - Damage/loss records
- `/discounts` - Discount management
- `/purchase-orders` - PO management
- `/sales` - Sales processing

### Products (`/api/v2/products`)
- `/templates` - Product template CRUD
- `/variants` - Product variant CRUD

### Global Templates (`/api/v2/global-templates`)
- GET `/` - List templates
- GET `/{id}` - Get template
- POST `/apply` - Apply template to organization

## Testing

```bash
./gradlew test           # Run all tests
./gradlew compileJava    # Compile only
./gradlew bootRun        # Run application
```

## Adding New Features

1. **Create Command/Query**: Define record in `application/command/` or `application/query/`
2. **Create Handler**: Implement `CommandHandler<C,R>` or `QueryHandler<Q,R>`
3. **Create/Update Entity**: JPA entity in `domain/model/`
4. **Create/Update Repository**: Spring Data interface in `domain/repository/`
5. **Create DTOs**: Request/Response DTOs in `application/dto/`
6. **Create Controller**: REST controller in `infrastructure/web/`
7. **Register Routes**: Add to `SecurityConfig` if needed
