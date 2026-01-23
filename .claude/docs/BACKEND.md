# Backend Architecture

> **Path**: `src/main/java/com/store/mgmt/` (this repo)

## Package Structure

```
com.store.mgmt/
├── auth/           # JWT authentication
├── users/          # User, Role, Permission
├── organization/   # Organization, Store, UserOrgRole
├── inventory/      # Products, Stock, PO, Sales
├── pos/            # Point of Sale transactions
├── config/         # Security, Seeder, Audit
├── common/         # BaseEntity, Exceptions
├── globaltemplates/# Global product templates
├── reporting/      # Reports
├── seeder/         # Data seeding
└── utils/          # Constants (PermissionType, RoleType)
```

## Core Entities

### BaseEntity (`common/model/BaseEntity.java`)
All entities extend this:
```java
UUID id;                    // TimeOrderedEpoch UUID
LocalDateTime createdAt, updatedAt, deletedAt;
String createdBy, updatedBy, deletedBy;
Integer version;            // Optimistic locking
```

### User (`users/model/entity/User.java`)
```java
String username, email, passwordHash;
String firstName, lastName, imageUrl;
boolean isActive;
Set<UserOrganizationRole> organizationRoles;  // EAGER fetch
```

### Organization (`organization/model/entity/Organization.java`)
```java
String name, description, contactInfo;
String appliedTemplateCode;
List<Store> stores;
List<UserOrganizationRole> userRoles;
```

### Store (`organization/model/entity/Store.java`)
```java
Organization organization;
String name, location, countryCode, contactInfo;
StoreStatus status;  // ACTIVE, INACTIVE, CLOSED
List<Sale> sales;
```

### UserOrganizationRole (`organization/model/entity/UserOrganizationRole.java`)
Links User + Organization + Role (+ optional Store):
```java
User user;
Organization organization;
Role role;
Store store;  // Optional - null for org-level roles
```

### Role & Permission
```java
// Role
String name, description;
Set<Permission> permissions;  // ManyToMany

// Permission
String name, description;
```

## Inventory Domain

### ProductTemplate
Master product definition:
```java
Organization organization;
String name, description, imageUrl;
Category category;
Brand brand;
UnitOfMeasure baseUoM;
Integer reorderPoint;
boolean requiresExpiry, isActive;
Map<String, String> attributes;  // Dynamic attributes
List<ProductVariant> variants;
```

### ProductVariant
SKU-level definition:
```java
ProductTemplate template;
Organization organization;
String sku, barcode;
BigDecimal costPrice, retailPrice;
Map<String, String> variantAttributes;  // color, size, etc.
boolean isActive;
List<InventoryItem> inventoryItems;
```

### InventoryItem
Stock at a location:
```java
ProductVariant variant;
InventoryLocation location;
BatchLot batchLot;
LocalDate expiryDate;
StockLevel stockLevel;  // OneToOne
```

### StockLevel
```java
InventoryItem inventoryItem;
Integer quantity;
```

### Sale & SaleItem
```java
// Sale
Store store;
User processedBy;
LocalDateTime saleTimestamp;
BigDecimal totalAmount, totalDiscountAmount;
PaymentMethod paymentMethod;
String transactionId, notes;
List<SaleItem> items;

// SaleItem
Sale sale;
ProductVariant variant;
Integer quantity;
BigDecimal unitPrice, discountAmount, totalPrice;
```

### PurchaseOrder & PurchaseOrderItem
```java
// PurchaseOrder
Organization organization;
Supplier supplier;
User createdBy;
LocalDate orderDate, expectedDeliveryDate, actualDeliveryDate;
PurchaseOrderStatus status;  // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
BigDecimal totalEstimatedAmount;
String trackingNumber, notes;
List<PurchaseOrderItem> items;
```

## Services

### Auth Services
- `AuthService` / `AuthServiceImpl` - Login, register, token refresh
- `JWTService` - Token generation (15min access, 7day refresh)

### User Services
- `UserService` / `UserServiceImpl` - CRUD, role assignment
- `RoleService` / `RoleServiceImpl` - Role CRUD, permission assignment

### Organization Services
- `OrganizationService` / `OrganizationServiceImpl`
- `StoreService` / `StoreServiceImpl`
- `UserAssignmentService` - Assign users to orgs/stores

### Inventory Services
- `InventoryService` / `InventoryServiceImpl` - Products, stock, PO, sales

## Repositories

All extend `JpaRepository<Entity, UUID>`:

| Repository | Key Methods |
|------------|-------------|
| `UserRepository` | `findByEmail`, `findByUsername`, `findByEmailWithRoles` |
| `RoleRepository` | `findByName`, `findAllWithPermissions` |
| `OrganizationRepository` | `findByName` |
| `StoreRepository` | `findByOrganizationId` |
| `ProductTemplateRepository` | `findByOrganizationId` |
| `ProductVariantRepository` | `findBySku`, `findByBarcode` |
| `InventoryItemRepository` | Complex stock queries |

## DTOs & Mappers

### DTO Naming Convention
- `Create*DTO` - For creation
- `Update*DTO` - For updates
- `*DTO` - For responses

### MapStruct Mappers
Located in each domain's `mapper/` package:
- `CategoryMapper`, `BrandMapper`, `SupplierMapper`
- `InventoryItemMapper`, `PurchaseOrderMapper`
- `SaleMapper`, `DiscountMapper`, `DamageLossMapper`

## Configuration

### application.properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/triveni_mgmt_db
spring.jpa.hibernate.ddl-auto=validate  # Use create-drop in dev

# JWT
jwt.secret=<secret>
jwt.expiration-ms=900000        # 15 minutes
jwt.refresh-expiration-ms=604800000  # 7 days

# Server
server.port=8080
```

### Security Headers
- XSS Protection enabled
- Frame options: DENY
- CSP: default-src 'self'
- HttpOnly cookies for JWT

## Exception Handling

### Custom Exceptions
- `AuthenticationException` - Auth failures
- `ResourceNotFoundException` - 404 responses
- `InsufficientStockException` - Stock validation
- `DuplicateResourceException` - Unique constraint violations
- `InvalidOperationException` - Business logic violations

### GlobalExceptionHandler
Centralized `@ControllerAdvice` returning `ErrorResponse` objects.
