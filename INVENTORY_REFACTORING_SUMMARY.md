# Inventory Management Refactoring Summary

## Overview
This document summarizes the changes made to align DTOs, entities, and structure with the refactored inventory management system for a robust, production-grade SaaS application.

## Critical Changes Applied

### 1. Entity Updates - equals/hashCode
**Status: ✅ Completed**

All inventory entities now use `@EqualsAndHashCode(callSuper = false, of = {"id"})` to prevent proxy issues and ensure stable hash codes:
- `ProductTemplate`
- `ProductVariant`
- `InventoryItem`
- `StockTransaction`
- `StockLevel`
- `BatchLot`
- `UoMConversion`
- `Category`

### 2. ProductTemplate - SKU/Barcode Removed
**Status: ✅ Completed**

- **ProductTemplate DTOs Updated**: Removed `sku` and `barcode` fields
- **ProductTemplate Entity**: No longer contains SKU/barcode (moved to ProductVariant)
- **New Structure**: SKU and barcode are now unique per organization at the variant level

### 3. ProductVariant - SKU/Barcode Added
**Status: ✅ Completed**

- **New DTOs Created**:
  - `ProductVariantDTO` - Full variant details with SKU/barcode
  - `CreateProductVariantDTO` - For creating variants
- **Entity**: `ProductVariant` has unique constraints on `(organization_id, sku)` and `(organization_id, barcode)`

### 4. StockTransaction - Immutable Records
**Status: ✅ Completed**

- **New DTOs Created**:
  - `StockTransactionDTO` - Immutable transaction record
  - `CreateStockTransactionDTO` - For creating transactions
- **TransactionType Enum**: Created `com.store.mgmt.inventory.model.enums.TransactionType`
- **Entity**: All quantity changes must go through `StockTransaction` records

### 5. StockLevel - Calculated Values
**Status: ✅ Completed**

- **New DTO Created**: `StockLevelDTO` - Represents calculated stock levels
- **Entity**: `StockLevel` is maintained via triggers/service layer from `StockTransaction` records
- **Fields**: `onHand`, `committed`, `available` (calculated), `lowStockThreshold`, `maxStockLevel`

### 6. InventoryItem - Updated Structure
**Status: ✅ Completed**

- **DTOs Updated**:
  - `InventoryItemDTO` - Now references `variantId` (not `productTemplateId`)
  - `CreateInventoryItemDTO` - Simplified to variant, location, batchLot, expiry
- **Entity**: References `ProductVariant` instead of `ProductTemplate`
- **No Direct Quantity**: Quantity is maintained through `StockLevel` (derived from transactions)

### 7. BatchLot Support
**Status: ✅ Completed**

- **New DTOs Created**:
  - `BatchLotDTO` - Full batch/lot details
  - `CreateBatchLotDTO` - For creating batches
- **Entity**: Tracks batch numbers, manufacture/expiry dates, supplier

### 8. UoM Conversion Support
**Status: ✅ Completed**

- **New DTOs Created**:
  - `UoMConversionDTO` - Conversion ratios between units
  - `CreateUoMConversionDTO` - For creating conversions
- **Entity**: Enables unit conversions (e.g., 1 Box = 12 Each)

### 9. Category - Organization-Scoped Uniqueness
**Status: ✅ Completed**

- **Entity**: Already has unique constraints on `(organization_id, code)` and `(organization_id, name)`
- **Note**: Service/controller should enforce this at the application level

## DTO Files Created/Updated

### Created:
1. `ProductTemplateDTO.java` - New comprehensive DTO for templates
2. `ProductVariantDTO.java` - Variant with SKU/barcode
3. `CreateProductVariantDTO.java` - Create variant
4. `StockTransactionDTO.java` - Immutable transaction record
5. `CreateStockTransactionDTO.java` - Create transaction
6. `StockLevelDTO.java` - Stock levels
7. `BatchLotDTO.java` - Batch/lot tracking
8. `CreateBatchLotDTO.java` - Create batch
9. `UoMConversionDTO.java` - Unit conversions
10. `CreateUoMConversionDTO.java` - Create conversion

### Updated:
1. `CreateProductDTO.java` - Removed SKU, barcode, price, quantity
2. `ProductDTO.java` - Updated to match ProductTemplate structure (no SKU/barcode at template level)
3. `UpdateProductDTO.java` - Refactored (no longer extends CreateProductDTO)
4. `InventoryItemDTO.java` - References variantId instead of productTemplateId
5. `CreateInventoryItemDTO.java` - Simplified, removed direct quantity

## Next Steps Required

### 1. Service Layer Updates ⚠️ CRITICAL
**Status: ⏳ Pending**

The `InventoryServiceImpl` needs significant refactoring:

#### Current Issues:
- Direct quantity mutations (e.g., `updateInventoryItemQuantity`) need to be replaced with `StockTransaction` creation
- Methods like `createInventoryItem` should not accept quantity directly
- Stock levels should be calculated from transactions (via triggers or service methods)

#### Required Changes:
1. **Remove Direct Quantity Updates**: Replace all methods that directly modify quantity
2. **Add Transaction-Based Methods**:
   ```java
   StockTransactionDTO createStockTransaction(CreateStockTransactionDTO dto);
   StockLevelDTO getStockLevel(UUID inventoryItemId);
   void updateStockLevelFromTransaction(StockTransaction transaction);
   ```
3. **Add ProductVariant Methods**:
   ```java
   ProductVariantDTO createProductVariant(CreateProductVariantDTO dto);
   List<ProductVariantDTO> getVariantsByTemplate(UUID templateId);
   ProductVariantDTO updateVariant(UUID variantId, UpdateProductVariantDTO dto);
   ```
4. **Add BatchLot Methods**:
   ```java
   BatchLotDTO createBatchLot(CreateBatchLotDTO dto);
   List<BatchLotDTO> getAllBatchLots();
   ```
5. **Add UoM Conversion Methods**:
   ```java
   UoMConversionDTO createUoMConversion(CreateUoMConversionDTO dto);
   List<UoMConversionDTO> getConversionsByUom(UUID uomId);
   ```

### 2. Controller Updates
**Status: ⏳ Pending**

Update `InventoryController` to:
1. Remove endpoints that directly modify quantity
2. Add endpoints for:
   - ProductVariant CRUD
   - StockTransaction creation
   - StockLevel queries
   - BatchLot management
   - UoM conversion management
3. Update existing endpoints to use new DTOs

### 3. Database Triggers (Recommended)
**Status: ⏳ Pending**

Consider adding database triggers to automatically update `StockLevel` when `StockTransaction` is inserted:
```sql
CREATE OR REPLACE FUNCTION update_stock_level()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE stock_levels
    SET on_hand = on_hand + NEW.quantity_delta,
        available = on_hand - committed
    WHERE inventory_item_id = NEW.inventory_item_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER stock_transaction_trigger
AFTER INSERT ON stock_transactions
FOR EACH ROW
EXECUTE FUNCTION update_stock_level();
```

### 4. Repository Updates
**Status: ⏳ Pending**

Add repositories for:
- `ProductVariantRepository`
- `StockTransactionRepository`
- `StockLevelRepository`
- `BatchLotRepository`
- `UoMConversionRepository`

### 5. Mapper Updates
**Status: ⏳ Pending**

Create/update MapStruct mappers:
- `ProductVariantMapper`
- `StockTransactionMapper`
- `StockLevelMapper`
- `BatchLotMapper`
- `UoMConversionMapper`

### 6. Frontend Updates
**Status: ⏳ Pending**

The frontend needs to be updated to:
1. Work with ProductTemplates (no SKU/barcode)
2. Create/manage ProductVariants (with SKU/barcode)
3. Display stock levels from transactions
4. Support batch/lot tracking
5. Support UoM conversions

## Architecture Principles Applied

1. **Immutability**: Stock transactions are immutable - all changes create new transaction records
2. **Audit Trail**: Complete history of stock movements via `StockTransaction`
3. **Calculated State**: Stock levels are derived from transactions, not stored directly
4. **Separation of Concerns**: 
   - Templates define product structure
   - Variants contain SKU/barcode and pricing
   - InventoryItems represent physical stock at locations
   - Transactions record movements
5. **Organization Scoping**: SKU/barcode uniqueness is per organization
6. **Batch Tracking**: Full batch/lot support for expiry and traceability
7. **Unit Conversions**: Flexible UoM conversion system

## Breaking Changes

⚠️ **Warning**: These changes are breaking and will require:
- Database migrations
- Service layer refactoring
- Controller endpoint updates
- Frontend API client regeneration
- Frontend component updates

## Testing Checklist

- [ ] ProductTemplate CRUD (no SKU/barcode)
- [ ] ProductVariant CRUD (with SKU/barcode)
- [ ] InventoryItem creation (references variant)
- [ ] StockTransaction creation
- [ ] StockLevel calculation from transactions
- [ ] BatchLot management
- [ ] UoM conversion
- [ ] Category uniqueness per organization
- [ ] Stock adjustments via transactions (not direct)
- [ ] Sales processing via transactions
- [ ] Purchase receipts via transactions

## Notes

- All entities now use stable equals/hashCode based on ID only
- TransactionType enum is in proper package
- Category uniqueness is enforced at database level
- Stock levels should be updated via triggers or service methods after transaction creation

