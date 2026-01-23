# API Reference

> **Base URL**: `http://localhost:8080/api/v1`

## Authentication

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Login with credentials |
| POST | `/auth/register` | Register new user |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Logout and invalidate tokens |
| GET | `/auth/validate-token` | Validate current token |

### Authenticated Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/auth/me` | Get current user info |
| GET | `/auth/organizations` | Get user's organizations |
| POST | `/auth/select-tenant` | Select active organization |

### Request/Response Examples

```json
// POST /auth/login
Request: { "username": "admin", "password": "admin123" }
Response: { "user": {...}, "accessToken": "...", "refreshToken": "..." }

// GET /auth/me
Response: {
  "id": "uuid",
  "username": "admin",
  "email": "admin@store.com",
  "organizationRoles": [
    { "organization": {...}, "role": {...}, "store": null }
  ]
}
```

## Users

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/users` | USER_READ |
| GET | `/users/{id}` | USER_READ |
| POST | `/users` | USER_WRITE |
| PUT | `/users/{id}` | USER_WRITE |
| DELETE | `/users/{id}` | USER_WRITE |
| POST | `/users/{id}/roles/{roleId}` | USER_WRITE |
| DELETE | `/users/{id}/roles/{roleId}` | USER_WRITE |
| POST | `/users/invite` | SUPER_ADMIN |
| POST | `/users/assign/organization` | USER_WRITE |
| POST | `/users/assign/store` | USER_WRITE |
| POST | `/users/remove/organization` | USER_WRITE |
| POST | `/users/remove/store` | USER_WRITE |

## Roles

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/roles` | ROLE_READ |
| GET | `/roles/{id}` | ROLE_READ |
| POST | `/roles` | ROLE_WRITE |
| PUT | `/roles/{id}` | ROLE_WRITE |
| DELETE | `/roles/{id}` | ROLE_WRITE |
| POST | `/roles/{id}/permissions/{permissionId}` | ROLE_WRITE |
| DELETE | `/roles/{id}/permissions/{permissionId}` | ROLE_WRITE |

## Organizations

| Method | Endpoint | Role |
|--------|----------|------|
| GET | `/organizations` | SUPER_ADMIN, ORG_ADMIN |
| GET | `/organizations/{id}` | SUPER_ADMIN, ORG_ADMIN |
| POST | `/organizations` | SUPER_ADMIN |
| PUT | `/organizations/{id}` | SUPER_ADMIN, ORG_ADMIN |
| DELETE | `/organizations/{id}` | SUPER_ADMIN |

## Stores

| Method | Endpoint | Role |
|--------|----------|------|
| GET | `/stores` | SUPER_ADMIN, ORG_ADMIN, STORE_MANAGER |
| GET | `/stores/{id}` | SUPER_ADMIN, ORG_ADMIN, STORE_MANAGER |
| POST | `/stores` | SUPER_ADMIN, ORG_ADMIN |
| PUT | `/stores/{id}` | SUPER_ADMIN, ORG_ADMIN |
| DELETE | `/stores/{id}` | SUPER_ADMIN, ORG_ADMIN |

## Inventory

### Products

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/products` | PRODUCT_READ |
| GET | `/inventory/products/{id}` | PRODUCT_READ |
| POST | `/inventory/products` | PRODUCT_WRITE |
| PUT | `/inventory/products/{id}` | PRODUCT_WRITE |
| DELETE | `/inventory/products/{id}` | PRODUCT_WRITE |

### Inventory Items

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/items` | INVENTORY_ITEM_READ |
| GET | `/inventory/items/{id}` | INVENTORY_ITEM_READ |
| GET | `/inventory/items/by-template/{templateId}` | INVENTORY_ITEM_READ |
| GET | `/inventory/items/by-variant/{variantId}` | INVENTORY_ITEM_READ |
| POST | `/inventory/items` | INVENTORY_ITEM_WRITE |
| PUT | `/inventory/items/{id}` | INVENTORY_ITEM_WRITE |
| DELETE | `/inventory/items/{id}` | INVENTORY_ITEM_WRITE |

### Categories

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/categories` | CATEGORY_READ |
| POST | `/inventory/categories` | CATEGORY_WRITE |
| PUT | `/inventory/categories/{id}` | CATEGORY_WRITE |
| DELETE | `/inventory/categories/{id}` | CATEGORY_WRITE |

### Brands

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/brands` | BRAND_READ |
| POST | `/inventory/brands` | BRAND_WRITE |
| PUT | `/inventory/brands/{id}` | BRAND_WRITE |
| DELETE | `/inventory/brands/{id}` | BRAND_WRITE |

### Suppliers

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/suppliers` | SUPPLIER_READ |
| POST | `/inventory/suppliers` | SUPPLIER_WRITE |
| PUT | `/inventory/suppliers/{id}` | SUPPLIER_WRITE |
| DELETE | `/inventory/suppliers/{id}` | SUPPLIER_WRITE |

### Locations

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/locations` | LOCATION_READ |
| POST | `/inventory/locations` | LOCATION_WRITE |
| PUT | `/inventory/locations/{id}` | LOCATION_WRITE |
| DELETE | `/inventory/locations/{id}` | LOCATION_WRITE |

### Units of Measure

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/uom` | UOM_READ |
| POST | `/inventory/uom` | UOM_WRITE |
| PUT | `/inventory/uom/{id}` | UOM_WRITE |
| DELETE | `/inventory/uom/{id}` | UOM_WRITE |

## Purchase Orders

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/purchase-orders` | PO_READ |
| GET | `/inventory/purchase-orders/{id}` | PO_READ |
| POST | `/inventory/purchase-orders` | PO_WRITE |
| PUT | `/inventory/purchase-orders/{id}` | PO_WRITE |
| DELETE | `/inventory/purchase-orders/{id}` | PO_WRITE |
| POST | `/inventory/purchase-orders/{id}/receive` | PO_WRITE |

## Sales

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/sales` | SALE_READ |
| GET | `/inventory/sales/{id}` | SALE_READ |
| POST | `/inventory/sales` | SALE_WRITE |
| PUT | `/inventory/sales/{id}` | SALE_WRITE |
| DELETE | `/inventory/sales/{id}` | SALE_WRITE |

## Discounts

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/discounts` | DISCOUNT_READ |
| POST | `/inventory/discounts` | DISCOUNT_WRITE |
| PUT | `/inventory/discounts/{id}` | DISCOUNT_WRITE |
| DELETE | `/inventory/discounts/{id}` | DISCOUNT_WRITE |

## Damage & Loss

| Method | Endpoint | Permission |
|--------|----------|------------|
| GET | `/inventory/damage-loss` | DAMAGE_LOSS_READ |
| POST | `/inventory/damage-loss` | DAMAGE_LOSS_WRITE |
| PUT | `/inventory/damage-loss/{id}` | DAMAGE_LOSS_WRITE |
| DELETE | `/inventory/damage-loss/{id}` | DAMAGE_LOSS_WRITE |

## Global Templates (Super Admin Only)

| Method | Endpoint | Role |
|--------|----------|------|
| GET | `/global-templates` | SUPER_ADMIN |
| POST | `/global-templates` | SUPER_ADMIN |
| PUT | `/global-templates/{id}` | SUPER_ADMIN |
| DELETE | `/global-templates/{id}` | SUPER_ADMIN |

## Request Headers

| Header | Purpose | Example |
|--------|---------|---------|
| `Authorization` | Bearer token (if not using cookies) | `Bearer <token>` |
| `X-Organization-Id` | Active organization | `uuid` |
| `X-Store-Id` | Active store (for store-scoped requests) | `uuid` |
| `Content-Type` | Request body type | `application/json` |

## Error Response Format

```json
{
  "timestamp": "2026-01-23T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users"
}
```

## Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (not authenticated) |
| 403 | Forbidden (no permission) |
| 404 | Not Found |
| 409 | Conflict (duplicate resource) |
| 500 | Internal Server Error |
