# Business Setup and System Architecture

## Overview

Triveni Management System is a **multi-tenant SaaS (Software as a Service)** application designed for retail and inventory management. The system allows organizations to manage their stores, products, inventory, sales, and operations through a centralized platform.

## Core Concepts

### 1. Multi-Tenancy Architecture

The system uses an **organization-based multi-tenancy** model where:

- Each **Organization** acts as an independent tenant
- Organizations are completely isolated from each other
- All data (products, stores, users, inventory) is scoped to an organization
- Organizations can have multiple stores, each operating independently

### 2. User Registration and Organization Creation

**Initial Setup:**
- Any user can register and create an organization
- When a user creates an organization, they automatically become the **Super Admin** of that organization
- The Super Admin has full administrative privileges within their organization
- Organizations serve as tenants, providing complete data isolation

### 3. Organization Structure

```
Organization (Tenant)
├── Stores (Multiple locations)
│   ├── Inventory Items
│   ├── Staff (Store Managers, Cashiers, etc.)
│   └── Operations
├── Users (Invited by Super Admin)
│   ├── Roles (Organization-level or Store-level)
│   └── Permissions
├── Product Templates (Master Data)
├── Categories, Brands, Suppliers
└── Settings & Configuration
```

### 4. User Management and Invitations

**Super Admin Capabilities:**
- Invite users to join their organization
- Assign roles and permissions to invited users
- Manage user access at organization or store level
- Remove users from the organization

**Invited Users:**
- Receive an invitation to join an organization
- Can be assigned specific roles (e.g., Store Manager, Inventory Manager, Cashier)
- Have permissions based on their assigned roles
- Can access organization data based on their permissions
- May have access to:
  - Create and manage product templates
  - Manage categories, brands, suppliers
  - Access inventory management
  - Process sales transactions
  - Generate reports (depending on role)

### 5. Roles and Permissions

**Role-Based Access Control (RBAC):**
- **Roles** define a user's position (e.g., Super Admin, Store Manager, Cashier)
- **Permissions** define what actions a user can perform
- Roles can have multiple permissions
- Permissions are granular (e.g., `PRODUCT_READ`, `PRODUCT_WRITE`, `SALE_WRITE`)

**Common Roles:**
- **SUPER_ADMIN**: Full access to the organization
- **ORG_ADMIN**: Organization-level administrative access
- **STORE_MANAGER**: Manage a specific store
- **CASHIER**: Process sales and transactions
- **INVENTORY_MANAGER**: Manage inventory and products

**Permission Examples:**
- `PRODUCT_READ`, `PRODUCT_WRITE`: Product template management
- `INVENTORY_ITEM_READ`, `INVENTORY_ITEM_WRITE`: Inventory management
- `SALE_READ`, `SALE_WRITE`: Sales transaction processing
- `USER_READ`, `USER_WRITE`: User management
- `ORG_READ`, `ORG_WRITE`: Organization management

### 6. Product Template (Master Data)

**What is a Product Template?**
A **Product Template** is the master definition of a product before it becomes actual inventory. Think of it as a "recipe" or "blueprint" for a product.

**Key Characteristics:**
- **Unique SKU (Stock Keeping Unit)**: Internal identifier for the product
- **Unique Barcode**: External identifier (EAN-13, UPC-A format)
- **Name and Description**: Product information
- **Category**: Product classification (e.g., Electronics, Clothing, Food)
- **Unit of Measure**: How the product is measured (e.g., Piece, Kilogram, Liter)
- **Brand**: Manufacturer or brand name (optional)
- **Image URL**: Product image reference
- **Reorder Point**: Minimum stock level to trigger reordering
- **Requires Expiration Date**: Whether the product has expiration tracking
- **Organization-scoped**: Each organization has its own product templates

**Example:**
- Product Template: "Samsung Galaxy S24" (SKU: GAL-S24-256)
- This template defines the product, but doesn't represent actual inventory
- Actual inventory items are created from this template at specific stores

**Product Template vs Inventory Item:**
- **Product Template**: "What product is this?" (Master data, organization-level)
- **Inventory Item**: "Where is it? How many? What batch/lot?" (Actual stock, store-level)

### 7. Categories

**What are Categories?**
Categories are used to classify and organize product templates for easier management and navigation.

**Key Characteristics:**
- **Unique Code**: Short identifier (e.g., "ELEC", "CLO", "FOOD")
- **Unique Name**: Display name (e.g., "Electronics", "Clothing", "Food & Beverages")
- **Description**: Category details
- **Organization-scoped**: Each organization defines its own categories

**Purpose:**
- Group related products together
- Enable filtering and searching
- Organize product catalog
- Generate category-based reports

### 8. Brands

**What are Brands?**
Brands represent manufacturers or product brands.

**Key Characteristics:**
- **Unique Name**: Brand name (e.g., "Samsung", "Nike", "Coca-Cola")
- **Organization-scoped**: Each organization defines its own brands

**Purpose:**
- Group products by manufacturer
- Track brand performance
- Filter products by brand

### 9. Suppliers

**What are Suppliers?**
Suppliers are vendors or companies from whom you purchase products.

**Key Characteristics:**
- **Company Information**: Name, contact details
- **Organization-scoped**: Each organization manages its own suppliers

**Purpose:**
- Create purchase orders
- Track supplier performance
- Manage vendor relationships

### 10. Stores

**What are Stores?**
Stores are physical or logical locations where inventory is held and operations are performed.

**Key Characteristics:**
- **Belongs to an Organization**: Each store is part of one organization
- **Can have Staff**: Store-specific employees (Store Manager, Cashiers)
- **Has Inventory**: Physical stock of products at that location
- **Can process Sales**: Point of sale transactions

**Store Management:**
- Super Admin and Org Admin can create and manage stores
- Store Managers have access to their specific store
- Inventory is tracked per store
- Sales transactions are store-specific

### 11. Inventory Items

**What are Inventory Items?**
Inventory Items represent actual physical stock of products at a specific store location.

**Key Characteristics:**
- **Based on Product Template**: Links to a product template
- **Store-specific**: Located at a particular store
- **Quantity**: Actual stock count
- **Batch/Lot Information**: Tracking for expiration, lot numbers
- **Location within Store**: Specific storage area (optional)

**Inventory Management:**
- Products are received from suppliers
- Stock levels are tracked and updated with sales
- Reordering is triggered based on reorder points
- Damage/loss can be recorded

### 12. Unit of Measure (UoM)

**What is Unit of Measure?**
Unit of Measure defines how products are quantified (e.g., Piece, Kilogram, Liter, Box).

**Key Characteristics:**
- **Name**: Full name (e.g., "Kilogram", "Piece")
- **Code**: Short code (e.g., "kg", "pc", "L")
- **Organization-scoped**: Each organization defines its own units

**Examples:**
- Weight: Kilogram (kg), Gram (g), Pound (lb)
- Volume: Liter (L), Milliliter (mL), Gallon
- Count: Piece (pc), Box, Carton

### 13. Purchase Orders

**What are Purchase Orders?**
Purchase Orders are requests to suppliers to deliver products to stores.

**Key Characteristics:**
- **Supplier**: The vendor
- **Store**: Destination store
- **Items**: Products and quantities ordered
- **Status**: Pending, Received, Cancelled

### 14. Sales Transactions

**What are Sales Transactions?**
Sales Transactions record customer purchases at stores.

**Key Characteristics:**
- **Store-specific**: Transaction occurs at a specific store
- **Items**: Products sold with quantities and prices
- **Discounts**: Applied discounts (if any)
- **Payment Method**: Cash, Card, etc.

### 15. Discounts

**What are Discounts?**
Discounts are promotional offers applied to products or sales.

**Key Characteristics:**
- **Type**: Percentage or fixed amount
- **Scope**: Product-specific, category-wide, or store-wide
- **Valid Period**: Start and end dates

### 16. Damage/Loss

**What is Damage/Loss?**
Damage/Loss records products that are damaged, expired, or lost.

**Key Characteristics:**
- **Reason**: Damage, expiration, theft, etc.
- **Quantity**: Amount lost
- **Inventory Impact**: Reduces inventory count

### 17. Stock Checks

**What are Stock Checks?**
Stock Checks are periodic inventory audits to verify actual stock levels.

**Key Characteristics:**
- **Physical Count**: Actual count of products
- **System Count**: Expected count from system
- **Variance**: Difference between physical and system counts

## Data Flow

### Product Lifecycle

1. **Create Product Template** (Super Admin / Invited User with permissions)
   - Define product: SKU, name, description, category, UoM, barcode
   - This is master data, no inventory yet

2. **Receive Inventory** (Store Manager / Inventory Manager)
   - Create inventory items from product template
   - Assign to a store
   - Set initial quantity

3. **Sell Product** (Cashier / Store Manager)
   - Process sale transaction
   - Inventory is automatically reduced
   - Sale is recorded

4. **Reorder** (Automated / Manual)
   - When stock falls below reorder point
   - Create purchase order to supplier
   - Receive products and update inventory

### User Journey

1. **Registration**
   - User registers and creates organization
   - Becomes Super Admin

2. **Organization Setup**
   - Create stores
   - Define categories, brands, suppliers
   - Set up units of measure
   - Create product templates

3. **Invite Users**
   - Super Admin invites team members
   - Assigns roles and permissions
   - Users receive invitation and join

4. **Operations**
   - Users access based on permissions
   - Manage inventory, process sales
   - Generate reports

## Technical Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.3
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT authentication
- **API**: RESTful APIs with OpenAPI/Swagger documentation

### Frontend (Next.js)
- **Framework**: Next.js 15 with App Router
- **UI Library**: Material-UI (MUI)
- **State Management**: React Query (TanStack Query)
- **Forms**: React Hook Form with Yup validation

### Key Patterns
- **Multi-tenancy**: Organization-scoped data isolation
- **RBAC**: Role-based access control with permissions
- **Repository Pattern**: Data access layer
- **DTO Pattern**: Data transfer objects for API
- **Service Layer**: Business logic separation

## For New Developers

### Getting Started

1. **Understand Multi-Tenancy**
   - Everything is scoped to an organization
   - Never access data from another organization
   - Always filter by `organizationId`

2. **Understand Permissions**
   - Check permissions before allowing actions
   - Use `@PreAuthorize` in controllers
   - Frontend should also check permissions

3. **Product Template vs Inventory**
   - Product Template = "What is this product?"
   - Inventory Item = "Where is it? How many?"

4. **Organization Context**
   - Users belong to organizations
   - Stores belong to organizations
   - Products belong to organizations
   - Always respect organization boundaries

### Common Tasks

**Creating a Product:**
1. Ensure user has `PRODUCT_WRITE` permission
2. Create product template with required fields
3. Template is organization-scoped automatically

**Managing Inventory:**
1. Ensure user has `INVENTORY_ITEM_WRITE` permission
2. Create inventory items from product templates
3. Assign to a store
4. Track quantity and location

**Processing Sales:**
1. Ensure user has `SALE_WRITE` permission
2. Select store (if user has access)
3. Add products from inventory
4. Apply discounts if applicable
5. Process payment
6. Inventory is automatically updated

## Security Considerations

- **Organization Isolation**: Users can only access their organization's data
- **Permission Checks**: Both backend and frontend enforce permissions
- **JWT Tokens**: Include organization context for multi-tenancy
- **Role Hierarchy**: Super Admin > Org Admin > Store Manager > Cashier

## Future Enhancements

- Multi-organization support for users
- Store-to-store transfers
- Advanced reporting and analytics
- Mobile app support
- Barcode scanning integration
- Integration with accounting systems

