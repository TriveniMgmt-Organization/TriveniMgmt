# Inventory Service Implementation Guide

## Overview
This document provides guidance for completing the `InventoryServiceImpl` refactoring. The service needs significant updates to support the new entity structure.

## Required Updates

### 1. Add New Dependencies to Constructor

Add these repositories:
- `ProductVariantRepository productVariantRepository`
- `StockTransactionRepository stockTransactionRepository`
- `StockLevelRepository stockLevelRepository`
- `BatchLotRepository batchLotRepository`
- `UoMConversionRepository uomConversionRepository`

Add these mappers:
- `ProductVariantMapper productVariantMapper`
- `StockTransactionMapper stockTransactionMapper`
- `StockLevelMapper stockLevelMapper`
- `BatchLotMapper batchLotMapper`
- `UoMConversionMapper uomConversionMapper`

### 2. Fix createProduct Method

**Current Issue**: Checks for SKU/barcode which are no longer on ProductTemplate

**Fix**:
```java
@Override
@Transactional
public ProductDTO createProduct(CreateProductDTO createDTO) {
    log.info("Creating new product template: {} for organization ID: {}", createDTO.getName(), TenantContext.getCurrentOrganizationId());
    
    Category category = findCategoryOrThrow(createDTO.getCategoryId());
    UnitOfMeasure uom = findUnitOfMeasureOrThrow(createDTO.getUnitOfMeasureId());

    ProductTemplate newProduct = productTemplateMapper.toEntity(createDTO);
    newProduct.setOrganization(TenantContext.getCurrentOrganization());
    newProduct.setCategory(category);
    newProduct.setUnitOfMeasure(uom);
    
    if (createDTO.getBrandId() != null) {
        newProduct.setBrand(findBrandOrThrow(createDTO.getBrandId()));
    }

    ProductTemplate savedProduct = productTemplateRepository.save(newProduct);
    logAuditEntry("CREATE_PRODUCT", savedProduct.getId(), "Created product template: " + savedProduct.getName());
    return productTemplateMapper.toDto(savedProduct);
}
```

### 3. Fix updateProduct Method

**Current Issue**: Checks for SKU/barcode on ProductTemplate

**Fix**: Remove SKU/barcode checks - they are on variants now.

### 4. Replace updateInventoryItemQuantity with createStockTransaction

**REMOVE**: `updateInventoryItemQuantity` method

**REPLACE WITH**: Stock transaction-based approach:

```java
@Override
@Transactional
public StockTransactionDTO createStockTransaction(CreateStockTransactionDTO createDTO) {
    InventoryItem inventoryItem = inventoryItemRepository.findById(createDTO.getInventoryItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + createDTO.getInventoryItemId()));
    
    // Validate transaction
    if (createDTO.getQuantityDelta() == 0) {
        throw new InvalidOperationException("Quantity delta cannot be zero");
    }
    
    // Check stock availability for negative deltas (sales, transfers out)
    if (createDTO.getQuantityDelta() < 0) {
        StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(createDTO.getInventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock level not found for inventory item"));
        if (stockLevel.getAvailable() < Math.abs(createDTO.getQuantityDelta())) {
            throw new InsufficientStockException("Insufficient stock available");
        }
    }
    
    // Create transaction
    StockTransaction transaction = stockTransactionMapper.toEntity(createDTO);
    transaction.setInventoryItem(inventoryItem);
    transaction.setUser(TenantContext.getCurrentUser()); // Get from context
    transaction.setTimestamp(LocalDateTime.now());
    
    StockTransaction savedTransaction = stockTransactionRepository.save(transaction);
    
    // Update stock level (or trigger will do it)
    updateStockLevelFromTransaction(savedTransaction);
    
    logAuditEntry("CREATE_STOCK_TRANSACTION", savedTransaction.getId(), 
        "Created transaction: " + savedTransaction.getType() + ", delta: " + savedTransaction.getQuantityDelta());
    
    return stockTransactionMapper.toDto(savedTransaction);
}

private void updateStockLevelFromTransaction(StockTransaction transaction) {
    StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(transaction.getInventoryItemId())
            .orElseGet(() -> {
                StockLevel newLevel = new StockLevel();
                newLevel.setInventoryItem(transaction.getInventoryItem());
                newLevel.setOnHand(0);
                newLevel.setCommitted(0);
                newLevel.setAvailable(0);
                return newLevel;
            });
    
    stockLevel.setOnHand(stockLevel.getOnHand() + transaction.getQuantityDelta());
    stockLevel.setAvailable(stockLevel.getOnHand() - stockLevel.getCommitted());
    
    stockLevelRepository.save(stockLevel);
}
```

### 5. Update createInventoryItem

**Current Issue**: Uses `productTemplateId`, should use `variantId`

**Fix**:
```java
@Override
@Transactional
public InventoryItemDTO createInventoryItem(CreateInventoryItemDTO createDTO) {
    ProductVariant variant = findVariantOrThrow(createDTO.getVariantId());
    Location location = findLocationOrThrow(createDTO.getLocationId());
    
    // Check if inventory item already exists (variant + location + batchLot)
    Optional<InventoryItem> existing = inventoryItemRepository
            .findByVariantIdAndLocationIdAndBatchLotId(
                createDTO.getVariantId(), 
                createDTO.getLocationId(), 
                createDTO.getBatchLotId());
    
    if (existing.isPresent()) {
        throw new DuplicateResourceException("Inventory item already exists for this variant, location, and batch");
    }
    
    InventoryItem newItem = new InventoryItem();
    newItem.setVariant(variant);
    newItem.setLocation(location);
    if (createDTO.getBatchLotId() != null) {
        newItem.setBatchLot(findBatchLotOrThrow(createDTO.getBatchLotId()));
    }
    if (createDTO.getExpiryDate() != null) {
        newItem.setExpiryDate(createDTO.getExpiryDate());
    }
    
    InventoryItem savedItem = inventoryItemRepository.save(newItem);
    
    // Create initial stock level
    StockLevel stockLevel = new StockLevel();
    stockLevel.setInventoryItem(savedItem);
    stockLevel.setOnHand(0);
    stockLevel.setCommitted(0);
    stockLevel.setAvailable(0);
    stockLevelRepository.save(stockLevel);
    
    return inventoryItemMapper.toDto(savedItem);
}
```

### 6. Implement New Helper Methods

```java
private ProductVariant findVariantOrThrow(UUID variantId) {
    return productVariantRepository.findByIdAndOrganizationId(variantId, TenantContext.getCurrentOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Product variant not found: " + variantId));
}

private BatchLot findBatchLotOrThrow(UUID batchLotId) {
    return batchLotRepository.findById(batchLotId)
            .orElseThrow(() -> new ResourceNotFoundException("Batch lot not found: " + batchLotId));
}

private UnitOfMeasure findUnitOfMeasureById(UUID uomId) {
    return unitOfMeasureRepository.findByIdAndOrganizationId(uomId, TenantContext.getCurrentOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found: " + uomId));
}
```

## Implementation Checklist

- [ ] Add new repository dependencies to constructor
- [ ] Add new mapper dependencies to constructor
- [ ] Fix createProduct (remove SKU/barcode checks)
- [ ] Fix updateProduct (remove SKU/barcode checks)
- [ ] Replace createInventoryItem (use variantId)
- [ ] Remove updateInventoryItemQuantity
- [ ] Implement createStockTransaction
- [ ] Implement getStockLevel
- [ ] Implement ProductVariant CRUD methods
- [ ] Implement BatchLot CRUD methods
- [ ] Implement UoMConversion CRUD methods
- [ ] Update stock check methods (use variantId)
- [ ] Update processSale (use variants instead of templates)
- [ ] Update processPurchaseOrderReceipt (use variants)

## Notes

- All quantity changes must go through StockTransaction
- StockLevel is maintained from transactions (manual or trigger)
- InventoryItem no longer has direct quantity field
- ProductVariant contains SKU/barcode, not ProductTemplate
- Category uniqueness is per organization (already in entity)

