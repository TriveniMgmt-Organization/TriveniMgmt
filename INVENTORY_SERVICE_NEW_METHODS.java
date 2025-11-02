// This file contains new methods that need to be added to InventoryServiceImpl
// Copy and integrate these methods into the service implementation

// ============================================================================
// 1. Add these to the constructor dependencies (already listed in guide)
// ============================================================================

// ============================================================================
// 2. Helper Methods
// ============================================================================

private ProductVariant findVariantOrThrow(UUID variantId) {
    return productVariantRepository.findByIdAndOrganizationId(variantId, TenantContext.getCurrentOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with ID: " + variantId));
}

private BatchLot findBatchLotOrThrow(UUID batchLotId) {
    return batchLotRepository.findById(batchLotId)
            .orElseThrow(() -> new ResourceNotFoundException("Batch lot not found with ID: " + batchLotId));
}

// ============================================================================
// 3. Product Variant Management
// ============================================================================

@Override
@Transactional
public ProductVariantDTO createProductVariant(CreateProductVariantDTO createDTO) {
    log.info("Creating new product variant with SKU: {} for organization ID: {}", 
        createDTO.getSku(), TenantContext.getCurrentOrganizationId());
    
    // Check SKU uniqueness
    if (productVariantRepository.findBySkuAndOrganizationId(
            createDTO.getSku(), TenantContext.getCurrentOrganizationId()).isPresent()) {
        throw new DuplicateResourceException("Variant with SKU '" + createDTO.getSku() + "' already exists in organization.");
    }
    
    // Check barcode uniqueness if provided
    if (createDTO.getBarcode() != null && productVariantRepository.findByBarcodeAndOrganizationId(
            createDTO.getBarcode(), TenantContext.getCurrentOrganizationId()).isPresent()) {
        throw new DuplicateResourceException("Variant with barcode '" + createDTO.getBarcode() + "' already exists in organization.");
    }
    
    ProductTemplate template = findProductTemplateOrThrow(createDTO.getTemplateId());
    
    ProductVariant newVariant = productVariantMapper.toEntity(createDTO);
    newVariant.setOrganization(TenantContext.getCurrentOrganization());
    newVariant.setTemplate(template);
    
    ProductVariant savedVariant = productVariantRepository.save(newVariant);
    logAuditEntry("CREATE_PRODUCT_VARIANT", savedVariant.getId(), "Created variant: " + savedVariant.getSku());
    return productVariantMapper.toDto(savedVariant);
}

@Override
@Transactional(readOnly = true)
public List<ProductVariantDTO> getVariantsByTemplate(UUID templateId) {
    findProductTemplateOrThrow(templateId);
    List<ProductVariant> variants = productVariantRepository.findByTemplateId(templateId);
    return productVariantMapper.toDtoList(variants);
}

@Override
@Transactional(readOnly = true)
public List<ProductVariantDTO> getAllVariants(boolean includeInactive) {
    List<ProductVariant> variants = productVariantRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
    if (!includeInactive) {
        variants = variants.stream().filter(ProductVariant::isActive).collect(Collectors.toList());
    }
    return productVariantMapper.toDtoList(variants);
}

@Override
@Transactional(readOnly = true)
public ProductVariantDTO getVariantById(UUID variantId) {
    ProductVariant variant = findVariantOrThrow(variantId);
    return productVariantMapper.toDto(variant);
}

@Override
@Transactional
public ProductVariantDTO updateVariant(UUID variantId, UpdateProductVariantDTO updateDTO) {
    ProductVariant existing = findVariantOrThrow(variantId);
    
    // Check SKU uniqueness if changing
    if (updateDTO.getSku() != null && !updateDTO.getSku().equals(existing.getSku())) {
        if (productVariantRepository.findBySkuAndOrganizationId(updateDTO.getSku(), 
                TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("SKU '" + updateDTO.getSku() + "' already in use");
        }
    }
    
    // Check barcode uniqueness if changing
    if (updateDTO.getBarcode() != null && !updateDTO.getBarcode().equals(existing.getBarcode())) {
        if (productVariantRepository.findByBarcodeAndOrganizationId(updateDTO.getBarcode(), 
                TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Barcode '" + updateDTO.getBarcode() + "' already in use");
        }
    }
    
    productVariantMapper.updateVariantFromDto(updateDTO, existing);
    ProductVariant saved = productVariantRepository.save(existing);
    logAuditEntry("UPDATE_PRODUCT_VARIANT", saved.getId(), "Updated variant: " + saved.getSku());
    return productVariantMapper.toDto(saved);
}

@Override
@Transactional
public void deleteVariant(UUID variantId) {
    ProductVariant variant = findVariantOrThrow(variantId);
    variant.setActive(false);
    productVariantRepository.save(variant);
    logAuditEntry("DELETE_PRODUCT_VARIANT", variantId, "Deactivated variant: " + variant.getSku());
}

// ============================================================================
// 4. Stock Transaction Management
// ============================================================================

@Override
@Transactional
public StockTransactionDTO createStockTransaction(CreateStockTransactionDTO createDTO) {
    InventoryItem inventoryItem = inventoryItemRepository.findById(createDTO.getInventoryItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + createDTO.getInventoryItemId()));
    
    if (createDTO.getQuantityDelta() == 0) {
        throw new InvalidOperationException("Quantity delta cannot be zero");
    }
    
    // For negative deltas, check stock availability
    if (createDTO.getQuantityDelta() < 0) {
        StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(createDTO.getInventoryItemId())
                .orElse(new StockLevel()); // Default to 0 if not found
        if (stockLevel.getAvailable() == null || stockLevel.getAvailable() < Math.abs(createDTO.getQuantityDelta())) {
            throw new InsufficientStockException("Insufficient stock available. Available: " + 
                (stockLevel.getAvailable() != null ? stockLevel.getAvailable() : 0) + 
                ", Requested: " + Math.abs(createDTO.getQuantityDelta()));
        }
    }
    
    StockTransaction transaction = stockTransactionMapper.toEntity(createDTO);
    transaction.setInventoryItem(inventoryItem);
    // Note: User should come from TenantContext or SecurityContext
    // transaction.setUser(TenantContext.getCurrentUser());
    transaction.setTimestamp(LocalDateTime.now());
    
    StockTransaction savedTransaction = stockTransactionRepository.save(transaction);
    
    // Update stock level
    updateStockLevelFromTransaction(savedTransaction);
    
    logAuditEntry("CREATE_STOCK_TRANSACTION", savedTransaction.getId(), 
        "Transaction: " + savedTransaction.getType() + ", delta: " + savedTransaction.getQuantityDelta());
    
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
                return stockLevelRepository.save(newLevel);
            });
    
    stockLevel.setOnHand(stockLevel.getOnHand() + transaction.getQuantityDelta());
    stockLevel.setAvailable(stockLevel.getOnHand() - stockLevel.getCommitted());
    
    stockLevelRepository.save(stockLevel);
}

@Override
@Transactional(readOnly = true)
public List<StockTransactionDTO> getTransactionsByInventoryItem(UUID inventoryItemId) {
    List<StockTransaction> transactions = stockTransactionRepository
            .findByInventoryItemIdOrderByTimestampDesc(inventoryItemId);
    return stockTransactionMapper.toDtoList(transactions);
}

@Override
@Transactional(readOnly = true)
public StockTransactionDTO getTransactionById(UUID transactionId) {
    StockTransaction transaction = stockTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Stock transaction not found: " + transactionId));
    return stockTransactionMapper.toDto(transaction);
}

@Override
@Transactional(readOnly = true)
public List<StockTransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    List<StockTransaction> transactions = stockTransactionRepository.findByTimestampBetween(startDate, endDate);
    return stockTransactionMapper.toDtoList(transactions);
}

// ============================================================================
// 5. Stock Level Management
// ============================================================================

@Override
@Transactional(readOnly = true)
public StockLevelDTO getStockLevel(UUID inventoryItemId) {
    StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(inventoryItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Stock level not found for inventory item: " + inventoryItemId));
    return stockLevelMapper.toDto(stockLevel);
}

@Override
@Transactional(readOnly = true)
public List<StockLevelDTO> getStockLevelsByVariant(UUID variantId) {
    List<StockLevel> stockLevels = stockLevelRepository.findByVariantId(variantId);
    return stockLevelMapper.toDtoList(stockLevels);
}

@Override
@Transactional(readOnly = true)
public List<StockLevelDTO> getLowStockItems() {
    List<StockLevel> lowStock = stockLevelRepository.findLowStockItems();
    return stockLevelMapper.toDtoList(lowStock);
}

// ============================================================================
// 6. Batch Lot Management
// ============================================================================

@Override
@Transactional
public BatchLotDTO createBatchLot(CreateBatchLotDTO createDTO) {
    if (batchLotRepository.findByBatchNumber(createDTO.getBatchNumber()).isPresent()) {
        throw new DuplicateResourceException("Batch lot with number '" + createDTO.getBatchNumber() + "' already exists");
    }
    
    BatchLot newBatchLot = batchLotMapper.toEntity(createDTO);
    if (createDTO.getSupplierId() != null) {
        newBatchLot.setSupplier(findSupplierOrThrow(createDTO.getSupplierId()));
    }
    
    BatchLot saved = batchLotRepository.save(newBatchLot);
    logAuditEntry("CREATE_BATCH_LOT", saved.getId(), "Created batch: " + saved.getBatchNumber());
    return batchLotMapper.toDto(saved);
}

@Override
@Transactional(readOnly = true)
public List<BatchLotDTO> getAllBatchLots() {
    List<BatchLot> batchLots = batchLotRepository.findAll();
    return batchLotMapper.toDtoList(batchLots);
}

@Override
@Transactional(readOnly = true)
public BatchLotDTO getBatchLotById(UUID batchLotId) {
    BatchLot batchLot = findBatchLotOrThrow(batchLotId);
    return batchLotMapper.toDto(batchLot);
}

@Override
@Transactional(readOnly = true)
public List<BatchLotDTO> getExpiringBatchLots(LocalDate startDate, LocalDate endDate) {
    List<BatchLot> expiring = batchLotRepository.findExpiringBetween(startDate, endDate);
    return batchLotMapper.toDtoList(expiring);
}

// ============================================================================
// 7. UoM Conversion Management
// ============================================================================

@Override
@Transactional
public UoMConversionDTO createUoMConversion(CreateUoMConversionDTO createDTO) {
    UnitOfMeasure fromUom = findUnitOfMeasureOrThrow(createDTO.getFromUomId());
    UnitOfMeasure toUom = findUnitOfMeasureOrThrow(createDTO.getToUomId());
    
    if (fromUom.getId().equals(toUom.getId())) {
        throw new InvalidOperationException("From and To UoM cannot be the same");
    }
    
    if (uomConversionRepository.findByFromUomIdAndToUomId(createDTO.getFromUomId(), createDTO.getToUomId()).isPresent()) {
        throw new DuplicateResourceException("Conversion already exists for these UoMs");
    }
    
    if (createDTO.getRatio().compareTo(BigDecimal.ZERO) <= 0) {
        throw new InvalidOperationException("Conversion ratio must be positive");
    }
    
    UoMConversion conversion = uomConversionMapper.toEntity(createDTO);
    conversion.setFromUom(fromUom);
    conversion.setToUom(toUom);
    
    UoMConversion saved = uomConversionRepository.save(conversion);
    logAuditEntry("CREATE_UOM_CONVERSION", saved.getId(), 
        "Created conversion: " + fromUom.getName() + " to " + toUom.getName());
    return uomConversionMapper.toDto(saved);
}

@Override
@Transactional(readOnly = true)
public List<UoMConversionDTO> getAllUoMConversions() {
    List<UoMConversion> conversions = uomConversionRepository.findAll();
    return uomConversionMapper.toDtoList(conversions);
}

@Override
@Transactional(readOnly = true)
public UoMConversionDTO getUoMConversionById(UUID conversionId) {
    UoMConversion conversion = uomConversionRepository.findById(conversionId)
            .orElseThrow(() -> new ResourceNotFoundException("UoM conversion not found: " + conversionId));
    return uomConversionMapper.toDto(conversion);
}

@Override
@Transactional(readOnly = true)
public List<UoMConversionDTO> getConversionsByUom(UUID uomId) {
    List<UoMConversion> conversions = uomConversionRepository.findAllByUomId(uomId);
    return uomConversionMapper.toDtoList(conversions);
}

@Override
@Transactional(readOnly = true)
public UoMConversionDTO getConversion(UUID fromUomId, UUID toUomId) {
    UoMConversion conversion = uomConversionRepository.findByFromUomIdAndToUomId(fromUomId, toUomId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversion not found from UoM " + fromUomId + " to " + toUomId));
    return uomConversionMapper.toDto(conversion);
}

// ============================================================================
// 8. Updated Stock Check Methods
// ============================================================================

@Override
@Transactional(readOnly = true)
public int getTotalStockQuantity(UUID variantId) {
    Integer total = stockLevelRepository.getTotalOnHandByVariantId(variantId);
    return total != null ? total : 0;
}

@Override
@Transactional(readOnly = true)
public int getTotalStockQuantityForTemplate(UUID templateId) {
    // Sum across all variants of the template
    List<ProductVariant> variants = productVariantRepository.findByTemplateId(templateId);
    return variants.stream()
            .mapToInt(v -> getTotalStockQuantity(v.getId()))
            .sum();
}

@Override
@Transactional(readOnly = true)
public int getStockQuantityAtLocation(UUID variantId, UUID locationId) {
    List<StockLevel> levels = stockLevelRepository.findByVariantId(variantId);
    return levels.stream()
            .filter(sl -> sl.getInventoryItem().getLocation().getId().equals(locationId))
            .mapToInt(StockLevel::getOnHand)
            .sum();
}

@Override
@Transactional(readOnly = true)
public BigDecimal getVariantRetailPrice(UUID variantId) {
    ProductVariant variant = findVariantOrThrow(variantId);
    return variant.getRetailPrice();
}

@Override
@Transactional(readOnly = true)
public boolean checkStockAvailability(UUID variantId, int quantityNeeded) {
    return getTotalStockQuantity(variantId) >= quantityNeeded;
}

@Override
@Transactional(readOnly = true)
public boolean checkStockAvailabilityAtLocation(UUID variantId, UUID locationId, int quantityNeeded) {
    return getStockQuantityAtLocation(variantId, locationId) >= quantityNeeded;
}

// ============================================================================
// 9. Updated Inventory Item Methods
// ============================================================================

@Override
@Transactional(readOnly = true)
public List<InventoryItemDTO> getInventoryItemsForVariant(UUID variantId) {
    findVariantOrThrow(variantId);
    List<InventoryItem> items = inventoryItemRepository.findByVariantId(variantId);
    return inventoryItemMapper.toDtoList(items);
}

@Override
@Transactional(readOnly = true)
public List<InventoryItemDTO> getInventoryItemsForTemplate(UUID templateId) {
    findProductTemplateOrThrow(templateId);
    // Get via variants
    List<ProductVariant> variants = productVariantRepository.findByTemplateId(templateId);
    return variants.stream()
            .flatMap(v -> inventoryItemRepository.findByVariantId(v.getId()).stream())
            .map(inventoryItemMapper::toDto)
            .collect(Collectors.toList());
}

