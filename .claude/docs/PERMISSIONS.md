# Permission System (RBAC)

> **Source**: `config/DataSeeder.java`

## Architecture

```
User ─────┬────> UserOrganizationRole ─────> Role ─────> Permissions
          │              │
          │              ├── Organization (required)
          │              └── Store (optional)
          │
          └── Can have multiple UserOrganizationRoles
```

## Permissions (33 total)

### Organization & Store
| Permission | Description |
|------------|-------------|
| `ORG_READ` | View organization details |
| `ORG_WRITE` | Manage organization settings |
| `STORE_READ` | View store details |
| `STORE_WRITE` | Manage store settings |

### User Management
| Permission | Description |
|------------|-------------|
| `USER_READ` | View user details |
| `USER_WRITE` | Create, update, delete users |
| `ROLE_READ` | View role details |
| `ROLE_WRITE` | Create, update, delete roles |

### Product Catalog
| Permission | Description |
|------------|-------------|
| `PRODUCT_READ` | View product templates & variants |
| `PRODUCT_WRITE` | Manage product catalog |
| `CATEGORY_READ` | View categories |
| `CATEGORY_WRITE` | Manage categories |
| `BRAND_READ` | View brands |
| `BRAND_WRITE` | Manage brands |

### Inventory
| Permission | Description |
|------------|-------------|
| `INVENTORY_ITEM_READ` | View inventory items & stock |
| `INVENTORY_ITEM_WRITE` | Manage inventory |
| `LOCATION_READ` | View storage locations |
| `LOCATION_WRITE` | Manage locations |
| `UOM_READ` | View units of measure |
| `UOM_WRITE` | Manage units |
| `STOCK_CHECK_READ` | View stock reconciliation |

### Purchasing
| Permission | Description |
|------------|-------------|
| `SUPPLIER_READ` | View suppliers |
| `SUPPLIER_WRITE` | Manage suppliers |
| `PO_READ` | View purchase orders |
| `PO_WRITE` | Create, manage purchase orders |

### Sales
| Permission | Description |
|------------|-------------|
| `SALE_READ` | View sales transactions |
| `SALE_WRITE` | Process sales |
| `DISCOUNT_READ` | View discounts |
| `DISCOUNT_WRITE` | Manage discounts |

### Tracking
| Permission | Description |
|------------|-------------|
| `DAMAGE_LOSS_READ` | View damage/loss records |
| `DAMAGE_LOSS_WRITE` | Record damage/loss |
| `REPORT_READ` | View reports & analytics |

## Roles (8 total)

### SUPER_ADMIN
**Scope**: System-wide
**Description**: Full system access

**Permissions**: ALL (32 permissions)
```
ORG_READ, ORG_WRITE, STORE_READ, STORE_WRITE,
PRODUCT_READ, PRODUCT_WRITE, USER_READ, USER_WRITE,
ROLE_READ, ROLE_WRITE, INVENTORY_ITEM_READ, INVENTORY_ITEM_WRITE,
CATEGORY_READ, CATEGORY_WRITE, BRAND_READ, BRAND_WRITE,
SUPPLIER_READ, SUPPLIER_WRITE, LOCATION_READ, LOCATION_WRITE,
UOM_READ, UOM_WRITE, PO_READ, PO_WRITE,
SALE_READ, SALE_WRITE, DISCOUNT_READ, DISCOUNT_WRITE,
DAMAGE_LOSS_READ, DAMAGE_LOSS_WRITE, STOCK_CHECK_READ, REPORT_READ
```

### ORG_ADMIN
**Scope**: Organization-level
**Description**: Manage organization settings and all stores

**Permissions**: (30 permissions - no REPORT_READ, PRODUCT_READ/WRITE)
```
ORG_READ, ORG_WRITE, STORE_READ, STORE_WRITE,
USER_READ, USER_WRITE, ROLE_READ, ROLE_WRITE,
INVENTORY_ITEM_READ, INVENTORY_ITEM_WRITE,
CATEGORY_READ, CATEGORY_WRITE, BRAND_READ, BRAND_WRITE,
SUPPLIER_READ, SUPPLIER_WRITE, LOCATION_READ, LOCATION_WRITE,
UOM_READ, UOM_WRITE, PO_READ, PO_WRITE,
SALE_READ, SALE_WRITE, DISCOUNT_READ, DISCOUNT_WRITE,
DAMAGE_LOSS_READ, DAMAGE_LOSS_WRITE, STOCK_CHECK_READ
```

### STORE_MANAGER
**Scope**: Store-level
**Description**: Manage store operations and inventory

**Permissions**: (24 permissions)
```
STORE_READ, STORE_WRITE,
USER_READ, USER_WRITE,
PRODUCT_READ, PRODUCT_WRITE,
INVENTORY_ITEM_READ, INVENTORY_ITEM_WRITE,
CATEGORY_READ, BRAND_READ,
SUPPLIER_READ, SUPPLIER_WRITE, LOCATION_READ, LOCATION_WRITE,
UOM_READ, PO_READ, PO_WRITE,
SALE_READ, SALE_WRITE, DISCOUNT_READ,
DAMAGE_LOSS_READ, DAMAGE_LOSS_WRITE, STOCK_CHECK_READ
```

### ADMIN
**Scope**: General admin
**Description**: Administrative access (no org/store write)

**Permissions**: (23 permissions)
```
STORE_READ,
PRODUCT_READ, PRODUCT_WRITE, USER_READ, USER_WRITE, ROLE_READ,
INVENTORY_ITEM_READ, INVENTORY_ITEM_WRITE,
CATEGORY_READ, BRAND_READ,
SUPPLIER_READ, SUPPLIER_WRITE, LOCATION_READ, LOCATION_WRITE,
UOM_READ, PO_READ, PO_WRITE,
SALE_READ, SALE_WRITE, DISCOUNT_READ,
DAMAGE_LOSS_READ, DAMAGE_LOSS_WRITE, STOCK_CHECK_READ, REPORT_READ
```

### MANAGER
**Scope**: Sales and inventory
**Description**: Day-to-day operations management

**Permissions**: (14 permissions - READ heavy)
```
STORE_READ, PRODUCT_READ,
INVENTORY_ITEM_READ, CATEGORY_READ, BRAND_READ,
SUPPLIER_READ, LOCATION_READ, UOM_READ, PO_READ,
SALE_READ, SALE_WRITE, DISCOUNT_READ,
DAMAGE_LOSS_READ, STOCK_CHECK_READ
```

### CASHIER
**Scope**: Sales transactions
**Description**: Process sales at POS

**Permissions**: (5 permissions)
```
STORE_READ, SALE_READ, SALE_WRITE, DISCOUNT_READ, STOCK_CHECK_READ
```

### SUPPORT
**Scope**: User support
**Description**: Assist users with issues

**Permissions**: (3 permissions)
```
STORE_READ, USER_READ, USER_WRITE
```

### CUSTOMER
**Scope**: Limited access
**Description**: External customer portal (future)

**Permissions**: (3 permissions)
```
STORE_READ, DISCOUNT_READ, STOCK_CHECK_READ
```

## Role Hierarchy Summary

| Role | Org Mgmt | Store Mgmt | Users | Inventory | Sales | Reports |
|------|----------|------------|-------|-----------|-------|---------|
| SUPER_ADMIN | Full | Full | Full | Full | Full | Full |
| ORG_ADMIN | Full | Full | Full | Full | Full | - |
| STORE_MANAGER | - | Full | Full | Full | Full | - |
| ADMIN | - | Read | Full | Full | Full | Full |
| MANAGER | - | Read | - | Read | Full | - |
| CASHIER | - | Read | - | - | Full | - |
| SUPPORT | - | Read | Full | - | - | - |
| CUSTOMER | - | Read | - | - | Read | - |

## Security Configuration

### Endpoint Protection (`SecurityConfig.java`)

```java
// Public endpoints
.requestMatchers("/api/v1/auth/**").permitAll()

// Role-based endpoints
.requestMatchers("/api/v1/global-templates/**").hasRole("SUPER_ADMIN")
.requestMatchers("/api/v1/organizations/**").hasAnyRole("SUPER_ADMIN", "ORG_ADMIN")
.requestMatchers("/api/v1/stores/**").hasAnyRole("SUPER_ADMIN", "ORG_ADMIN", "STORE_MANAGER")

// Permission-based endpoints
.requestMatchers("/api/v1/users/**").hasAnyAuthority("USER_READ", "USER_WRITE")
.requestMatchers("/api/v1/roles/**").hasAnyAuthority("ROLE_READ", "ROLE_WRITE")
.requestMatchers("/api/v1/inventory/**").hasAnyAuthority("INVENTORY_ITEM_READ", "INVENTORY_ITEM_WRITE")
```

### Method-Level Security

```java
@PreAuthorize("hasAuthority('USER_WRITE')")
public User createUser(CreateUserDTO dto) { ... }

@PreAuthorize("hasAuthority('PRODUCT_READ')")
public List<Product> getProducts() { ... }
```

## Frontend Permission Checks

### Gatekeeper Component

```tsx
// Check single permission
<Gatekeeper permission="PRODUCT_WRITE">
  <EditButton />
</Gatekeeper>

// Check role
<Gatekeeper role="ORG_ADMIN">
  <OrgSettings />
</Gatekeeper>

// Check multiple roles
<Gatekeeper role={["SUPER_ADMIN", "ORG_ADMIN"]}>
  <AdminPanel />
</Gatekeeper>
```

### Programmatic Checks

```tsx
const { hasPermission, hasRole } = useAuth();

if (hasPermission('SALE_WRITE')) {
  // Show sale button
}

if (hasRole('STORE_MANAGER')) {
  // Show store settings
}
```

## Seeded Users

| Username | Email | Password | Role |
|----------|-------|----------|------|
| admin | admin@store.com | admin123 | SUPER_ADMIN |
| manager | manager@store.com | manager123 | STORE_MANAGER |

Each seeded user gets:
1. Their own Organization (`{username}'s Organization`)
2. A default Store (`Main Store`)
3. A UserOrganizationRole linking them

## Multi-Tenancy Notes

- Users can belong to multiple organizations with different roles
- `UserOrganizationRole` is the key entity for access control
- Store-level roles have `store_id` set; org-level roles have it null
- Frontend switches context via `X-Store-Id` header
- Backend validates access based on user's roles for the current org/store
