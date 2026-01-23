# Database Schema

> **Database**: PostgreSQL 16
> **ORM**: Hibernate/JPA with Spring Data

## Entity Relationship Diagram (Conceptual)

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Permission    │────<│      Role       │>────│      User       │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                              │                        │
                              │                        │
                              └────────────────────────┘
                                        │
                              ┌─────────▼─────────┐
                              │ UserOrganization  │
                              │      Role         │
                              └─────────┬─────────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
            ┌───────▼───────┐   ┌───────▼───────┐   ┌───────▼───────┐
            │  Organization │   │     Store     │   │     Role      │
            └───────┬───────┘   └───────┬───────┘   └───────────────┘
                    │                   │
    ┌───────────────┼───────────────────┤
    │               │                   │
┌───▼───┐   ┌───────▼───────┐   ┌───────▼───────┐
│ Store │   │ProductTemplate│   │     Sale      │
└───────┘   └───────┬───────┘   └───────────────┘
                    │
            ┌───────▼───────┐
            │ProductVariant │
            └───────┬───────┘
                    │
            ┌───────▼───────┐
            │InventoryItem  │
            └───────┬───────┘
                    │
            ┌───────▼───────┐
            │  StockLevel   │
            └───────────────┘
```

## Core Tables

### users
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| username | VARCHAR | UNIQUE, NOT NULL |
| email | VARCHAR | UNIQUE, NOT NULL |
| password_hash | VARCHAR | NOT NULL |
| first_name | VARCHAR | |
| last_name | VARCHAR | |
| image_url | VARCHAR | |
| is_active | BOOLEAN | DEFAULT true |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |
| created_by | VARCHAR | |
| updated_by | VARCHAR | |
| deleted_at | TIMESTAMP | Soft delete |
| version | INTEGER | Optimistic lock |

### roles
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| name | VARCHAR | UNIQUE, NOT NULL |
| description | VARCHAR | |
| (audit fields) | | |

### permissions
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| name | VARCHAR | UNIQUE, NOT NULL |
| description | VARCHAR | |
| (audit fields) | | |

### role_permissions (Join Table)
| Column | Type | Constraints |
|--------|------|-------------|
| role_id | UUID | FK → roles |
| permission_id | UUID | FK → permissions |

### organizations
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| name | VARCHAR | NOT NULL |
| description | VARCHAR | |
| contact_info | VARCHAR | |
| applied_template_code | VARCHAR | |
| (audit fields) | | |

### stores
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| name | VARCHAR | NOT NULL |
| location | VARCHAR | |
| country_code | VARCHAR | |
| contact_info | VARCHAR | |
| status | VARCHAR | ACTIVE, INACTIVE, CLOSED |
| (audit fields) | | |

### user_organization_roles
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| user_id | UUID | FK → users |
| organization_id | UUID | FK → organizations |
| role_id | UUID | FK → roles |
| store_id | UUID | FK → stores (nullable) |
| (audit fields) | | |

**Note**: If `store_id` is null, the role applies at organization level.

## Inventory Tables

### categories
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| name | VARCHAR | NOT NULL |
| description | VARCHAR | |
| parent_id | UUID | FK → categories (self-ref) |
| (audit fields) | | |

### brands
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| name | VARCHAR | NOT NULL |
| description | VARCHAR | |
| logo_url | VARCHAR | |
| (audit fields) | | |

### suppliers
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| name | VARCHAR | NOT NULL |
| contact_name | VARCHAR | |
| email | VARCHAR | |
| phone | VARCHAR | |
| address | VARCHAR | |
| (audit fields) | | |

### unit_of_measures
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| name | VARCHAR | NOT NULL |
| abbreviation | VARCHAR | |
| (audit fields) | | |

### product_templates
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| category_id | UUID | FK → categories |
| brand_id | UUID | FK → brands |
| base_uom_id | UUID | FK → unit_of_measures |
| name | VARCHAR | NOT NULL |
| description | TEXT | |
| image_url | VARCHAR | |
| reorder_point | INTEGER | |
| requires_expiry | BOOLEAN | |
| is_active | BOOLEAN | DEFAULT true |
| (audit fields) | | |

### product_template_attributes (ElementCollection)
| Column | Type | Constraints |
|--------|------|-------------|
| product_template_id | UUID | FK → product_templates |
| attribute_key | VARCHAR | |
| attribute_value | VARCHAR | |

### product_variants
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| template_id | UUID | FK → product_templates |
| organization_id | UUID | FK → organizations |
| sku | VARCHAR | UNIQUE per org |
| barcode | VARCHAR | UNIQUE per org |
| cost_price | DECIMAL | |
| retail_price | DECIMAL | |
| is_active | BOOLEAN | DEFAULT true |
| (audit fields) | | |

### product_variant_attributes (ElementCollection)
| Column | Type | Constraints |
|--------|------|-------------|
| product_variant_id | UUID | FK → product_variants |
| attribute_key | VARCHAR | |
| attribute_value | VARCHAR | |

### inventory_locations
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| store_id | UUID | FK → stores |
| name | VARCHAR | NOT NULL |
| description | VARCHAR | |
| location_type | VARCHAR | |
| (audit fields) | | |

### batch_lots
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| lot_number | VARCHAR | UNIQUE per org |
| manufacturing_date | DATE | |
| expiry_date | DATE | |
| (audit fields) | | |

### inventory_items
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| variant_id | UUID | FK → product_variants |
| location_id | UUID | FK → inventory_locations |
| batch_lot_id | UUID | FK → batch_lots (nullable) |
| expiry_date | DATE | |
| (audit fields) | | |

**Unique Constraint**: (variant_id, location_id, batch_lot_id)

### stock_levels
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| inventory_item_id | UUID | FK → inventory_items (OneToOne) |
| quantity | INTEGER | NOT NULL |
| (audit fields) | | |

### stock_transactions
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| inventory_item_id | UUID | FK → inventory_items |
| transaction_type | VARCHAR | |
| quantity | INTEGER | |
| reference_type | VARCHAR | |
| reference_id | UUID | |
| (audit fields) | | |

## Sales Tables

### sales
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| store_id | UUID | FK → stores |
| processed_by | UUID | FK → users |
| sale_timestamp | TIMESTAMP | NOT NULL |
| total_amount | DECIMAL | |
| total_discount_amount | DECIMAL | |
| payment_method | VARCHAR | |
| transaction_id | VARCHAR | |
| notes | TEXT | |
| (audit fields) | | |

### sale_items
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| sale_id | UUID | FK → sales |
| variant_id | UUID | FK → product_variants |
| quantity | INTEGER | NOT NULL |
| unit_price | DECIMAL | |
| discount_amount | DECIMAL | |
| total_price | DECIMAL | |
| (audit fields) | | |

## Purchase Order Tables

### purchase_orders
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| supplier_id | UUID | FK → suppliers |
| created_by | UUID | FK → users |
| order_date | DATE | NOT NULL |
| expected_delivery_date | DATE | |
| actual_delivery_date | DATE | |
| status | VARCHAR | PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED |
| total_estimated_amount | DECIMAL | |
| tracking_number | VARCHAR | |
| notes | TEXT | |
| (audit fields) | | |

### purchase_order_items
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| purchase_order_id | UUID | FK → purchase_orders |
| variant_id | UUID | FK → product_variants |
| quantity | INTEGER | NOT NULL |
| unit_cost | DECIMAL | |
| total_cost | DECIMAL | |
| received_quantity | INTEGER | |
| (audit fields) | | |

## Other Tables

### discounts
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| organization_id | UUID | FK → organizations |
| name | VARCHAR | NOT NULL |
| discount_type | VARCHAR | |
| value | DECIMAL | |
| start_date | DATE | |
| end_date | DATE | |
| is_active | BOOLEAN | |
| (audit fields) | | |

### damage_losses
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| inventory_item_id | UUID | FK → inventory_items |
| quantity | INTEGER | NOT NULL |
| reason | VARCHAR | |
| reported_by | UUID | FK → users |
| reported_at | TIMESTAMP | |
| (audit fields) | | |

### refresh_tokens
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| user_id | UUID | FK → users |
| token | VARCHAR | UNIQUE |
| expiry_date | TIMESTAMP | |
| (audit fields) | | |

## Indexes

Key indexes for performance:
- `users(email)` - Login lookup
- `users(username)` - Login lookup
- `product_variants(organization_id, sku)` - SKU lookup
- `product_variants(organization_id, barcode)` - Barcode lookup
- `inventory_items(variant_id, location_id, batch_lot_id)` - Stock lookup
- `sales(store_id, sale_timestamp)` - Sales reporting
- `purchase_orders(organization_id, status)` - PO listing

## Database Configuration

### Development
```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.liquibase.enabled=false
```

### Production
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

## Soft Delete Pattern

All entities support soft delete:
- `deleted_at` - Timestamp when deleted
- `deleted_by` - User who deleted

Queries should filter by `deleted_at IS NULL` for active records.
