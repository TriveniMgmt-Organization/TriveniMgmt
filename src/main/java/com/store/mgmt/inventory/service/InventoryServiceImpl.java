package com.store.mgmt.inventory.service;

import com.store.mgmt.common.exception.ResourceNotFoundException;
import com.store.mgmt.config.TenantContext;
import com.store.mgmt.inventory.exceptions.DuplicateResourceException;
import com.store.mgmt.inventory.exceptions.InsufficientStockException;
import com.store.mgmt.inventory.exceptions.InvalidOperationException;
import com.store.mgmt.inventory.mapper.*;
import com.store.mgmt.inventory.model.dto.*;
import com.store.mgmt.inventory.model.entity.*;
import com.store.mgmt.inventory.model.enums.*;
import com.store.mgmt.inventory.repository.*;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import com.store.mgmt.organization.repository.UserOrganizationRoleRepository;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.service.AuditLogService;
import lombok.extern.slf4j.Slf4j; // For logging
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final ProductTemplateRepository productTemplateRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryLocationRepository inventoryLocationRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final DiscountRepository discountRepository;
    private final DamageLossRepository damageLossRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final AuditLogService auditLogService;
    
    // New repositories
    private final ProductVariantRepository productVariantRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockLevelRepository stockLevelRepository;
    private final BatchLotRepository batchLotRepository;
    private final UoMConversionRepository uomConversionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Mappers
    private final ProductTemplateMapper productTemplateMapper;
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final SaleMapper saleMapper;
    private final SaleItemMapper saleItemMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final DiscountMapper discountMapper;
    private final DamageLossMapper damageLossMapper;
    private final SupplierMapper supplierMapper;
    private final InventoryLocationMapper inventoryLocationMapper;
    private final UnitOfMeasureMapper unitOfMeasureMapper;
    
    // New mappers
    private final ProductVariantMapper productVariantMapper;
    private final StockTransactionMapper stockTransactionMapper;
    private final StockLevelMapper stockLevelMapper;
    private final BatchLotMapper batchLotMapper;
    private final UoMConversionMapper uomConversionMapper;

    public InventoryServiceImpl(
            ProductTemplateRepository productTemplateRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            SupplierRepository supplierRepository,
            InventoryItemRepository inventoryItemRepository,
            InventoryLocationRepository inventoryLocationRepository,
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            DiscountRepository discountRepository,
            DamageLossRepository damageLossRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            UserOrganizationRoleRepository userOrganizationRoleRepository,
            AuditLogService auditLogService,
            ProductTemplateMapper productTemplateMapper,
            CategoryMapper categoryMapper,
            BrandMapper brandMapper,
            InventoryItemMapper inventoryItemMapper,
            SaleMapper saleMapper,
            SaleItemMapper saleItemMapper,
            PurchaseOrderMapper purchaseOrderMapper,
            PurchaseOrderItemMapper purchaseOrderItemMapper,
            DiscountMapper discountMapper,
            DamageLossMapper damageLossMapper,
            SupplierMapper supplierMapper,
            InventoryLocationMapper inventoryLocationMapper,
            UnitOfMeasureMapper unitOfMeasureMapper,
            ProductVariantRepository productVariantRepository,
            StockTransactionRepository stockTransactionRepository,
            StockLevelRepository stockLevelRepository,
            BatchLotRepository batchLotRepository,
            UoMConversionRepository uomConversionRepository,
            ProductVariantMapper productVariantMapper,
            StockTransactionMapper stockTransactionMapper,
            StockLevelMapper stockLevelMapper,
            BatchLotMapper batchLotMapper,
            UoMConversionMapper uomConversionMapper
    ) {
        this.productTemplateRepository = productTemplateRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.supplierRepository = supplierRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryLocationRepository = inventoryLocationRepository;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.discountRepository = discountRepository;
        this.damageLossRepository = damageLossRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.auditLogService = auditLogService;
        this.productVariantRepository = productVariantRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.batchLotRepository = batchLotRepository;
        this.uomConversionRepository = uomConversionRepository;
        this.productTemplateMapper = productTemplateMapper;
        this.categoryMapper = categoryMapper;
        this.brandMapper = brandMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.saleMapper = saleMapper;
        this.saleItemMapper = saleItemMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderItemMapper = purchaseOrderItemMapper;
        this.discountMapper = discountMapper;
        this.damageLossMapper = damageLossMapper;
        this.supplierMapper = supplierMapper;
        this.inventoryLocationMapper = inventoryLocationMapper;
        this.unitOfMeasureMapper = unitOfMeasureMapper;
        this.productVariantMapper = productVariantMapper;
        this.stockTransactionMapper = stockTransactionMapper;
        this.stockLevelMapper = stockLevelMapper;
        this.batchLotMapper = batchLotMapper;
        this.uomConversionMapper = uomConversionMapper;
    }

    // --- Helper Methods ---

    private ProductTemplate findProductTemplateOrThrow(UUID productId) {
        return productTemplateRepository.findByIdAndOrganizationId(productId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductTemplate not found with ID: " + productId));
    }

    private Brand findBrandOrThrow(UUID brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + brandId));
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findByIdAndOrganizationId(categoryId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
    }

    private InventoryLocation findInventoryLocationOrThrow(UUID locationId) {
        return inventoryLocationRepository.findByIdAndStoreId(locationId, TenantContext.getCurrentStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + locationId));
    }

    private Supplier findSupplierOrThrow(UUID supplierId) {
        return supplierRepository.findByIdAndOrganizationId(supplierId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + supplierId));
    }

    private InventoryItem findInventoryItemOrThrow(UUID inventoryItemId) {
        return inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + inventoryItemId));
    }

    private ProductVariant findVariantOrThrow(UUID variantId) {
        return productVariantRepository.findByIdAndOrganizationId(variantId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with ID: " + variantId));
    }

    private BatchLot findBatchLotOrThrow(UUID batchLotId) {
        return batchLotRepository.findById(batchLotId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch lot not found with ID: " + batchLotId));
    }

    /**
     * Gets or creates a batch lot. If customBatchNumber is provided, uses it; otherwise auto-generates.
     * Auto-generated format: LOT-YYYYMMDD-SEQ (e.g., LOT-20251113-001)
     */
    private BatchLot getOrCreateBatchLot(String customBatchNumber, LocalDate expiryDate) {
        String batchNumber = (customBatchNumber != null && !customBatchNumber.trim().isEmpty())
            ? customBatchNumber.trim()
            : generateBatchLotNumber();
        
        return batchLotRepository.findByBatchNumber(batchNumber)
                .orElseGet(() -> {
                    CreateBatchLotDTO dto = new CreateBatchLotDTO();
                    dto.setBatchNumber(batchNumber);
                    dto.setExpiryDate(expiryDate);
                    BatchLot newBatchLot = batchLotMapper.toEntity(dto);
                    BatchLot saved = batchLotRepository.save(newBatchLot);
                    if (customBatchNumber == null || customBatchNumber.trim().isEmpty()) {
                        log.debug("Auto-generated batch lot: {}", batchNumber);
                    }
                    return saved;
                });
    }

    /**
     * Generates a batch lot number in the format: LOT-YYYYMMDD-SEQ
     * Uses optimized database query to get max sequence directly
     */
    private String generateBatchLotNumber() {
        String datePrefix = "LOT-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        Integer maxSeq = batchLotRepository.findMaxSequenceForPattern(datePrefix + "%", datePrefix.length());
        return datePrefix + String.format("%03d", (maxSeq != null ? maxSeq : 0) + 1);
    }

    private PurchaseOrder findPurchaseOrderOrThrow(UUID purchaseOrderId) {
        return purchaseOrderRepository.findByIdAndOrganizationId(purchaseOrderId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + purchaseOrderId));
    }

    private Sale findSaleOrThrow(UUID saleId) {
        return saleRepository.findByIdAndStoreId(saleId, TenantContext.getCurrentStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + saleId));
    }

    private Discount findDiscountOrThrow(UUID discountId) {
        return discountRepository.findByIdAndStoreId(discountId, TenantContext.getCurrentStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));
    }

    private UnitOfMeasure findUnitOfMeasureOrThrow(UUID uomId) {
        return unitOfMeasureRepository.findByIdAndOrganizationId(uomId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit of Measure not found with ID: " + uomId));
    }

    private User findUserOrThrow(UUID userId) {
        return userOrganizationRoleRepository.findByUserIdAndOrganizationId(userId, TenantContext.getCurrentOrganizationId())
                .map(UserOrganizationRole::getUser)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private DamageLoss findDamageLossOrThrow(UUID damageLossId) {
        return damageLossRepository.findByIdAndOrganizationId(damageLossId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Damage/Loss record not found with ID: " + damageLossId));
    }

    // --- Brand Management ---
    @Override
    @Transactional
    public BrandDTO createBrand(CreateBrandDTO createDTO) {
        log.info("Creating new brand with name: {} for organization ID: {}", createDTO.getName());

        if (brandRepository.findByName(createDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Brand with name '" + createDTO.getName() + "' already exists.");
        }

        Brand newBrand = brandMapper.toEntity(createDTO);
        Brand savedBrand = brandRepository.save(newBrand);
        logAuditEntry("CREATE_BRAND", savedBrand.getId(), "Created brand: " + savedBrand.getName());
        log.info("Brand created with ID: {}", savedBrand.getId());
        return brandMapper.toDto(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandDTO> getAllBrands(boolean includeInactive) {
        List<Brand> brands = brandRepository.findAll();
        if (!includeInactive) {
            brands = brands.stream().filter(Brand::isActive).collect(Collectors.toList());
        }
        return brandMapper.toDtoList(brands);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandDTO getBrandById(UUID brandId) {
        log.debug("Fetching brand with ID: {} for organization ID: {}", brandId, TenantContext.getCurrentOrganizationId());
        Brand brand = findBrandOrThrow(brandId);
        return brandMapper.toDto(brand);
    }

    @Override
    @Transactional
    public BrandDTO updateBrand(UUID brandId, UpdateBrandDTO updateDTO) {
        log.info("Updating brand with ID: {} for organization ID: {}", brandId);
        Brand existingBrand = findBrandOrThrow(brandId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingBrand.getName())) {
            if (brandRepository.findByName(updateDTO.getName()).isPresent()) {
                throw new DuplicateResourceException("Brand with name '" + updateDTO.getName() + "' already exists.");
            }
        }

        brandMapper.updateBrandFromDto(updateDTO, existingBrand);
        Brand updatedBrand = brandRepository.save(existingBrand);
        logAuditEntry("UPDATE_BRAND", updatedBrand.getId(), "Updated brand: " + updatedBrand.getName());
        log.info("Brand updated with ID: {}", updatedBrand.getId());
        return brandMapper.toDto(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(UUID brandId) {
        log.warn("Attempting to logically delete brand with ID: {} for organization ID: {}", brandId, TenantContext.getCurrentOrganizationId());
        Brand brand = findBrandOrThrow(brandId);
        brand.setActive(false);
        brandRepository.save(brand);
        logAuditEntry("DELETE_BRAND", brandId, "Logically deleted brand: " + brand.getName());
        log.info("Brand with ID {} logically deleted (set to inactive).", brandId);
    }

    // --- Product Management ---

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllProductCategories(boolean includeInactive) {
        log.debug("Fetching all product categories for organization ID: {}", TenantContext.getCurrentOrganizationId());
        List<Category> categories = categoryRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        if (!includeInactive) {
            categories = categories.stream().filter(Category::isActive).collect(Collectors.toList());
        }
        return categoryMapper.toDtoList(categories);
    }

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
        log.info("Product template created with ID: {}", savedProduct.getId());
        return productTemplateMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts(boolean includeInactive) {
        log.debug("Fetching all products for organization ID: {}, includeInactive: {}", TenantContext.getCurrentOrganizationId(), includeInactive);
        List<ProductTemplate> products = productTemplateRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        if (!includeInactive) {
            products = products.stream().filter(pt -> pt.isActive()).collect(Collectors.toList());
        }
        return productTemplateMapper.toDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID productId) {
        log.debug("Fetching product with ID: {} for organization ID: {}", productId, TenantContext.getCurrentOrganizationId());
        ProductTemplate product = findProductTemplateOrThrow(productId);
        return productTemplateMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(UUID productId, UpdateProductDTO updateDTO) {
        log.info("Updating product template with ID: {} for organization ID: {}", productId, TenantContext.getCurrentOrganizationId());
        ProductTemplate existingProduct = findProductTemplateOrThrow(productId);

        productTemplateMapper.updateProductFromDto(updateDTO, existingProduct);

        if (updateDTO.getCategoryId() != null) existingProduct.setCategory(findCategoryOrThrow(updateDTO.getCategoryId()));
        if (updateDTO.getUnitOfMeasureId() != null) existingProduct.setUnitOfMeasure(findUnitOfMeasureOrThrow(updateDTO.getUnitOfMeasureId()));
        if (updateDTO.getBrandId() != null) existingProduct.setBrand(findBrandOrThrow(updateDTO.getBrandId()));

        ProductTemplate updatedProduct = productTemplateRepository.save(existingProduct);
        logAuditEntry("UPDATE_PRODUCT", updatedProduct.getId(), "Updated product template: " + updatedProduct.getName());
        log.info("Product template updated with ID: {}", updatedProduct.getId());
        return productTemplateMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId) {
        log.warn("Attempting to logically delete product with ID: {} for organization ID: {}", productId, TenantContext.getCurrentOrganizationId());
        ProductTemplate product = findProductTemplateOrThrow(productId);
        product.setActive(false);
        productTemplateRepository.save(product);
        logAuditEntry("DELETE_PRODUCT", productId, "Logically deleted product: " + product.getName());
        log.info("Product with ID {} logically deleted (set to inactive).", productId);
    }

    // --- Inventory Item Management ---

    @Override
    @Transactional
    public InventoryItemDTO createInventoryItem(CreateInventoryItemDTO createDTO) {
        log.info("Creating new inventory item for variant ID: {} at location ID: {}", createDTO.getVariantId(), createDTO.getLocationId());
        
        // Validate and fetch entities
        ProductVariant variant = findVariantOrThrow(createDTO.getVariantId());
        InventoryLocation location = findInventoryLocationOrThrow(createDTO.getLocationId());

        // Validate business rules
        if (!variant.isActive()) {
            throw new InvalidOperationException("Cannot create inventory item for inactive variant: " + variant.getSku());
        }
        if (!location.isActive()) {
            throw new InvalidOperationException("Cannot create inventory item for inactive location: " + location.getName());
        }
        // Validate that variant's organization matches location's store's organization
        if (!variant.getOrganization().getId().equals(location.getStore().getOrganization().getId())) {
            throw new SecurityException("Variant and location must belong to the same organization");
        }

        // Get or create batch lot (synchronized to prevent race conditions)
        BatchLot batchLot = createDTO.getBatchLotId() != null 
            ? findBatchLotOrThrow(createDTO.getBatchLotId())
            : getOrCreateBatchLotSynchronized(createDTO.getCustomBatchNumber(), createDTO.getExpiryDate());

        // Create inventory item
        InventoryItem newItem = new InventoryItem();
        newItem.setVariant(variant);
        newItem.setLocation(location);
        newItem.setBatchLot(batchLot);
        
        if (createDTO.getExpiryDate() != null) {
            newItem.setExpiryDate(createDTO.getExpiryDate());
        }

        // Save inventory item - let database unique constraint handle duplicates
        InventoryItem savedItem;
        try {
            savedItem = inventoryItemRepository.save(newItem);
            // Flush to trigger constraint violation immediately if duplicate exists
            entityManager.flush();
        } catch (Exception e) {
            // Check if it's a constraint violation
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("uq_inventory_item")) {
                throw new DuplicateResourceException("Inventory item already exists for this variant, location, and batch combination.");
            }
            throw e;
        }

        // Create StockLevel in same transaction - if this fails, entire transaction rolls back
        StockLevel stockLevel = new StockLevel();
        stockLevel.setInventoryItem(savedItem);
        stockLevel.setOnHand(0);
        stockLevel.setCommitted(0);
        stockLevel.setAvailable(0);
        stockLevel.setLowStockThreshold(10);

        try {
            stockLevelRepository.save(stockLevel);
            entityManager.flush(); // Ensure it's persisted
        } catch (Exception e) {
            log.error("Failed to create StockLevel for InventoryItem {}: {}", savedItem.getId(), e.getMessage());
            throw new InvalidOperationException("Failed to initialize stock level for inventory item: " + e.getMessage());
        }

        // Update the saved item reference for consistency
        savedItem.setStockLevel(stockLevel);

        logAuditEntry("CREATE_INVENTORY_ITEM", savedItem.getId(), "Created inventory item for variant: " + variant.getSku());
        log.info("New inventory item created with ID: {}", savedItem.getId());
        return inventoryItemMapper.toDto(savedItem);
    }

    /**
     * Synchronized method to prevent race conditions when creating batch lots.
     * Uses database-level locking to ensure only one batch lot is created for a given batch number.
     */
    private synchronized BatchLot getOrCreateBatchLotSynchronized(String customBatchNumber, LocalDate expiryDate) {
        String batchNumber = (customBatchNumber != null && !customBatchNumber.trim().isEmpty())
            ? customBatchNumber.trim()
            : generateBatchLotNumber();
        
        // Use pessimistic locking to prevent race conditions
        return batchLotRepository.findByBatchNumber(batchNumber)
                .orElseGet(() -> {
                    CreateBatchLotDTO dto = new CreateBatchLotDTO();
                    dto.setBatchNumber(batchNumber);
                    dto.setExpiryDate(expiryDate);
                    BatchLot newBatchLot = batchLotMapper.toEntity(dto);
                    try {
                        BatchLot saved = batchLotRepository.save(newBatchLot);
                        entityManager.flush(); // Ensure it's persisted immediately
                        if (customBatchNumber == null || customBatchNumber.trim().isEmpty()) {
                            log.debug("Auto-generated batch lot: {}", batchNumber);
                        }
                        return saved;
                    } catch (Exception e) {
                        // If save fails due to duplicate, try to fetch again
                        log.warn("Failed to save batch lot, attempting to retrieve existing: {}", e.getMessage());
                        return batchLotRepository.findByBatchNumber(batchNumber)
                                .orElseThrow(() -> new InvalidOperationException("Failed to create or retrieve batch lot: " + e.getMessage()));
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemDTO getInventoryItemById(UUID inventoryItemId) {
        log.debug("Fetching inventory item with ID: {} for store ID: {}", inventoryItemId, TenantContext.getCurrentStoreId());
        InventoryItem item = findInventoryItemOrThrow(inventoryItemId);
        return inventoryItemMapper.toDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getAllInventoryItems() {
        UUID storeId = TenantContext.getCurrentStoreId();
        log.debug("Fetching all inventory items for store ID: {}", storeId);
        if (storeId == null) {
            throw new IllegalStateException("Store context is not set. Cannot fetch inventory items without a store context.");
        }
        List<InventoryItem> items = inventoryItemRepository.findByStoreId(storeId);
        return inventoryItemMapper.toDtoList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getInventoryItemsForTemplate(UUID templateId) {
        log.debug("Fetching all inventory items for template ID: {}", templateId);
        findProductTemplateOrThrow(templateId);
        // Get via variants
        List<ProductVariant> variants = productVariantRepository.findByTemplateId(templateId);
        return variants.stream()
                .flatMap(v -> inventoryItemRepository.findByVariantId(v.getId()).stream())
                .map(inventoryItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getInventoryItemsForVariant(UUID variantId) {
        log.debug("Fetching all inventory items for variant ID: {}", variantId);
        findVariantOrThrow(variantId);
        List<InventoryItem> items = inventoryItemRepository.findByVariantId(variantId);
        return inventoryItemMapper.toDtoList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getInventoryItemsAtLocation(UUID locationId) {
        log.debug("Fetching all inventory items at location ID: {}", locationId);
        findInventoryLocationOrThrow(locationId);
        List<InventoryItem> items = inventoryItemRepository.findByLocationId(locationId);
        return inventoryItemMapper.toDtoList(items);
    }

    // REMOVED: updateInventoryItemQuantity - use createStockTransaction instead
    // All quantity changes must go through StockTransaction for audit trail

    @Override
    @Transactional
    public void deleteInventoryItem(UUID inventoryItemId) {
        log.warn("Deleting inventory item with ID: {} for store ID: {}", inventoryItemId, TenantContext.getCurrentStoreId());
        InventoryItem item = findInventoryItemOrThrow(inventoryItemId);
        inventoryItemRepository.delete(item);
        logAuditEntry("DELETE_INVENTORY_ITEM", inventoryItemId, "Deleted inventory item");
        log.info("Inventory item with ID {} deleted.", inventoryItemId);
    }

    // --- Stock Operations ---

    @Override
    @Transactional
    public void processSale(CreateSaleDTO saleDTO) {
        log.info("Processing new sale for store ID: {}", TenantContext.getCurrentStoreId());

        if (saleDTO == null || saleDTO.getItems() == null || saleDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("Sale DTO and its items must not be null or empty");
        }

        User user = null;
        if (saleDTO.getUserId() != null) {
            user = findUserOrThrow(saleDTO.getUserId());
        }

        Sale newSale = saleMapper.toEntity(saleDTO);
        newSale.setStore(TenantContext.getCurrentStore());
        newSale.setSaleTimestamp(LocalDateTime.now());
        newSale.setPaymentMethod(PaymentMethod.valueOf(String.valueOf(saleDTO.getPaymentMethod())));
        newSale.setUser(user);

        Set<SaleItem> saleItems = saleDTO.getItems().stream().map(itemDTO -> {
            ProductTemplate product = findProductTemplateOrThrow(itemDTO.getProductTemplateId());
            List<InventoryItem> availableInventoryItems = getAvailableInventoryItems(product.getId());
            int quantityToSell = itemDTO.getQuantity();

            if (getTotalStockQuantity(product.getId()) < quantityToSell) {
                throw new InsufficientStockException("Insufficient total stock for product " + product.getName() + ". Requested: " + quantityToSell + ", Available: " + getTotalStockQuantity(product.getId()));
            }

            allocateStock(availableInventoryItems, quantityToSell, product.getName());

            return createSaleItem(itemDTO, newSale, product);
        }).collect(Collectors.toSet());

        BigDecimal totalAmount = saleItems.stream()
                .map(saleItem -> saleItem.getUnitPrice().multiply(BigDecimal.valueOf(saleItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscountAmount = saleItems.stream()
                .map(SaleItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        newSale.setTotalAmount(totalAmount.subtract(totalDiscountAmount));
        newSale.setTotalDiscountAmount(totalDiscountAmount);
        newSale.setSaleItems(saleItems);

        Sale savedSale = saleRepository.save(newSale);
        saleItemRepository.saveAll(saleItems);
        logAuditEntry("CREATE_SALE", savedSale.getId(), "Processed sale with total: " + savedSale.getTotalAmount());
        log.info("Sale with ID {} processed successfully.", savedSale.getId());
    }

    private List<InventoryItem> getAvailableInventoryItems(UUID variantId) {
        List<InventoryItem> items = inventoryItemRepository.findByVariantId(variantId);
        // Filter by available stock levels and sort by expiry
        return items.stream()
                .filter(item -> {
                    StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(item.getId()).orElse(null);
                    return stockLevel != null && stockLevel.getAvailable() > 0;
                })
                .sorted((item1, item2) -> {
                    if (item1.getExpiryDate() != null && item2.getExpiryDate() != null) {
                        return item1.getExpiryDate().compareTo(item2.getExpiryDate());
                    }
                    if (item1.getExpiryDate() != null) return -1;
                    if (item2.getExpiryDate() != null) return 1;
                    return item1.getCreatedAt().compareTo(item2.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    private void allocateStock(List<InventoryItem> availableInventoryItems, int quantityToSell, String productName) {
        int remainingToSell = quantityToSell;
        for (InventoryItem inventoryItem : availableInventoryItems) {
            if (remainingToSell <= 0) break;

            // Get current stock level for this inventory item
            StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(inventoryItem.getId())
                    .orElse(new StockLevel());
            int availableQuantity = stockLevel.getAvailable();
            
            int quantityTaken = Math.min(remainingToSell, availableQuantity);
            if (quantityTaken > 0) {
                // Create stock transaction for the sale
                CreateStockTransactionDTO transactionDTO = new CreateStockTransactionDTO();
                transactionDTO.setInventoryItemId(inventoryItem.getId());
                transactionDTO.setQuantityDelta(-quantityTaken);
                transactionDTO.setType(TransactionType.SALE.name());
                transactionDTO.setReference("SALE");
                
                createStockTransaction(transactionDTO);
                remainingToSell -= quantityTaken;
                log.debug("Decremented {} units from inventory item ID {} for product {}", quantityTaken, inventoryItem.getId(), productName);
            }
        }

        if (remainingToSell > 0) {
            throw new InsufficientStockException("Failed to fully allocate stock for product " + productName + ". Remaining: " + remainingToSell);
        }
    }

    private SaleItem createSaleItem(CreateSaleItemDTO itemDTO, Sale newSale, ProductTemplate product) {
        SaleItem saleItem = saleItemMapper.toEntity(itemDTO);
        saleItem.setSale(newSale);
        saleItem.setProductTemplate(product);
        saleItem.setUnitPrice(itemDTO.getUnitPrice());
        saleItem.setDiscountAmount(itemDTO.getDiscountAmount());
        return saleItem;
    }

    @Override
    @Transactional
    public void processPurchaseOrderReceipt(UUID purchaseOrderId, List<PurchaseOrderItemDTO> receivedItems) {
        log.info("Processing receipt for Purchase Order ID: {} for organization ID: {}", purchaseOrderId, TenantContext.getCurrentOrganizationId());
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(purchaseOrderId);

        if (purchaseOrder.getStatus() ==PurchaseOrderStatus.CANCELLED || purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED_COMPLETE) {
            throw new InvalidOperationException("Cannot receive items for a cancelled or already completed purchase order.");
        }

        boolean allItemsReceived = true;

        for (PurchaseOrderItemDTO receivedItemDTO : receivedItems) {
            PurchaseOrderItem orderItem = purchaseOrder.getPurchaseOrderItems().stream()
                    .filter(item -> item.getProductTemplate().getId().equals(receivedItemDTO.getProductTemplateId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidOperationException("Product ID " + receivedItemDTO.getProductTemplateId() + " not found in Purchase Order " + purchaseOrderId));

            if (receivedItemDTO.getQuantity() < 0) {
                throw new InvalidOperationException("Received quantity cannot be negative for product ID: " + receivedItemDTO.getProductTemplateId());
            }

            int newReceivedQuantity = orderItem.getReceivedQuantity() + receivedItemDTO.getQuantity();
            if (newReceivedQuantity > orderItem.getOrderedQuantity()) {
                throw new InvalidOperationException("Received quantity for product " + orderItem.getProductTemplate().getName() + " exceeds ordered quantity in PO " + purchaseOrderId);
            }

            orderItem.setReceivedQuantity(newReceivedQuantity);
            purchaseOrderItemRepository.save(orderItem);

            InventoryLocation receiptLocation = findInventoryLocationOrThrow(receivedItemDTO.getLocationId());
            ProductVariant variant = orderItem.getVariant();
            
            // Find or create BatchLot if batch/lot info is provided
            BatchLot batchLot = null;
            if (receivedItemDTO.getBatchNumber() != null && !receivedItemDTO.getBatchNumber().isEmpty()) {
                batchLot = batchLotRepository.findByBatchNumber(receivedItemDTO.getBatchNumber())
                        .orElseGet(() -> {
                            CreateBatchLotDTO batchLotDTO = new CreateBatchLotDTO();
                            batchLotDTO.setBatchNumber(receivedItemDTO.getBatchNumber());
                            if (receivedItemDTO.getExpirationDate() != null) {
                                batchLotDTO.setExpiryDate(receivedItemDTO.getExpirationDate().toLocalDate());
                            }
                            batchLotDTO.setSupplierId(purchaseOrder.getSupplier().getId());
                            BatchLot newBatchLot = batchLotMapper.toEntity(batchLotDTO);
                            newBatchLot.setSupplier(purchaseOrder.getSupplier());
                            return batchLotRepository.save(newBatchLot);
                        });
            }
            
            // Find or create InventoryItem
            Optional<InventoryItem> existingItem = inventoryItemRepository
                    .findByVariantIdAndLocationIdAndBatchLotId(
                            variant.getId(), 
                            receiptLocation.getId(),
                            batchLot != null ? batchLot.getId() : null);
            
            InventoryItem inventoryItem;
            if (existingItem.isPresent()) {
                inventoryItem = existingItem.get();
            } else {
                inventoryItem = new InventoryItem();
                inventoryItem.setVariant(variant);
                inventoryItem.setLocation(receiptLocation);
                if (batchLot != null) {
                    inventoryItem.setBatchLot(batchLot);
                }
                if (receivedItemDTO.getExpirationDate() != null) {
                    inventoryItem.setExpiryDate(receivedItemDTO.getExpirationDate().toLocalDate());
                }
                inventoryItem = inventoryItemRepository.save(inventoryItem);
            }
            
            // Create StockTransaction for receipt
            StockTransaction receiptTransaction = new StockTransaction();
            receiptTransaction.setInventoryItem(inventoryItem);
            receiptTransaction.setType(TransactionType.RECEIPT);
            receiptTransaction.setQuantityDelta(receivedItemDTO.getQuantity());
            receiptTransaction.setReferenceType("PURCHASE_ORDER");
            receiptTransaction.setReferenceId(purchaseOrderId);
            receiptTransaction.setUser(TenantContext.getCurrentUser());
            stockTransactionRepository.save(receiptTransaction);
            
            // Update stock level
            updateStockLevelFromTransaction(receiptTransaction);
            
            logAuditEntry("PROCESS_PO_RECEIPT", inventoryItem.getId(), 
                    "Received " + receivedItemDTO.getQuantity() + " units of variant " + variant.getSku());

            if (orderItem.getReceivedQuantity() < orderItem.getOrderedQuantity()) {
                allItemsReceived = false;
            }
        }

        if (allItemsReceived) {
            purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED_COMPLETE);
            purchaseOrder.setActualDeliveryDate(LocalDate.now());
            log.info("Purchase Order ID {} marked as RECEIVED_COMPLETE.", purchaseOrderId);
        } else {
            purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED_PARTIAL);
            log.info("Purchase Order ID {} marked as RECEIVED_PARTIAL.", purchaseOrderId);
        }
        purchaseOrderRepository.save(purchaseOrder);
        logAuditEntry("PROCESS_PURCHASE_ORDER_RECEIPT", purchaseOrderId, "Processed receipt for purchase order");
    }

    @Override
    @Transactional
    public DamageLossDTO recordDamageLoss(CreateDamageLossDTO createDTO) {
        log.info("Recording damage/loss for product ID: {} in store ID: {}", createDTO.getProductTemplateId(), TenantContext.getCurrentStoreId());
        ProductTemplate product = findProductTemplateOrThrow(createDTO.getProductTemplateId());
        InventoryLocation location = findInventoryLocationOrThrow(createDTO.getLocationId());
        User user = findUserOrThrow(createDTO.getUserId());

        if (!location.getStore().getId().equals(TenantContext.getCurrentStoreId())) {
            throw new SecurityException("Location does not belong to current store.");
        }

        if (createDTO.getQuantity() <= 0) {
            throw new InvalidOperationException("Quantity for damage/loss must be positive.");
        }

        // Find inventory items for this product template via variants
        List<InventoryItem> itemsForTemplate = inventoryItemRepository.findByTemplateId(product.getId());
        Optional<InventoryItem> itemToDecrementOpt = itemsForTemplate.stream()
                .filter(item -> item.getLocation().getId().equals(location.getId()))
                .filter(item -> {
                    StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(item.getId()).orElse(null);
                    return stockLevel != null && stockLevel.getAvailable() >= createDTO.getQuantity() && stockLevel.getAvailable() > 0;
                })
                .findFirst();

        if (itemToDecrementOpt.isEmpty()) {
            throw new InsufficientStockException("Insufficient specific stock at location " + location.getName() + " for product " + product.getName() + " to record loss.");
        }

        InventoryItem itemToDecrement = itemToDecrementOpt.get();
        
        // Create stock transaction for damage/loss
        CreateStockTransactionDTO transactionDTO = new CreateStockTransactionDTO();
        transactionDTO.setInventoryItemId(itemToDecrement.getId());
        transactionDTO.setQuantityDelta(-createDTO.getQuantity());
        transactionDTO.setType(TransactionType.DAMAGE_LOSS.name());
        transactionDTO.setReference("DAMAGE_LOSS");
        
        createStockTransaction(transactionDTO);

        DamageLoss damageLoss = damageLossMapper.toEntity(createDTO);
        damageLoss.setProductTemplate(product);
        damageLoss.setStore(TenantContext.getCurrentStore());
        damageLoss.setLocation(location);
        damageLoss.setUser(user);
        damageLoss.setReason(DamageLossReason.valueOf(String.valueOf(createDTO.getReason())));

        DamageLoss savedDamageLoss = damageLossRepository.save(damageLoss);
        logAuditEntry("RECORD_DAMAGE_LOSS", savedDamageLoss.getId(), "Recorded damage/loss for product: " + product.getName());
        log.info("Recorded damage/loss ID {} for product ID {}", savedDamageLoss.getId(), product.getId());
        return damageLossMapper.toDto(savedDamageLoss);
    }

    // --- Product Variant Management ---

    @Override
    @Transactional
    public ProductVariantDTO createProductVariant(CreateProductVariantDTO createDTO) {
        log.info("Creating new product variant with SKU: {} for organization ID: {}", 
            createDTO.getSku(), TenantContext.getCurrentOrganizationId());
        
        if (productVariantRepository.findBySkuAndOrganizationId(
                createDTO.getSku(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Variant with SKU '" + createDTO.getSku() + "' already exists in organization.");
        }
        
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
        
        if (updateDTO.getSku() != null && !updateDTO.getSku().equals(existing.getSku())) {
            if (productVariantRepository.findBySkuAndOrganizationId(updateDTO.getSku(), 
                    TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("SKU '" + updateDTO.getSku() + "' already in use");
            }
        }
        
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

    // --- Stock Transaction Management ---

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
                    .orElse(new StockLevel());
            if (stockLevel.getAvailable() < Math.abs(createDTO.getQuantityDelta())) {
                throw new InsufficientStockException("Insufficient stock available. Available: " + 
                    stockLevel.getAvailable() + 
                    ", Requested: " + Math.abs(createDTO.getQuantityDelta()));
            }
        }
        
        StockTransaction transaction = stockTransactionMapper.toEntity(createDTO);
        transaction.setInventoryItem(inventoryItem);
        transaction.setUser(TenantContext.getCurrentUser());
        transaction.setTimestamp(LocalDateTime.now());
        
        StockTransaction savedTransaction = stockTransactionRepository.save(transaction);
        
        // Update stock level
        updateStockLevelFromTransaction(savedTransaction);
        
        logAuditEntry("CREATE_STOCK_TRANSACTION", savedTransaction.getId(), 
            "Transaction: " + savedTransaction.getType() + ", delta: " + savedTransaction.getQuantityDelta());
        
        return stockTransactionMapper.toDto(savedTransaction);
    }

    private void updateStockLevelFromTransaction(StockTransaction transaction) {
        StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(transaction.getInventoryItem().getId())
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

    // --- Stock Level Management ---

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

    // --- Batch/Lot Management ---

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

    // --- UoM Conversion Management ---

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

    // --- Stock Check & Information ---

    @Override
    @Transactional(readOnly = true)
    public int getTotalStockQuantity(UUID variantId) {
        Integer total = stockLevelRepository.getTotalOnHandByVariantId(variantId);
        return total != null ? total : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalStockQuantityForTemplate(UUID templateId) {
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

    // --- Category Management ---

    @Override
    @Transactional
    public CategoryDTO createCategory(CreateCategoryDTO createDTO) {
        log.info("Creating new category: {} for organization ID: {}", createDTO.getName(), TenantContext.getCurrentOrganizationId());
        if (categoryRepository.findByNameAndOrganizationId(createDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + createDTO.getName() + "' already exists in organization.");
        }
        Category newCategory = categoryMapper.toEntity(createDTO);
        newCategory.setOrganization(TenantContext.getCurrentOrganization());
        Category savedCategory = categoryRepository.save(newCategory);
        logAuditEntry("CREATE_CATEGORY", savedCategory.getId(), "Created category: " + savedCategory.getName());
        log.info("Category created with ID: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories for organization ID: {}", TenantContext.getCurrentOrganizationId());
        List<Category> categories = categoryRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        return categoryMapper.toDtoList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(UUID categoryId) {
        log.debug("Fetching category with ID: {} for organization ID: {}", categoryId, TenantContext.getCurrentOrganizationId());
        Category category = findCategoryOrThrow(categoryId);
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(UUID categoryId, UpdateCategoryDTO updateDTO) {
        log.info("Updating category with ID: {} for organization ID: {}", categoryId, TenantContext.getCurrentOrganizationId());
        Category existingCategory = findCategoryOrThrow(categoryId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingCategory.getName())) {
            if (categoryRepository.findByNameAndOrganizationId(updateDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("Category name '" + updateDTO.getName() + "' is already in use in organization.");
            }
        }

        categoryMapper.updateCategoryFromDto(updateDTO, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);
        logAuditEntry("UPDATE_CATEGORY", updatedCategory.getId(), "Updated category: " + updatedCategory.getName());
        log.info("Category updated with ID: {}", updatedCategory.getId());
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        log.warn("Attempting to delete category with ID: {} for organization ID: {}", categoryId, TenantContext.getCurrentOrganizationId());
        Category category = findCategoryOrThrow(categoryId);

        if (!productTemplateRepository.findByCategoryIdAndOrganizationId(categoryId, TenantContext.getCurrentOrganizationId()).isEmpty()) {
            throw new InvalidOperationException("Cannot delete category ID " + categoryId + " as products are associated with it.");
        }

        categoryRepository.delete(category);
        logAuditEntry("DELETE_CATEGORY", categoryId, "Deleted category: " + category.getName());
        log.info("Category with ID {} deleted successfully.", categoryId);
    }

    // --- Supplier Management ---

    @Override
    @Transactional
    public SupplierDTO createSupplier(CreateSupplierDTO createDTO) {
        log.info("Creating new supplier: {} for organization ID: {}", createDTO.getName(), TenantContext.getCurrentOrganizationId());
        if (supplierRepository.findByNameAndOrganizationId(createDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Supplier with name '" + createDTO.getName() + "' already exists in organization.");
        }
        Supplier newSupplier = supplierMapper.toEntity(createDTO);
        newSupplier.setOrganization(TenantContext.getCurrentOrganization());
        Supplier savedSupplier = supplierRepository.save(newSupplier);
        logAuditEntry("CREATE_SUPPLIER", savedSupplier.getId(), "Created supplier: " + savedSupplier.getName());
        log.info("Supplier created with ID: {}", savedSupplier.getId());
        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getAllSuppliers() {
        log.debug("Fetching all suppliers for organization ID: {}", TenantContext.getCurrentOrganizationId());
        List<Supplier> suppliers = supplierRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        return supplierMapper.toDtoList(suppliers);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(UUID supplierId) {
        log.debug("Fetching supplier with ID: {} for organization ID: {}", supplierId, TenantContext.getCurrentOrganizationId());
        Supplier supplier = findSupplierOrThrow(supplierId);
        return supplierMapper.toDto(supplier);
    }

    @Override
    @Transactional
    public SupplierDTO updateSupplier(UUID supplierId, UpdateSupplierDTO updateDTO) {
        log.info("Updating supplier with ID: {} for organization ID: {}", supplierId, TenantContext.getCurrentOrganizationId());
        Supplier existingSupplier = findSupplierOrThrow(supplierId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingSupplier.getName())) {
            if (supplierRepository.findByNameAndOrganizationId(updateDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("Supplier name '" + updateDTO.getName() + "' is already in use in organization.");
            }
        }

        supplierMapper.updateSupplierFromDto(updateDTO, existingSupplier);
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        logAuditEntry("UPDATE_SUPPLIER", updatedSupplier.getId(), "Updated supplier: " + updatedSupplier.getName());
        log.info("Supplier updated with ID: {}", updatedSupplier.getId());
        return supplierMapper.toDto(updatedSupplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(UUID supplierId) {
        log.warn("Attempting to delete supplier with ID: {} for organization ID: {}", supplierId, TenantContext.getCurrentOrganizationId());
        Supplier supplier = findSupplierOrThrow(supplierId);

        if (!purchaseOrderRepository.findBySupplierIdAndOrganizationId(supplierId, TenantContext.getCurrentOrganizationId()).isEmpty()) {
            throw new InvalidOperationException("Cannot delete supplier ID " + supplierId + " as purchase orders are associated with it.");
        }

        supplierRepository.delete(supplier);
        logAuditEntry("DELETE_SUPPLIER", supplierId, "Deleted supplier: " + supplier.getName());
        log.info("Supplier with ID {} deleted successfully.", supplierId);
    }

    // --- Location Management ---

    @Override
    @Transactional
    public InventoryLocationDTO createInventoryLocation(CreateInventoryLocationDTO createDTO) {
        log.info("Creating new location: {} for store ID: {}", createDTO.getName(), TenantContext.getCurrentStoreId());
        if (inventoryLocationRepository.findByNameAndStoreId(createDTO.getName(), TenantContext.getCurrentStoreId()).isPresent()) {
            throw new DuplicateResourceException("Location with name '" + createDTO.getName() + "' already exists in store.");
        }
        InventoryLocation newLocation = inventoryLocationMapper.toEntity(createDTO);
        newLocation.setStore(TenantContext.getCurrentStore());
        newLocation.setType(InventoryLocationType.valueOf(String.valueOf(createDTO.getType())));
        InventoryLocation savedLocation = inventoryLocationRepository.save(newLocation);
        logAuditEntry("CREATE_INVENTORY_LOCATION", savedLocation.getId(), "Created location: " + savedLocation.getName());
        log.info("Location created with ID: {}", savedLocation.getId());
        return inventoryLocationMapper.toDto(savedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryLocationDTO> getAllInventoryLocations() {
        log.debug("Fetching all locations for store ID: {}", TenantContext.getCurrentStoreId());
        List<InventoryLocation> locations = inventoryLocationRepository.findByStoreId(TenantContext.getCurrentStoreId());
        return inventoryLocationMapper.toDtoList(locations);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryLocationDTO getInventoryLocationById(UUID inventoryLocationId) {
        log.debug("Fetching location with ID: {} for store ID: {}", inventoryLocationId, TenantContext.getCurrentStoreId());
        InventoryLocation location = findInventoryLocationOrThrow(inventoryLocationId);
        return inventoryLocationMapper.toDto(location);
    }

    @Override
    @Transactional
    public InventoryLocationDTO updateInventoryLocation(UUID inventoryLocationId, UpdateInventoryLocationDTO updateDTO) {
        log.info("Updating location with ID: {} for store ID: {}", inventoryLocationId, TenantContext.getCurrentStoreId());
        InventoryLocation existingLocation = findInventoryLocationOrThrow(inventoryLocationId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingLocation.getName())) {
            if (inventoryLocationRepository.findByNameAndStoreId(updateDTO.getName(), TenantContext.getCurrentStoreId()).isPresent()) {
                throw new DuplicateResourceException("Location name '" + updateDTO.getName() + "' is already in use in store.");
            }
        }

        inventoryLocationMapper.updateInventoryLocationFromDto(updateDTO, existingLocation);
        if (updateDTO.getType() != null) existingLocation.setType(InventoryLocationType.valueOf(String.valueOf(updateDTO.getType())));

        InventoryLocation updatedLocation = inventoryLocationRepository.save(existingLocation);
        logAuditEntry("UPDATE_INVENTORY_LOCATION", updatedLocation.getId(), "Updated inventory location: " + updatedLocation.getName());
        log.info("Inventory location updated with ID: {}", updatedLocation.getId());
        return inventoryLocationMapper.toDto(updatedLocation);
    }

    @Override
    @Transactional
    public void deleteInventoryLocation(UUID inventoryLocationId) {
        log.warn("Attempting to delete location with ID: {} for store ID: {}", inventoryLocationId, TenantContext.getCurrentStoreId());
        InventoryLocation location = findInventoryLocationOrThrow(inventoryLocationId);

        if (!inventoryItemRepository.findByLocationIdAndStoreId(inventoryLocationId, TenantContext.getCurrentStoreId()).isEmpty()) {
            throw new InvalidOperationException("Cannot delete location ID " + inventoryLocationId + " as inventory items are associated with it.");
        }
        if (!damageLossRepository.findByLocationIdAndStoreId(inventoryLocationId, TenantContext.getCurrentStoreId()).isEmpty()) {
            throw new InvalidOperationException("Cannot delete location ID " + inventoryLocationId + " as damage/loss records are associated with it.");
        }

        inventoryLocationRepository.delete(location);
        logAuditEntry("DELETE_INVENTORY_LOCATION", inventoryLocationId, "Deleted inventory location: " + location.getName());
        log.info("Location with ID {} deleted successfully.", inventoryLocationId);
    }

    // --- Purchase Order Management ---

    @Override
    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderDTO createDTO) {
        log.info("Creating new purchase order for supplier ID: {} in organization ID: {}", createDTO.getSupplierId(), TenantContext.getCurrentOrganizationId());
        Supplier supplier = findSupplierOrThrow(createDTO.getSupplierId());
        User user = findUserOrThrow(createDTO.getUserId());

        PurchaseOrder newPO = purchaseOrderMapper.toEntity(createDTO);
        newPO.setOrganization(TenantContext.getCurrentOrganization());
        newPO.setSupplier(supplier);
        newPO.setOrderDate(LocalDateTime.now());
        newPO.setStatus(PurchaseOrderStatus.PENDING);
        newPO.setUser(user);

        Set<PurchaseOrderItem> poItems = createDTO.getItems().stream().map(itemDTO -> {
            ProductTemplate product = findProductTemplateOrThrow(itemDTO.getProductTemplateId());
            PurchaseOrderItem poItem = purchaseOrderItemMapper.toEntity(itemDTO);
            poItem.setPurchaseOrder(newPO);
            poItem.setProductTemplate(product);
            return poItem;
        }).collect(Collectors.toSet());

        newPO.setPurchaseOrderItems(poItems);
        newPO.setTotalEstimatedAmount(poItems.stream()
                .map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getOrderedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        PurchaseOrder savedPO = purchaseOrderRepository.save(newPO);
        poItems.forEach(item -> item.setPurchaseOrder(savedPO));
        purchaseOrderItemRepository.saveAll(poItems);
        logAuditEntry("CREATE_PURCHASE_ORDER", savedPO.getId(), "Created purchase order for supplier: " + supplier.getName());
        log.info("Purchase Order created with ID: {}", savedPO.getId());
        return purchaseOrderMapper.toDto(savedPO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getAllPurchaseOrders(PurchaseOrderStatus statusFilter) {
        log.debug("Fetching all purchase orders for organization ID: {} with status filter: {}", TenantContext.getCurrentOrganizationId(), statusFilter);
        List<PurchaseOrder> purchaseOrders;
        if (statusFilter != null) {
            purchaseOrders = purchaseOrderRepository.findByStatusAndOrganizationId(statusFilter, TenantContext.getCurrentOrganizationId());
        } else {
            purchaseOrders = purchaseOrderRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        }
        return purchaseOrderMapper.toDtoList(purchaseOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getPurchaseOrderById(UUID purchaseOrderId) {
        log.debug("Fetching purchase order with ID: {} for organization ID: {}", purchaseOrderId, TenantContext.getCurrentOrganizationId());
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(purchaseOrderId);
        return purchaseOrderMapper.toDto(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO updatePurchaseOrder(UUID purchaseOrderId, UpdatePurchaseOrderDTO updateDTO) {
        log.info("Updating purchase order with ID: {} for organization ID: {}", purchaseOrderId, TenantContext.getCurrentOrganizationId());
        PurchaseOrder existingPO = findPurchaseOrderOrThrow(purchaseOrderId);

        if (existingPO.getStatus() ==PurchaseOrderStatus.RECEIVED_COMPLETE || existingPO.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot update a completed or cancelled purchase order.");
        }

        purchaseOrderMapper.updatePurchaseOrderFromDto(updateDTO, existingPO);

        if (updateDTO.getSupplierId() != null) existingPO.setSupplier(findSupplierOrThrow(updateDTO.getSupplierId()));
        if (updateDTO.getUserId() != null) existingPO.setUser(findUserOrThrow(updateDTO.getUserId()));
        if (updateDTO.getStatus() != null) existingPO.setStatus(PurchaseOrderStatus.valueOf(updateDTO.getStatus() != null ? updateDTO.getStatus().toUpperCase() : null));

        PurchaseOrder updatedPO = purchaseOrderRepository.save(existingPO);
        logAuditEntry("UPDATE_PURCHASE_ORDER", updatedPO.getId(), "Updated purchase order");
        log.info("Purchase order updated with ID: {}", updatedPO.getId());
        return purchaseOrderMapper.toDto(updatedPO);
    }

    @Override
    @Transactional
    public void cancelPurchaseOrder(UUID purchaseOrderId) {
        log.warn("Attempting to cancel purchase order with ID: {} for organization ID: {}", purchaseOrderId, TenantContext.getCurrentOrganizationId());
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(purchaseOrderId);

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED_COMPLETE) {
            throw new InvalidOperationException("Cannot cancel a completed purchase order.");
        }
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new InvalidOperationException("Purchase order is already cancelled.");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(purchaseOrder);
        logAuditEntry("CANCEL_PURCHASE_ORDER", purchaseOrderId, "Cancelled purchase order");
        log.info("Purchase Order with ID {} cancelled successfully.", purchaseOrderId);
    }

    // --- Sales History ---

    @Override
    @Transactional(readOnly = true)
    public SaleDTO getSaleById(UUID saleId) {
        log.debug("Fetching sale with ID: {} for store ID: {}", saleId, TenantContext.getCurrentStoreId());
        Sale sale = findSaleOrThrow(saleId);
        return saleMapper.toDto(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByDateRange(SalesDateRangeDTO dateRange) {
        log.debug("Fetching sales between {} and {} for store ID: {}", dateRange.getStartDate(), dateRange.getEndDate(), TenantContext.getCurrentStoreId());
        List<Sale> sales = saleRepository.findBySaleTimestampBetweenAndStoreId(dateRange.getStartDate(), dateRange.getEndDate().withHour(23).withMinute(59).withSecond(59).withNano(999999999), TenantContext.getCurrentStoreId());
        return saleMapper.toDtoList(sales);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesForProduct(UUID productId) {
        log.debug("Fetching sales for product ID: {} in store ID: {}", productId, TenantContext.getCurrentStoreId());
        findProductTemplateOrThrow(productId);
        List<SaleItem> saleItems = saleItemRepository.findByProductTemplateIdAndStoreId(productId, TenantContext.getCurrentStoreId());
        List<Sale> sales = saleItems.stream()
                .map(SaleItem::getSale)
                .distinct()
                .collect(Collectors.toList());
        return saleMapper.toDtoList(sales);
    }

    // --- Discount Management ---

    @Override
    @Transactional
    public DiscountDTO createDiscount(CreateDiscountDTO createDTO) {
        log.info("Creating new discount: {} for organization ID: {}", createDTO.getName(), TenantContext.getCurrentOrganizationId());
        if (discountRepository.findByNameAndOrganizationId(createDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Discount with name '" + createDTO.getName() + "' already exists in organization.");
        }
        ProductTemplate product = null;
        if (createDTO.getProductTemplateId() != null) {
            product = findProductTemplateOrThrow(createDTO.getProductTemplateId());
        }
        Category category = null;
        if (createDTO.getCategoryId() != null) {
            category = findCategoryOrThrow(createDTO.getCategoryId());
        }

        Discount newDiscount = discountMapper.toEntity(createDTO);
        newDiscount.setOrganization(TenantContext.getCurrentOrganization());
        newDiscount.setProductTemplate(product);
        newDiscount.setCategory(category);
        newDiscount.setType(DiscountType.valueOf(String.valueOf(createDTO.getType())));

        Discount savedDiscount = discountRepository.save(newDiscount);
        logAuditEntry("CREATE_DISCOUNT", savedDiscount.getId(), "Created discount: " + savedDiscount.getName());
        log.info("Discount created with ID: {}", savedDiscount.getId());
        return discountMapper.toDto(savedDiscount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountDTO> getAllDiscounts(boolean includeInactive) {
        log.debug("Fetching all discounts for organization ID: {}, includeInactive: {}", TenantContext.getCurrentOrganizationId(), includeInactive);
        List<Discount> discounts = discountRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        if (!includeInactive) {
            discounts = discounts.stream().filter(Discount::isActive).collect(Collectors.toList());
        }
        return discountMapper.toDtoList(discounts);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountDTO getDiscountById(UUID discountId) {
        log.debug("Fetching discount with ID: {} for organization ID: {}", discountId, TenantContext.getCurrentOrganizationId());
        Discount discount = findDiscountOrThrow(discountId);
        return discountMapper.toDto(discount);
    }

    @Override
    @Transactional
    public DiscountDTO updateDiscount(UUID discountId, UpdateDiscountDTO updateDTO) {
        log.info("Updating discount with ID: {} for organization ID: {}", discountId, TenantContext.getCurrentOrganizationId());
        Discount existingDiscount = findDiscountOrThrow(discountId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingDiscount.getName())) {
            if (discountRepository.findByNameAndOrganizationId(updateDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("Discount name '" + updateDTO.getName() + "' is already in use in organization.");
            }
        }

        discountMapper.updateDiscountFromDto(updateDTO, existingDiscount);
        if (updateDTO.getProductTemplateId() != null) existingDiscount.setProductTemplate(findProductTemplateOrThrow(updateDTO.getProductTemplateId()));
        if (updateDTO.getCategoryId() != null) existingDiscount.setCategory(findCategoryOrThrow(updateDTO.getCategoryId()));
        if (updateDTO.getType() != null) existingDiscount.setType(DiscountType.valueOf(String.valueOf(updateDTO.getType())));

        Discount updatedDiscount = discountRepository.save(existingDiscount);
        logAuditEntry("UPDATE_DISCOUNT", updatedDiscount.getId(), "Updated discount: " + updatedDiscount.getName());
        log.info("Discount updated with ID: {}", updatedDiscount.getId());
        return discountMapper.toDto(updatedDiscount);
    }

    @Override
    @Transactional
    public void deactivateDiscount(UUID discountId) {
        log.warn("Deactivating discount with ID: {} for organization ID: {}", discountId, TenantContext.getCurrentOrganizationId());
        Discount discount = findDiscountOrThrow(discountId);
        if (!discount.isActive()) {
            log.info("Discount ID {} is already inactive.", discountId);
            return;
        }
        discount.setActive(false);
        discountRepository.save(discount);
        logAuditEntry("DEACTIVATE_DISCOUNT", discountId, "Deactivated discount: " + discount.getName());
        log.info("Discount ID {} deactivated.", discountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountDTO> getActiveDiscountsForProduct(UUID productId) {
        log.debug("Fetching active discounts for product ID: {} in organization ID: {}", productId, TenantContext.getCurrentOrganizationId());
        findProductTemplateOrThrow(productId);
        LocalDate today = LocalDate.now();
        List<Discount> discounts = discountRepository.findByProductTemplateIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(productId, today, today);
        return discountMapper.toDtoList(discounts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountDTO> getActiveDiscountsForCategory(UUID categoryId) {
        log.debug("Fetching active discounts for category ID: {} in organization ID: {}", categoryId, TenantContext.getCurrentOrganizationId());
        findCategoryOrThrow(categoryId);
        LocalDate today = LocalDate.now();
        List<Discount> discounts = discountRepository.findByCategoryIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(categoryId, today, today);
        return discountMapper.toDtoList(discounts);
    }

    // --- Damage/Loss Management ---

    @Override
    @Transactional(readOnly = true)
    public List<DamageLossDTO> getAllDamageLossRecords(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching all damage/loss records between {} and {} for store ID: {}", startDate, endDate, TenantContext.getCurrentStoreId());
        List<DamageLoss> records = damageLossRepository.findByDateRecordedBetweenAndStoreId(startDate, endDate, TenantContext.getCurrentStoreId());
        return damageLossMapper.toDtoList(records);
    }

    @Override
    @Transactional(readOnly = true)
    public DamageLossDTO getDamageLossRecordById(UUID damageLossId) {
        log.debug("Fetching damage/loss record with ID: {} for store ID: {}", damageLossId, TenantContext.getCurrentStoreId());
        DamageLoss record = findDamageLossOrThrow(damageLossId);
        return damageLossMapper.toDto(record);
    }

    // --- Unit of Measure Management ---

    @Override
    @Transactional
    public UnitOfMeasureDTO createUnitOfMeasure(CreateUnitOfMeasureDTO createDTO) {
        log.info("Creating new Unit of Measure: {} for organization ID: {}", createDTO.getName(), TenantContext.getCurrentOrganizationId());
        if (unitOfMeasureRepository.findByNameAndOrganizationId(createDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Unit of Measure with name '" + createDTO.getName() + "' already exists in organization.");
        }
        if (unitOfMeasureRepository.findByCodeAndOrganizationId(createDTO.getCode(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Unit of Measure with abbreviation '" + createDTO.getCode() + "' already exists in organization.");
        }
        UnitOfMeasure newUoM = unitOfMeasureMapper.toEntity(createDTO);
        newUoM.setOrganization(TenantContext.getCurrentOrganization());
        UnitOfMeasure savedUoM = unitOfMeasureRepository.save(newUoM);
        logAuditEntry("CREATE_UNIT_OF_MEASURE", savedUoM.getId(), "Created unit of measure: " + savedUoM.getName());
        log.info("Unit of Measure created with ID: {}", savedUoM.getId());
        return unitOfMeasureMapper.toDto(savedUoM);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureDTO> getAllUnitOfMeasures() {
        log.debug("Fetching all Units of Measure for organization ID: {}", TenantContext.getCurrentOrganizationId());
        List<UnitOfMeasure> uoms = unitOfMeasureRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        return unitOfMeasureMapper.toDtoList(uoms);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureDTO getUnitOfMeasureById(UUID uomId) {
        log.debug("Fetching Unit of Measure with ID: {} for organization ID: {}", uomId, TenantContext.getCurrentOrganizationId());
        UnitOfMeasure uom = findUnitOfMeasureOrThrow(uomId);
        return unitOfMeasureMapper.toDto(uom);
    }

    @Override
    @Transactional
    public UnitOfMeasureDTO updateUnitOfMeasure(UUID uomId, UpdateUnitOfMeasureDTO updateDTO) {
        log.info("Updating Unit of Measure with ID: {} for organization ID: {}", uomId, TenantContext.getCurrentOrganizationId());
        UnitOfMeasure existingUoM = findUnitOfMeasureOrThrow(uomId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingUoM.getName())) {
            if (unitOfMeasureRepository.findByNameAndOrganizationId(updateDTO.getName(), TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("Unit of Measure name '" + updateDTO.getName() + "' is already in use in organization.");
            }
        }
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(existingUoM.getCode())) {
            if (unitOfMeasureRepository.findByCodeAndOrganizationId(updateDTO.getCode(), TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("Unit of Measure abbreviation '" + updateDTO.getCode() + "' is already in use in organization.");
            }
        }

        unitOfMeasureMapper.updateUnitOfMeasureFromDto(updateDTO, existingUoM);
        UnitOfMeasure updatedUoM = unitOfMeasureRepository.save(existingUoM);
        logAuditEntry("UPDATE_UNIT_OF_MEASURE", updatedUoM.getId(), "Updated unit of measure: " + updatedUoM.getName());
        log.info("Unit of Measure updated with ID: {}", updatedUoM.getId());
        return unitOfMeasureMapper.toDto(updatedUoM);
    }

    @Override
    @Transactional
    public void deleteUnitOfMeasure(UUID uomId) {
        log.warn("Attempting to delete Unit of Measure with ID: {} for organization ID: {}", uomId, TenantContext.getCurrentOrganizationId());
        UnitOfMeasure uom = findUnitOfMeasureOrThrow(uomId);

        if (!productTemplateRepository.findByUnitOfMeasureIdAndOrganizationId(uomId, TenantContext.getCurrentOrganizationId()).isEmpty()) {
            throw new InvalidOperationException("Cannot delete Unit of Measure ID " + uomId + " as products are associated with it.");
        }

        unitOfMeasureRepository.delete(uom);
        logAuditEntry("DELETE_UNIT_OF_MEASURE", uomId, "Deleted unit of measure: " + uom.getName());
        log.info("Unit of Measure with ID {} deleted successfully.", uomId);
    }


    private void logAuditEntry(String action, UUID entityId, String message) {
        try {
            System.out.println("Audit entry logged successfully: " + log);
            auditLogService.builder()
                    .action(action)
//                    .entityType("Store")
                    .entityId(entityId)
                    .message(message)
                    .log();
        } catch (Exception e) {
            throw new RuntimeException("Failed to log audit entry", e);
        }
    }
}