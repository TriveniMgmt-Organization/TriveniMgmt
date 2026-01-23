# TriveniMgmt - Feature Roadmap & TODOs

> **Purpose**: Track planned features and improvements for future implementation.
> **Last Updated**: 2026-01-23

---

## High Priority Features

### Alerts & Notifications
- [ ] **Low Stock Alerts** - Email/push notifications when stock hits reorder point
- [ ] **Expiry Warnings** - Alerts for items approaching expiry date (configurable: 7/14/30 days)
- [ ] **PO Status Notifications** - Notify when PO is shipped, delivered, or has issues
- [ ] **Notifications Table** - Store notifications with read/unread status per user

### Inventory Operations
- [ ] **Stock Transfers** - Move inventory between stores or locations within a store
- [ ] **Inventory Adjustments** - Manual stock corrections with reason codes and audit trail
- [ ] **Cycle Counting** - Scheduled partial inventory counts (vs full stock takes)
- [ ] **Stock Movement History** - Complete audit trail of all quantity changes

### Purchase Orders
- [ ] **Goods Received Notes (GRN)** - Partial receiving of POs with quality checks
- [ ] **PO Approval Workflow** - Large POs require ORG_ADMIN approval
- [ ] **Supplier Performance Tracking** - On-time delivery rates, quality scores

### Returns & Adjustments
- [ ] **Return Management** - Track customer returns back to inventory
- [ ] **Damage/Loss Recording** - UI for reporting damaged or lost items (table exists)

---

## Medium Priority Features

### Reporting & Analytics
- [ ] **Cross-Store Reporting** - Total inventory value, stock movement between stores
- [ ] **Demand Forecasting** - Predict reorder quantities based on sales history
- [ ] **Inventory Valuation** - FIFO/LIFO/FEFO costing methods
- [ ] **Sales Analytics** - Top products, slow movers, profit margins
- [ ] **Export to PDF/Excel** - All reports exportable

### Product Management
- [ ] **Barcode/SKU Auto-Generation** - Generate if not provided
- [ ] **Product Images** - Multiple images per product/variant
- [ ] **Min/Max Stock Levels** - Different from reorder point, prevents over-ordering

### Location Management
- [ ] **Warehouse Zones/Bins** - More granular location tracking (A1-B3-C2)
- [ ] **Location Types** - Warehouse, showroom, transit, quarantine

---

## Lower Priority / Future Enhancements

### Financial
- [ ] **Multi-Currency Support** - Essential for international organizations
- [ ] **Tax Configuration** - Per-store or per-product tax rules
- [ ] **Invoice Generation** - Auto-generate invoices from sales

### Billing & Subscription
- [ ] **Stripe/Paddle Integration** - Subscription management
- [ ] **Usage-Based Pricing** - Per store, per user, or per transaction tiers
- [ ] **Feature Gating** - Free tier with limits, paid tiers unlock features
- [ ] **Trial Periods** - 14-30 day free trials

### Integrations
- [ ] **Accounting Software** - QuickBooks, Xero integration
- [ ] **E-commerce** - Shopify, WooCommerce sync
- [ ] **Shipping Carriers** - Track shipments from carriers

### UX Improvements
- [ ] **Guided Onboarding Wizard** - Walk new orgs through setup
- [ ] **CSV/Excel Import** - Bulk import products, inventory, suppliers
- [ ] **Bulk Actions** - Select multiple items for batch operations
- [ ] **Global Search** - Search products, POs, suppliers, users from anywhere
- [ ] **Mobile-Responsive Store View** - Store managers use tablets/phones

---

## Architecture Improvements

### Database
- [ ] **Stock Movements Table** - Separate from stock_transactions for transfers
  ```
  stock_movements: id, from_location_id, to_location_id, variant_id,
  quantity, movement_type, reference_id, notes, created_by
  ```
- [ ] **Notifications Table**
  ```
  notifications: id, organization_id, store_id, user_id, type,
  title, message, is_read, created_at
  ```

### Performance
- [ ] **Redis Caching** - Cache frequently accessed data (categories, brands)
- [ ] **Background Jobs** - Async processing for reports, bulk imports
- [ ] **Database Read Replicas** - For reporting queries

### Security
- [ ] **Two-Factor Authentication** - Optional 2FA for org admins
- [ ] **API Rate Limiting** - Per-user/org limits (config exists, needs implementation)
- [ ] **Audit Log Retention Policy** - Auto-archive old audit logs

---

## Current Focus (2026-01-23)

Working on:
1. **Global Templates** - Improve the global template system
2. **Inventory Adding Mechanism** - Streamline adding inventory to stores

---

## Completed Features

- [x] Multi-tenant architecture (Organization → Store)
- [x] Role-based access control (SUPER_ADMIN, ORG_ADMIN, STORE_MANAGER)
- [x] Product Templates with variants
- [x] Categories, Brands, Suppliers, UoM
- [x] Inventory locations per store
- [x] Batch/Lot tracking with expiry dates
- [x] Purchase Orders with line items
- [x] Basic POS/Sales
- [x] Discount rules
- [x] Soft deletes with audit trail
- [x] JWT authentication with HttpOnly cookies

---

## Notes

- Prioritize features that differentiate from competitors (batch tracking, multi-location)
- Consider customer feedback when re-prioritizing
- Each feature should have proper permission checks
- All new tables should follow BaseEntity pattern (audit fields, soft delete)
