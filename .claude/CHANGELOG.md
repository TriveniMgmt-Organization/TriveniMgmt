# Changelog

> **Purpose**: Track features, bug fixes, and decisions for context continuity.
> **Format**: Newest entries at top. Update after each significant change.

---

## 2026-01-24

### Architecture: Pragmatic Clean Architecture Fix

**Completed critical architecture fixes for multi-tenant isolation and module decoupling.**

#### Why "Pragmatic" Clean Architecture?

We chose pragmatic over strict clean architecture for these reasons:

| Strict Approach | Pragmatic Approach (Chosen) |
|-----------------|----------------------------|
| Pure domain POJOs + separate JPA entities | JPA entities in domain layer |
| ~2x code (domain + persistence + mappers) | Less boilerplate |
| Framework-agnostic domain | Spring Boot committed |

**Key insight**: The real architectural benefit comes from **module decoupling via UUID references**, not from hiding `@Entity` annotations. A `Category` with JPA annotations but no cross-module imports is cleaner than a pure POJO that imports `Organization` entity.

**What we enforce strictly:**
- ✅ No cross-module entity relationships (UUID only)
- ✅ CQRS with CommandBus/QueryBus
- ✅ Automatic tenant isolation (Hibernate filters)

**What we allow:**
- JPA annotations (`@Entity`, `@Column`) in domain models
- Spring Data repositories in domain layer

#### Problem Addressed
| Problem | Severity | Impact |
|---------|----------|--------|
| Cross-module entity references | HIGH | Modules couldn't be tested/deployed independently |
| User ↔ UserOrganizationRole circular dependency | HIGH | Refactoring cascades unexpectedly |
| No automatic tenant filtering | CRITICAL | Security risk - data leakage between tenants |

#### Changes Made

**1. Cross-Module Dependencies Broken (UUID-based References)**

Entities now reference other modules by UUID only, not by entity:

| Module | Entity | Changed Fields |
|--------|--------|----------------|
| Inventory | All entities | `organizationId`, `storeId`, `userId` (UUIDs) |
| Organization | UserOrganizationRole | `userId`, `roleId` (UUIDs) |
| Organization | Invitation | `roleId` (UUID) |
| Organization | UserAssignment | `userId`, `roleId` (UUIDs) |
| Auth | RefreshToken | `userId` (UUID) |
| Users | User | Removed `organizationRoles` field |

**Pattern applied:**
```java
// BEFORE (cross-module dependency)
@ManyToOne
@JoinColumn(name = "organization_id")
private Organization organization;

// AFTER (decoupled - UUID only)
@Column(name = "organization_id", nullable = false)
private UUID organizationId;
```

**2. Hibernate Tenant Filter Added**

- Created `package-info.java` with `@FilterDef` for tenant filter
- Added `@Filter` annotations to tenant-scoped entities
- Created `TenantFilter.java` service to enable/disable filter
- Created `TenantFilterAspect.java` for automatic filter enablement

**Tenant-filtered entities:**
- Category, ProductTemplate, ProductVariant, Supplier
- UnitOfMeasure, TaxRule, Discount
- PurchaseOrder, DamageLoss

**3. Repository Queries Updated**

Fixed all JPA queries referencing removed entity relationships:
- `pt.organization.id` → `pt.organizationId`
- `v.organization.id` → `v.organizationId`
- `ur.user.id` → `ur.userId`
- `i.location.store.id` → `i.location.storeId`

**4. Deprecated Methods**

Repository methods with old patterns deprecated with delegation:
- `JpaUserRepository.findAllWithRolesAndPermissions()` → `findAll()`
- `JpaUserRepository.findByIdWithRolesAndPermissions()` → `findById()`
- `RefreshTokenRepository.findByTokenWithUser()` → `findByToken()`

**Files Created:**
- `modules/inventory/domain/model/package-info.java` - Tenant filter definition
- `shared/infrastructure/security/TenantFilter.java` - Filter service
- `config/TenantFilterAspect.java` - AOP aspect for automatic filtering

**Files Modified (~30 files):**
- All inventory domain entities (Category, ProductTemplate, ProductVariant, Supplier, etc.)
- UserOrganizationRole, UserAssignment, Invitation (organization module)
- RefreshToken (auth module)
- User (users module - removed organizationRoles)
- Multiple repository interfaces (query fixes)
- Multiple handlers and mappers (UUID getter/setter changes)

**Intra-Module Relationships Preserved:**
- Store → Organization (within organization module)
- UserOrganizationRole → Organization, Store (within organization module)
- Invitation → Organization, Store (within organization module)

**Build Verified:** `./gradlew test` - BUILD SUCCESSFUL

---

### Architecture: Modular Monolith Restructure Complete

**Completed full restructure to modular monolith with clean architecture:**

**New Package Structure:**
```
com.store.mgmt/
├── modules/                    # Feature Modules
│   ├── auth/                   # Authentication & Authorization
│   ├── organization/           # Organization & Store management
│   ├── users/                  # User, Role, Permission
│   ├── inventory/              # Stock, Transactions, PO, Sales
│   ├── products/               # Product Templates & Variants
│   └── globaltemplates/        # Industry templates
├── shared/                     # Shared Kernel
│   ├── application/            # Command/Query interfaces
│   ├── domain/                 # BaseEntity, exceptions
│   └── infrastructure/         # CommandBus, QueryBus, security
└── config/                     # Application Configuration
```

**Shared Kernel Created:**
- `shared/domain/model/BaseEntity.java` - Base JPA entity with UUID, audit fields, soft delete
- `shared/domain/exception/ResourceNotFoundException.java` - Common 404 exception
- `shared/infrastructure/audit/AuditLog.java` - Audit log entity
- `shared/infrastructure/audit/AuditLogRepository.java` - Audit log repository
- `shared/application/command/Command.java`, `CommandHandler.java` - CQRS command interfaces
- `shared/application/query/Query.java`, `QueryHandler.java` - CQRS query interfaces
- `shared/infrastructure/CommandBus.java` - Command dispatcher
- `shared/infrastructure/QueryBus.java` - Query dispatcher

**Module Structure Pattern (each module):**
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
    ├── persistence/       # Custom JPA implementations
    └── service/           # Infrastructure services
```

**Legacy Code Removed:**
- Deleted duplicate repositories in `organization/repository/`, `users/repository/`
- Deleted duplicate entities in `organization/model/entity/`, `users/model/entity/`
- Deleted `globaltemplates/repository/`, `globaltemplates/model/entity/`
- Deleted `utils/` directory
- Removed `auth/model/entity/RefreshToken.java` (moved to modules)
- Removed `organization/enums/StoreStatus.java` (moved to modules)

**Import Updates:**
- Updated all imports from `com.store.mgmt.common.*` to `com.store.mgmt.shared.*`
- Updated entity imports to use new module paths
- Updated repository imports to use new module paths

**Entities Migrated:**
| Module | Entities |
|--------|----------|
| auth | RefreshToken |
| organization | Organization, Store, UserOrganizationRole, UserAssignment, Invitation, StoreStatus |
| users | User, Role, Permission, RoleType |
| inventory | Brand, Category, Supplier, UoM, Location, ProductTemplate, ProductVariant, InventoryItem, StockLevel, BatchLot, StockTransaction, DamageLoss, PurchaseOrder, Sale, Discount, TaxRule |
| globaltemplates | GlobalTemplate, GlobalTemplateItem |

**Documentation Updated:**
- `.claude/CONTEXT.md` - Complete rewrite with new architecture
- `.claude/docs/BACKEND.md` - Complete rewrite with CQRS patterns and module structure

**Build Verified:** `./gradlew compileJava` - BUILD SUCCESSFUL

### Entity Consolidation: Domain + JPA Merged

**Simplified architecture by merging domain POJOs with JPA entities:**

Previously there were separate classes:
- Domain: `User`, `Role`, `Permission` (POJOs in `domain/model/`)
- JPA: `UserEntity`, `RoleEntity`, `PermissionEntity` (JPA entities in `infrastructure/persistence/entity/`)
- Mappers to convert between them

Now consolidated to single JPA entities in `domain/model/`:
- `User.java` - Full JPA entity with `organizationRoles` relationship
- `Role.java` - Full JPA entity with `permissions` relationship
- `Permission.java` - Full JPA entity

**Files Deleted:**
- `infrastructure/persistence/entity/UserEntity.java`
- `infrastructure/persistence/entity/RoleEntity.java`
- `infrastructure/persistence/entity/PermissionEntity.java`
- `infrastructure/persistence/mapper/UserMapper.java`
- `infrastructure/persistence/mapper/RoleMapper.java`
- `infrastructure/persistence/mapper/PermissionMapper.java`
- `shared/domain/model/DomainEntity.java`

**Files Updated:**
- `UserOrganizationRole.java` - Now references `User` and `Role` directly
- `JpaUserRepository.java` - Returns `User` instead of `UserEntity`
- `JpaRoleRepository.java` - Returns `Role` instead of `RoleEntity`
- `JpaPermissionRepository.java` - Returns `Permission` instead of `PermissionEntity`
- Persistence adapters simplified to just delegate (no mapping needed)
- `DataSeeder.java` - Uses domain models directly

**Rationale:**
For a modular monolith, having separate domain POJOs and JPA entities added complexity without benefit. Other entities (Organization, Store, etc.) were already direct JPA entities in domain layer. This consolidation makes the codebase consistent.

**Build Verified:** `./gradlew compileJava` - BUILD SUCCESSFUL

---

### V1 to V2 Migration: 100% COMPLETE

**All 17 V2 modules implemented:**
- Auth, Global Templates, Products, Inventory Items
- Brands, Categories, Suppliers, Locations
- Units of Measure, UoM Conversions, Stock Checks, Batch/Lots
- Stock Transactions, Damage/Loss, Discounts
- Purchase Orders, Sales

**V2 Architecture:**
- CQRS with CommandBus/QueryBus dispatch
- Clean Architecture layers: application (command, query, dto, service), domain (service), infrastructure (web)
- All 79 V2 endpoints at `/api/v2/*`

**Key V2 Domain Services Created:**
- `StockAllocationService` - FIFO stock allocation with expiry date sorting
- `SalePricingService` - Discount calculation (PERCENTAGE and FIXED_AMOUNT)
- `PurchaseOrderReceiptService` - PO receipt processing with batch lot creation

**V1 Code Removed:**
- Controllers: AuthController, GlobalTemplateController, InventoryController, PosController
- Services: InventoryService, InventoryServiceImpl
- Mappers: 18 files in inventory/mapper/
- DTOs: 51 files in inventory/model/dto/

**V1 Code Kept (still used by V2):**
- auth/service/* - Used by V2 auth handlers (JWTService, etc.)
- globaltemplates/service/* - Used by V2 global templates handlers
- All entities, repositories, and enums (shared infrastructure)

**Files Created (Summary):**
- ~150 new V2 files in `src/main/java/com/store/mgmt/modules/`
- Controllers in `infrastructure/web/`
- Commands/Queries/Handlers in `application/`
- Domain services in `domain/service/`
- Mappers in `application/service/`

**Build Verified:** `./gradlew compileJava` - BUILD SUCCESSFUL

---

## 2026-01-23 (Session 2)

### Inventory Creation Flow: Performance & Production Improvements

**Backend Improvements:**

1. **N+1 Query Prevention** - Added optimized repository queries:
   - `findByStoreIdWithDetails()` - Uses JOIN FETCH for variant, template, location, batchLot, stockLevel
   - `findByIdWithDetails()` - Single item with all relations eagerly loaded
   - Updated `getAllInventoryItems()` and `getInventoryItemById()` to use optimized queries

2. **DTO Validation** - Added validation annotations to `CreateInventoryItemDTO`:
   - `@NotNull` on variantId and locationId
   - `@Size(max=100)` on customBatchNumber
   - `@Min(0)` on initialQuantity and lowStockThreshold

3. **New Features** - Added optional fields to inventory item creation:
   - `initialQuantity` - Set initial stock when creating (defaults to 0)
   - `lowStockThreshold` - Set alert threshold (defaults to 10)

4. **Mapper Improvements** - Added named mapping methods for nested DTOs:
   - `ProductVariantMapper.toVariantSummary()` - Includes template info
   - `InventoryLocationMapper.toLocationSummary()` - Essential location info
   - `BatchLotMapper.toBatchLotSummary()` - Essential batch info
   - Updated `InventoryItemMapper` to use these for complete DTO population

**Frontend Improvements:**

1. **New Form Fields:**
   - Initial Quantity field with validation
   - Low Stock Threshold field with validation

2. **Better Error Handling:**
   - Replaced `any` types with proper TypeScript typing
   - Fixed floating promise warnings with `void` operator

3. **UX Improvements:**
   - Added redirect to items list after successful creation
   - Fixed loading state bug (was showing error display during load)
   - Proper cache invalidation after mutations

4. **Form Schema Updates:**
   - Added yup validation for initialQuantity and lowStockThreshold
   - Number transform to handle empty strings

**Files Modified:**

Backend:
- `inventory/model/dto/CreateInventoryItemDTO.java` - Added validation, new fields
- `inventory/repository/InventoryItemRepository.java` - Added JOIN FETCH queries
- `inventory/service/InventoryServiceImpl.java` - Use optimized queries, handle new fields
- `inventory/mapper/InventoryItemMapper.java` - Use named mapping methods
- `inventory/mapper/ProductVariantMapper.java` - Added toVariantSummary
- `inventory/mapper/InventoryLocationMapper.java` - Added toLocationSummary
- `inventory/mapper/BatchLotMapper.java` - Added toBatchLotSummary

Frontend:
- `store/[storeId]/inventory/apiSchema.ts` - Added new field validations
- `store/[storeId]/inventory/items/add/page.tsx` - New fields, fixed errors
- `store/[storeId]/inventory/items/page.tsx` - Fixed loading state, error types
- `components/forms/inventory-item/InventoryItemForm.tsx` - Added new fields

---

### Template Selection UI: Complete Redesign

**Improved Organization Settings page with card-based template selection:**

**Backend Changes:**
- Added `description` field to `GlobalTemplateDTO` (was missing, entity had it)
- Added `itemCounts` field to `GlobalTemplateDTO` - maps entity type to count
- Removed orphaned `version` field from DTO (entity doesn't have it)
- Updated `GlobalTemplateMapper` to populate `itemCounts` from template items

**Frontend Changes:**
- Replaced dropdown with **template cards** showing:
  - Template name and type badge
  - Description text
  - Item counts by entity type (Brands, Categories, UOMs, Products, etc.)
  - "Apply Template" button per card
- Added **confirmation dialog** before applying (critical for one-time operation)
  - Shows template name and description
  - Lists all items that will be created
  - Warning about irreversible operation
- Added **loading skeleton** cards while templates load
- Added **applied template display** after application:
  - Success state with green styling
  - Shows what was created (entity counts with icons)
  - Info message about customization
- Removed confusing "Custom (No Template)" option
- Added entity type icons and labels for better visual understanding
- Fixed TypeScript lint errors

**Files Modified:**
- `backend/.../globaltemplates/model/dto/GlobalTemplateDTO.java`
- `backend/.../globaltemplates/mapper/GlobalTemplateMapper.java`
- `frontend/src/app/(dashboard)/organization/settings/page.tsx`
- `frontend/src/api/generated/models/globalTemplate.ts` (regenerated)

---

### TemplateCopyService: Added Supplier Support

**Added Supplier entity support to TemplateCopyService:**
- Templates can now copy Supplier entities to organizations
- Suppliers are organization-scoped (each org gets its own copy)
- Supports fields: name (required), contactPerson, email, phone, address, accountNumber
- Skips duplicates by checking name + organizationId

**Supported entity types now:**
| Entity Type | Scope | Notes |
|-------------|-------|-------|
| Brand | Global | Shared across all organizations |
| Category | Organization | Hierarchical with parentCode |
| UnitOfMeasure | Organization | Code + Name |
| ProductTemplate | Organization | Creates ProductVariant automatically |
| TaxRule | Organization | Country code + rate |
| InventoryLocation | Store | Assigned to first store |
| Supplier | Organization | **NEW** |

**Unsupported (intentionally skipped):**
- DAMAGE_LOSS_REASON: Uses enum `DamageLossReason`, not a database entity
- PAYMENT_METHOD: Not yet implemented as entity

**Files Modified:**
- `src/main/java/com/store/mgmt/globaltemplates/service/TemplateCopyService.java`

---

### Global Templates: Industry-Ready 10 Store Types

**Created comprehensive global templates for 10 common store types:**

| Code | Type | Description |
|------|------|-------------|
| RETAIL_PRO | General Retail | Apparel, gifts, home goods |
| GROCERY_PRO | Grocery/Supermarket | Fresh produce, dairy, frozen, bakery, meat |
| PHARMACY_PRO | Pharmacy | OTC, prescriptions, vitamins, health & beauty |
| GAS_STATION_PRO | Gas Station | Fuel, convenience store, tobacco |
| LIQUOR_PRO | Liquor Store | Wine, beer, spirits |
| ELECTRONICS_PRO | Electronics | Computers, phones, TVs, gaming, audio |
| HARDWARE_PRO | Hardware/Home Improvement | Tools, lumber, plumbing, electrical, paint |
| RESTAURANT_PRO | Restaurant/Cafe | Food service ingredients, beverages, supplies |
| BEAUTY_PRO | Beauty/Cosmetics | Skincare, makeup, haircare, fragrance |
| RETAIL_BASIC | Basic Retail | Minimal template for small shops |

**Each template includes:**
- 5-10 common brands for that industry
- Hierarchical category structure (parent/child)
- Appropriate units of measure (UOM)
- Tax rules by country
- 8-15 common product templates with realistic pricing
- 2-3 supplier contacts
- 15-25 store locations (sections, aisles, storage)
- Damage/loss reason codes

**Fixed issues in existing templates:**
- GROCERY_PRO: Added missing `GAL` UOM and `CONDIMENTS` category
- PHARMACY_PRO: Added missing `FIRST_AID` category
- RETAIL_BASIC: Fixed inconsistent JSON format (missing `data` wrapper)
- Standardized field naming (consistent snake_case in JSON)

**Files Created:**
- `seeds/globaltemplates/electronics-store.json`
- `seeds/globaltemplates/hardware-store.json`
- `seeds/globaltemplates/restaurant-cafe.json`
- `seeds/globaltemplates/beauty-store.json`

**Files Updated:**
- `seeds/globaltemplates/grocery-pro.json` (v2)
- `seeds/globaltemplates/pharmecy_starter.json` (v2)
- `seeds/globaltemplates/retail-basic.json` (v2)

---

### Documentation: Added Business Context

**Updated CONTEXT.md with business context section:**
- Target market: SMBs with 2-20 stores (retail chains, F&B distributors, wholesale)
- Value proposition: Template-based catalog, multi-location tracking, batch/lot support
- Feature scope by level (Organization vs Store)
- Positioning against competitors (Zoho Inventory, Cin7, inFlow)

**Recommendations documented for future implementation:**
- Low stock alerts and notifications
- Stock transfers between stores/locations
- Inventory adjustments with audit trail
- Demand forecasting and analytics
- GRN (Goods Received Notes) for partial PO receiving
- Multi-currency support
- Tax configuration per store/product

### Frontend: Authentication & UI Fixes

**Fixed cookie name mismatch causing login redirect failures:**
- `/api/me/route.ts` used `session-token` but middleware uses `session_token`
- Changed to consistent `session_token` (with underscore)

**Improved AuthContext error detection:**
- Enhanced `isAuthError()` to check both `error.status` and `error.response?.status`
- Added `isRedirecting` ref to prevent multiple concurrent redirects
- Proper redirect to login on 401/403 errors

**Removed misleading notification from NavigationContext:**
- Auth errors now handled centrally by AuthContext
- NavigationContext no longer shows "Error fetching stores" for auth failures

**Made empty state UI consistent:**
- Updated Templates page to match Categories empty state pattern
- Added Alert component with "Create First Template" button

### Backend: Reduced Logging Verbosity

**Updated application-dev.properties:**
- Changed `org.hibernate.SQL` to WARN (was DEBUG)
- Changed `org.hibernate.type.descriptor.sql.BasicBinder` to WARN
- Added comments for easy re-enabling during debugging

**Files Modified:**
- `backend/.claude/CONTEXT.md`
- `backend/src/main/resources/application-dev.properties`
- `frontend/src/contexts/AuthContext.tsx`
- `frontend/src/contexts/NavigationContext.tsx`
- `frontend/src/app/api/me/route.ts`
- `frontend/src/app/(dashboard)/organization/product-catalog/templates/page.tsx`

---

## 2026-01-23

### Backend: Fixed LazyInitializationException in Multiple Services

**Issue:**
- After Phase 4 EAGER→LAZY fetch optimization, multiple endpoints fail with:
  `LazyInitializationException: failed to lazily initialize collection of role: User.organizationRoles`

**Root Cause:**
- Service methods accessing User entities without eagerly fetching `organizationRoles`
- Mapper tries to access lazy-loaded collections after Hibernate session closes

**Fixes Applied:**

1. **AuthServiceImpl.validateToken()**
   - Changed to use `findByEmailWithRolesAndPermissions()`
   - Added `@Transactional(readOnly = true)`

2. **UserServiceImpl.getUser() and getAllUsers()**
   - Added new repository methods:
     - `findByIdWithRolesAndPermissions(UUID id)`
     - `findAllWithRolesAndPermissions()`
   - Added `@Transactional(readOnly = true)` to both methods
   - Use JOIN FETCH to eagerly load roles, permissions, organization, store

**Files Modified:**
- `src/main/java/com/store/mgmt/auth/service/AuthServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/UserServiceImpl.java`
- `src/main/java/com/store/mgmt/users/repository/UserRepository.java`

**Pattern to follow for future:**
- Any method accessing User and mapping to DTO must use eager-fetch query
- Add `@Transactional(readOnly = true)` for read operations

---

### Frontend: Navigation Performance Optimizations (Security-Balanced)

**React Query caching improved:**
- Enabled `staleTime: 5 minutes` - prevents refetch on navigation
- Enabled `gcTime: 30 minutes` - longer cache retention
- Disabled `refetchOnWindowFocus` - no unnecessary refetches
- Disabled `refetchOnMount` - uses cached data if fresh
- Reduced `retry: 1` - faster error handling

**Middleware simplified to industry best practice:**
- Cookie existence check only (no API calls in middleware)
- No JWT secret in frontend (security best practice)
- Middleware just guards routes, AuthContext does full validation
- AuthContext calls `/api/me` → backend validates → handles 401 errors
- Faster navigation (no API calls on every route change)
- Proper separation: Middleware=routing, AuthContext=authentication

**Navigation prefetching added:**
- Added `prefetchRoute()` to NavigationContext
- Routes are prefetched on hover in DynamicNavigation
- Faster perceived navigation speed

**Loading states improved:**
- Replaced "Loading..." text with skeleton loaders
- Table-like skeleton structure matches actual content
- Added `loading.tsx` to `/organization/` route segment
- Added `loading.tsx` to `/store/[storeId]/` route segment
- Each segment has appropriate skeleton layout

**Files Modified (Frontend):**
- `src/components/Providers/ReactQueryProvider.tsx`
- `src/middleware.ts` - Security-balanced auth with JWT verification
- `src/contexts/NavigationContext.tsx`
- `src/components/navigation/DynamicNavigation.tsx`
- `src/app/(dashboard)/loading.tsx`
- `src/app/(dashboard)/organization/loading.tsx` (new)
- `src/app/(dashboard)/store/[storeId]/loading.tsx` (new)

**Documentation Updated:**
- `.claude/docs/FRONTEND.md` - Added Performance Optimizations section

---

### Frontend: Compatibility Updates for Backend Changes

**Added 409 CONFLICT handling:**
- `light-client.ts` - Added 409 status code handling
- `error-messages.ts` - Added 409 status code handling
- Added `isConflictError()` helper function
- Added `isValidationError()` helper function
- Updated `getErrorTitle()` to return appropriate titles for conflict/validation errors

**Added X-Correlation-ID support:**
- `light-client.ts` - Generates and sends correlation ID header
- `api-client.ts` - Generates and sends correlation ID header
- Enables end-to-end request tracing with backend logs

**Updated error handling documentation:**
- `ERROR_HANDLING_GUIDE.md` - Added new error type helpers

**Files Modified (Frontend):**
- `src/lib/client/light-client.ts`
- `src/lib/server/api-client.ts`
- `src/utils/error-messages.ts`
- `ERROR_HANDLING_GUIDE.md`

---

### Cleanup: Phase 6 Code Cleanup Complete

**Removed dead commented code:**
- `Organization.java` - Removed old equals/hashCode (now handled by Lombok)
- `UserOrganizationRole.java` - Removed old equals/hashCode (now handled by Lombok)
- `OrganizationRepository.java` - Removed commented User queries
- `UserAssignmentRepository.java` - Removed commented User queries
- `PosServiceImpl.java` - Cleaned up unused imports and placeholder code
- `RateLimitConfig.java` - Cleaned up commented code, added TODO note
- `SecurityConfig.java` - Removed stale TODO comment

**Removed unused imports:**
- `Organization.java` - Removed HibernateProxy import
- `UserOrganizationRole.java` - Removed Objects import

**Files Modified:**
- `src/main/java/com/store/mgmt/organization/model/entity/Organization.java`
- `src/main/java/com/store/mgmt/organization/model/entity/UserOrganizationRole.java`
- `src/main/java/com/store/mgmt/organization/repository/OrganizationRepository.java`
- `src/main/java/com/store/mgmt/organization/repository/UserAssignmentRepository.java`
- `src/main/java/com/store/mgmt/pos/service/PosServiceImpl.java`
- `src/main/java/com/store/mgmt/config/security/RateLimitConfig.java`
- `src/main/java/com/store/mgmt/config/security/SecurityConfig.java`

---

### Infrastructure: Phase 5 Production Infrastructure Complete

**Added Correlation ID Filter:**
- New `CorrelationIdFilter` for distributed request tracing
- Generates UUID if `X-Correlation-ID` header not provided
- Adds correlation ID to MDC for logging
- Returns correlation ID in response header

**Added Request Logging:**
- New `RequestLoggingConfig` with `CommonsRequestLoggingFilter`
- Enabled via `logging.request.enabled=true`
- Logs request details (query string, payload, client info)
- Headers excluded to prevent auth token exposure

**Added Startup Validation:**
- New `ApplicationStartupValidator` component
- Validates JWT secret configuration
- Validates FRONTEND_URL for CORS
- Tests database connectivity on startup
- Fails startup in production if critical configs missing
- Warns about dev secrets used in non-dev profiles

**Added Logback Configuration:**
- New `logback-spring.xml` with correlation ID in log pattern
- Profile-specific logging levels (dev=DEBUG, prod=WARN)
- Rolling file appender for production (100MB files, 30 days retention)

**Added Graceful Shutdown:**
- `server.shutdown=graceful` for clean shutdown
- 30 second timeout for in-flight requests

**Added Response Compression:**
- Enabled GZIP compression for JSON/XML/HTML
- Minimum response size 1024 bytes

**Updated CORS Configuration:**
- Added `X-Correlation-ID` to allowed/exposed headers

**Updated Actuator:**
- Added `/actuator/health/**` and `/actuator/info` to public endpoints
- Enabled Kubernetes-style health probes

**Files Created:**
- `src/main/java/com/store/mgmt/config/CorrelationIdFilter.java`
- `src/main/java/com/store/mgmt/config/RequestLoggingConfig.java`
- `src/main/java/com/store/mgmt/config/ApplicationStartupValidator.java`
- `src/main/resources/logback-spring.xml`

**Files Modified:**
- `src/main/java/com/store/mgmt/config/security/SecurityConfig.java`
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`

---

### Performance: Phase 4 Database & Performance Complete

**Fixed N+1 query potential:**
- Changed `User.organizationRoles` from `FetchType.EAGER` to `FetchType.LAZY`
- Repositories already have optimized JOIN FETCH queries (`findByUsernameWithAllRelatedData`, etc.)

**Added database indexes:**
- `stores` table:
  - `idx_store_organization` on `organization_id`
  - `idx_store_status` on `status`
- `user_organization_roles` table:
  - `idx_uor_user` on `user_id`
  - `idx_uor_organization` on `organization_id`
  - `idx_uor_store` on `store_id`
  - `idx_uor_role` on `role_id`
  - `idx_uor_user_org` on `user_id, organization_id` (composite)

**Verified existing optimizations:**
- `@Version` in BaseEntity for optimistic locking ✓
- `open-in-view=false` to prevent lazy loading in views ✓
- HikariCP connection pool configured ✓
- Inventory entities already have comprehensive indexes ✓

**Files Modified:**
- `src/main/java/com/store/mgmt/users/model/entity/User.java`
- `src/main/java/com/store/mgmt/organization/model/entity/Store.java`
- `src/main/java/com/store/mgmt/organization/model/entity/UserOrganizationRole.java`

---

### Review: Phase 1-3 Gap Fixes

**Added `@Slf4j` to controllers missing logging:**
- `UserController`
- `RoleController`
- `InventoryController`
- `GlobalTemplateController`
- `PosController`

**Added `@Slf4j` to services missing logging:**
- `RoleServiceImpl`

**Fixed RuntimeException throws:**
- `AuditLogServiceImpl._persistAuditLog()` - Now logs error but doesn't throw
- `AuthServiceImpl.logAuditEntry()` - Now logs error but doesn't throw
- `OrganizationServiceImpl.logAuditEntry()` - Now logs error but doesn't throw
- `EmailService.sendInvitationEmail()` - Changed to `ResponseStatusException`

**Fixed EmailService bugs:**
- Fixed `@Value("${FRONTEND_URL")` → `@Value("${FRONTEND_URL}")`
- Added default for `spring.mail.from`

**Fixed HTTP status codes:**
- `RoleServiceImpl.createRole()` - Changed "already exists" from BAD_REQUEST to CONFLICT
- `RoleServiceImpl.updateRole()` - Changed "already exists" from BAD_REQUEST to CONFLICT

**Removed redundant validation:**
- `RoleServiceImpl.createRole()` - Removed null check (handled by @Valid)

**Files Modified:**
- `src/main/java/com/store/mgmt/users/controller/UserController.java`
- `src/main/java/com/store/mgmt/users/controller/RoleController.java`
- `src/main/java/com/store/mgmt/inventory/controller/InventoryController.java`
- `src/main/java/com/store/mgmt/globaltemplates/controller/GlobalTemplateController.java`
- `src/main/java/com/store/mgmt/pos/controller/PosController.java`
- `src/main/java/com/store/mgmt/users/service/RoleServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/AuditLogServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/EmailService.java`
- `src/main/java/com/store/mgmt/auth/service/AuthServiceImpl.java`
- `src/main/java/com/store/mgmt/organization/service/OrganizationServiceImpl.java`

---

### Validation: Phase 3 Input Validation Complete

**Added validation annotations to DTOs:**
- `CreateUserDTO` - @NotBlank, @Email, @Size for email and name fields
- `CreateOrganizationDTO` - @NotBlank, @Size for name, description, contactInfo
- `CreateStoreDTO` - @NotNull for organizationId, @NotBlank for name/location, @Size limits
- `InviteUserDTO` - @NotBlank, @Email for email, @NotNull for organizationId, @NotBlank for roleName
- `CreateUserAssignmentDTO` - @NotNull for organizationId, userId, roleId
- `RemoveUserAssignmentDTO` - @NotNull for organizationId, userId
- `CreateTenantDTO` - @NotNull for organizationId
- `RoleDTO` - @NotBlank, @Size for name and description

**Cleaned up redundant validation:**
- Removed manual null checks from `UserServiceImpl.createUser()` (handled by @NotBlank)
- Removed manual null checks from `OrganizationServiceImpl.createOrganization()` (handled by @NotBlank)
- Changed duplicate check errors from BAD_REQUEST to CONFLICT (proper HTTP semantics)

**Files Modified:**
- `src/main/java/com/store/mgmt/users/model/dto/CreateUserDTO.java`
- `src/main/java/com/store/mgmt/users/model/dto/RoleDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateOrganizationDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateStoreDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/InviteUserDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateUserAssignmentDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/RemoveUserAssignmentDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateTenantDTO.java`
- `src/main/java/com/store/mgmt/users/service/UserServiceImpl.java`
- `src/main/java/com/store/mgmt/organization/service/OrganizationServiceImpl.java`

---

### Code Quality: Phase 2 Architecture Improvements Complete

**Added missing `@Valid` annotations:**
- `OrganizationController.createOrganization()`
- `UserController.createUser()`, `updateUser()`, `inviteUser()`
- `UserController.assignUserToOrganization()`, `assignUserToStore()`
- `UserController.removeUserFromOrganization()`, `removeUserFromStore()`
- `RoleController.createRole()`, `updateRole()`

**Enhanced GlobalExceptionHandler:**
- Added `SecurityException` handler for authorization failures
- Added `IllegalStateException` handler for internal state errors

**Created shared AuthorizationService:**
- New `com.store.mgmt.common.service.AuthorizationService`
- Centralized authorization logic: `hasRole()`, `hasRoleInOrganization()`, `lacksRole()`, `lacksRoleInOrganization()`
- Added `requireRole()` and `requireRoleInOrganization()` for cleaner authorization checks
- Fixed bug in `StoreServiceImpl.hasRole()` that used `noneMatch` instead of `anyMatch`

**Refactored services to use AuthorizationService:**
- `StoreServiceImpl` - Removed private authorization methods, uses shared service
- `UserServiceImpl` - Uses shared service for authorization checks

**Files Modified:**
- `src/main/java/com/store/mgmt/common/service/AuthorizationService.java` (new)
- `src/main/java/com/store/mgmt/common/exception/GlobalExceptionHandler.java`
- `src/main/java/com/store/mgmt/organization/controller/OrganizationController.java`
- `src/main/java/com/store/mgmt/organization/service/StoreServiceImpl.java`
- `src/main/java/com/store/mgmt/users/controller/UserController.java`
- `src/main/java/com/store/mgmt/users/controller/RoleController.java`
- `src/main/java/com/store/mgmt/users/service/UserServiceImpl.java`

---

### Security: Phase 1 Critical Fixes Complete
- **Removed all `System.out.println` and `printStackTrace` calls** from application code
  - `SecurityConfig.java` - Replaced with SLF4J logger
  - `AuthController.java` - Replaced debug output with proper logging
  - `OrganizationController.java` - Removed debug code that read raw request body
  - `StoreController.java` - Removed debug code, added proper logging
  - `OrganizationServiceImpl.java` - Added @Slf4j, proper logging
  - `StoreServiceImpl.java` - Replaced with logger
  - `UserServiceImpl.java` - Replaced with logger
  - `InventoryServiceImpl.java` - Replaced with logger
  - `AuditLogServiceImpl.java` - Replaced with logger
  - Note: `Seeder.java` retains System.out (CLI utility, acceptable)

- **Removed hardcoded credentials** from `SecurityConfig.java`
  - Removed hardcoded email credentials (prasubd@gmail.com)
  - Mail configuration now loaded from environment variables
  - Added validation to ensure mail credentials are set when needed

- **Environment variable requirements enforced**
  - `application.properties` - Removed default credentials, requires env vars
  - `application-dev.properties` - Safe defaults for local development only
  - `application-prod.properties` - No defaults, all sensitive values via env vars

- **Fixed CORS configuration**
  - Replaced wildcard `*` in allowed-headers with explicit list
  - Headers: `Authorization,Content-Type,Accept,X-Requested-With,X-Store-Id,X-Organization-Id`

- **Created shared SecretKey bean** in SecurityConfig
  - Eliminates duplicate key creation for JWT encoder/decoder
  - Single source of truth for signing key

**Files Modified:**
- `src/main/java/com/store/mgmt/config/security/SecurityConfig.java`
- `src/main/java/com/store/mgmt/auth/controller/AuthController.java`
- `src/main/java/com/store/mgmt/organization/controller/OrganizationController.java`
- `src/main/java/com/store/mgmt/organization/controller/StoreController.java`
- `src/main/java/com/store/mgmt/organization/service/OrganizationServiceImpl.java`
- `src/main/java/com/store/mgmt/organization/service/StoreServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/UserServiceImpl.java`
- `src/main/java/com/store/mgmt/inventory/service/InventoryServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/AuditLogServiceImpl.java`
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`

---

### Documentation Moved to Backend Repo
- Moved `.claude/` and `CLAUDE.md` from parent folder into `backend/` repo
- Documentation is now version-controlled with the backend
- Frontend is a sibling repo at `../frontend/`

### Project Restructure
- Renamed `TriveniMgmt/` → `backend/`
- Renamed `triveni-mgmt-client/` → `frontend/`
- Updated all documentation to reflect new paths
- Git remotes unchanged (still point to original GitHub repo names)

**Note**: GitHub repos still named `TriveniMgmt` and `triveni-mgmt-client`. Local folders renamed for clarity.

### Documentation Created
- Created `.claude/` documentation structure for efficient context loading
- Generated comprehensive documentation for backend, frontend, API, database, and permissions
- Established changelog for tracking future changes

### Project State
- **Backend**: Spring Boot 3.5.3, Java 21, PostgreSQL 16
- **Frontend**: Next.js 15, React 19, MUI 5
- **Auth**: JWT with HttpOnly cookies
- **Multi-tenancy**: Organization → Store hierarchy
- **Seeded Users**: admin (SUPER_ADMIN), manager (STORE_MANAGER)

### Known Issues
- None documented yet

### Architecture Decisions
- Using Orval for API client generation from OpenAPI spec
- MapStruct for entity-DTO mapping in backend
- React Query for server state management
- Context API for client-side state (Auth, Navigation, Notifications)

---

## Template for New Entries

```markdown
## YYYY-MM-DD

### Feature: [Feature Name]
- Description of what was added
- Key files modified:
  - `path/to/file.java`
  - `path/to/component.tsx`
- Notes or decisions made

### Bug Fix: [Issue Description]
- Root cause
- Solution implemented
- Files changed

### Refactor: [Area]
- What was refactored and why
- Breaking changes (if any)

### Decision: [Topic]
- Context
- Options considered
- Decision made and rationale
```

---

## How to Use This Changelog

1. **After completing a feature**: Add an entry describing what was built
2. **After fixing a bug**: Document the issue and solution
3. **After making decisions**: Record the context and rationale
4. **Before starting work**: Read recent entries for context

This helps Claude understand recent changes without re-reading the entire codebase.
