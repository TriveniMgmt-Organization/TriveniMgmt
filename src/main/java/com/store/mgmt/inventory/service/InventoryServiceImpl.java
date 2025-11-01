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
    private final LocationRepository locationRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final DiscountRepository discountRepository;
    private final DamageLossRepository damageLossRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final AuditLogService auditLogService;

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
    private final LocationMapper locationMapper;
    private final UnitOfMeasureMapper unitOfMeasureMapper;

    public InventoryServiceImpl(
            ProductTemplateRepository productTemplateRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            SupplierRepository supplierRepository,
            InventoryItemRepository inventoryItemRepository,
            LocationRepository locationRepository,
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
            LocationMapper locationMapper,
            UnitOfMeasureMapper unitOfMeasureMapper
    ) {
        this.productTemplateRepository = productTemplateRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.supplierRepository = supplierRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.locationRepository = locationRepository;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.discountRepository = discountRepository;
        this.damageLossRepository = damageLossRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.userOrganizationRoleRepository = userOrganizationRoleRepository;
        this.auditLogService = auditLogService;
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
        this.locationMapper = locationMapper;
        this.unitOfMeasureMapper = unitOfMeasureMapper;
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

    private Location findLocationOrThrow(UUID locationId) {
        return locationRepository.findByIdAndStoreId(locationId, TenantContext.getCurrentStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + locationId));
    }

    private Supplier findSupplierOrThrow(UUID supplierId) {
        return supplierRepository.findByIdAndOrganizationId(supplierId, TenantContext.getCurrentOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + supplierId));
    }

    private InventoryItem findInventoryItemOrThrow(UUID inventoryItemId) {
        return inventoryItemRepository.findByIdAndStoreId(inventoryItemId, TenantContext.getCurrentStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + inventoryItemId));
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
        log.info("Creating new product with SKU: {} for organization ID: {}", createDTO.getSku(), TenantContext.getCurrentOrganizationId());

        if (productTemplateRepository.findBySkuAndOrganizationId(createDTO.getSku(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Product with SKU '" + createDTO.getSku() + "' already exists in organization.");
        }
        if (createDTO.getBarcode() != null && productTemplateRepository.findByBarcodeAndOrganizationId(createDTO.getBarcode(), TenantContext.getCurrentOrganizationId()).isPresent()) {
            throw new DuplicateResourceException("Product with Barcode '" + createDTO.getBarcode() + "' already exists in organization.");
        }

        Category category = findCategoryOrThrow(createDTO.getCategoryId());
        UnitOfMeasure uom = findUnitOfMeasureOrThrow(createDTO.getUnitOfMeasureId());

        ProductTemplate newProduct = productTemplateMapper.toEntity(createDTO);
        newProduct.setOrganization(TenantContext.getCurrentOrganization());
        newProduct.setCategory(category);
        newProduct.setUnitOfMeasure(uom);

        ProductTemplate savedProduct = productTemplateRepository.save(newProduct);
        logAuditEntry("CREATE_PRODUCT", savedProduct.getId(), "Created product: " + savedProduct.getName());
        log.info("Product created with ID: {}", savedProduct.getId());
        return productTemplateMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts(boolean includeInactive) {
        log.debug("Fetching all products for organization ID: {}, includeInactive: {}", TenantContext.getCurrentOrganizationId(), includeInactive);
        List<ProductTemplate> products = productTemplateRepository.findByOrganizationId(TenantContext.getCurrentOrganizationId());
        if (!includeInactive) {
            products = products.stream().filter(ProductTemplate::isActive).collect(Collectors.toList());
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
        log.info("Updating product with ID: {} for organization ID: {}", productId, TenantContext.getCurrentOrganizationId());
        ProductTemplate existingProduct = findProductTemplateOrThrow(productId);

        if (updateDTO.getSku() != null && !updateDTO.getSku().equals(existingProduct.getSku())) {
            if (productTemplateRepository.findBySkuAndOrganizationId(updateDTO.getSku(), TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("SKU '" + updateDTO.getSku() + "' is already in use by another product in organization.");
            }
        }
        if (updateDTO.getBarcode() != null && !updateDTO.getBarcode().equals(existingProduct.getBarcode())) {
            if (productTemplateRepository.findByBarcodeAndOrganizationId(updateDTO.getBarcode(), TenantContext.getCurrentOrganizationId()).isPresent()) {
                throw new DuplicateResourceException("Barcode '" + updateDTO.getBarcode() + "' is already in use by another product in organization.");
            }
        }

        productTemplateMapper.updateProductFromDto(updateDTO, existingProduct);

        if (updateDTO.getCategoryId() != null) existingProduct.setCategory(findCategoryOrThrow(updateDTO.getCategoryId()));
        if (updateDTO.getUnitOfMeasureId() != null) existingProduct.setUnitOfMeasure(findUnitOfMeasureOrThrow(updateDTO.getUnitOfMeasureId()));

        ProductTemplate updatedProduct = productTemplateRepository.save(existingProduct);
        logAuditEntry("UPDATE_PRODUCT", updatedProduct.getId(), "Updated product: " + updatedProduct.getName());
        log.info("Product updated with ID: {}", updatedProduct.getId());
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
        log.info("Creating new inventory item for product ID: {} in store ID: {}", createDTO.getProductTemplateId(), TenantContext.getCurrentStoreId());
        ProductTemplate product = findProductTemplateOrThrow(createDTO.getProductTemplateId());
        Location location = findLocationOrThrow(createDTO.getLocationId());

        if (!location.getStore().getId().equals(TenantContext.getCurrentStoreId())) {
            throw new SecurityException("Location does not belong to current store.");
        }

        Optional<InventoryItem> existingItem = inventoryItemRepository.findByProductTemplateIdAndStoreIdAndBatchNumberAndExpirationDate(
                product.getId(), TenantContext.getCurrentStoreId(), createDTO.getBatchNumber(), createDTO.getExpirationDate());

        if (existingItem.isPresent()) {
            InventoryItem itemToUpdate = existingItem.get();
            itemToUpdate.setQuantity(itemToUpdate.getQuantity() + createDTO.getQuantity());
            itemToUpdate.setRetailPrice(createDTO.getRetailPrice());
            itemToUpdate.setCostPrice(createDTO.getCostPrice());
            itemToUpdate.setLastStockUpdate(LocalDateTime.now());
            inventoryItemRepository.save(itemToUpdate);
            logAuditEntry("UPDATE_INVENTORY_ITEM", itemToUpdate.getId(), "Updated inventory item quantity: " + itemToUpdate.getQuantity());
            log.info("Updated existing inventory item ID {} with new quantity {}", itemToUpdate.getId(), itemToUpdate.getQuantity());
            return inventoryItemMapper.toDto(itemToUpdate);
        } else {
            InventoryItem newInventoryItem = inventoryItemMapper.toEntity(createDTO);
            newInventoryItem.setProductTemplate(product);
            newInventoryItem.setStore(TenantContext.getCurrentStore());
            newInventoryItem.setLocation(location);
            newInventoryItem.setLastStockUpdate(LocalDateTime.now());

            InventoryItem savedItem = inventoryItemRepository.save(newInventoryItem);
            logAuditEntry("CREATE_INVENTORY_ITEM", savedItem.getId(), "Created inventory item for product: " + product.getName());
            log.info("New inventory item created with ID: {}", savedItem.getId());
            return inventoryItemMapper.toDto(savedItem);
        }
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
    public List<InventoryItemDTO> getInventoryItemsForProduct(UUID productId) {
        log.debug("Fetching all inventory items for product ID: {} in store ID: {}", productId, TenantContext.getCurrentStoreId());
        findProductTemplateOrThrow(productId);
        List<InventoryItem> items = inventoryItemRepository.findByProductTemplateIdAndStoreId(productId, TenantContext.getCurrentStoreId());
        return inventoryItemMapper.toDtoList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getInventoryItemsAtLocation(UUID locationId) {
        log.debug("Fetching all inventory items at location ID: {} in store ID: {}", locationId, TenantContext.getCurrentStoreId());
        findLocationOrThrow(locationId);
        List<InventoryItem> items = inventoryItemRepository.findByLocationIdAndStoreId(locationId, TenantContext.getCurrentStoreId());
        return inventoryItemMapper.toDtoList(items);
    }

    @Override
    @Transactional
    public InventoryItemDTO updateInventoryItemQuantity(UUID inventoryItemId, Integer quantityChange) {
        log.info("Updating quantity for inventory item ID {}: change {}", inventoryItemId, TenantContext.getCurrentStoreId());
        InventoryItem item = findInventoryItemOrThrow(inventoryItemId);

        int newQuantity = item.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new InsufficientStockException("Inventory item ID " + inventoryItemId + " stock cannot be negative. Current: " + item.getQuantity() + ", Change: " + quantityChange);
        }

        item.setQuantity(newQuantity);
        item.setLastStockUpdate(LocalDateTime.now());
        InventoryItem updatedItem = inventoryItemRepository.save(item);
        logAuditEntry("UPDATE_INVENTORY_QUANTITY", updatedItem.getId(), "Updated inventory item quantity to: " + newQuantity);
        log.info("Inventory item ID {} quantity updated to {}", updatedItem.getId(), updatedItem.getQuantity());
        return inventoryItemMapper.toDto(updatedItem);
    }

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

    private List<InventoryItem> getAvailableInventoryItems(UUID productId) {
        return inventoryItemRepository.findByProductTemplateIdAndStoreId(productId, TenantContext.getCurrentStoreId()).stream()
                .filter(item -> item.getQuantity() > 0)
                .sorted((item1, item2) -> {
                    if (item1.getExpirationDate() != null && item2.getExpirationDate() != null) {
                        return item1.getExpirationDate().compareTo(item2.getExpirationDate());
                    }
                    if (item1.getExpirationDate() != null) return -1;
                    if (item2.getExpirationDate() != null) return 1;
                    return item1.getCreatedAt().compareTo(item2.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    private void allocateStock(List<InventoryItem> availableInventoryItems, int quantityToSell, String productName) {
        int remainingToSell = quantityToSell;
        for (InventoryItem inventoryItemToUpdate : availableInventoryItems) {
            if (remainingToSell <= 0) break;

            int quantityTaken = Math.min(remainingToSell, inventoryItemToUpdate.getQuantity());
            if (quantityTaken > 0) {
                updateInventoryItemQuantity(inventoryItemToUpdate.getId(), -quantityTaken);
                remainingToSell -= quantityTaken;
                log.debug("Decremented {} units from inventory item ID {} for product {}", quantityTaken, inventoryItemToUpdate.getId(), productName);
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

            Location receiptLocation = findLocationOrThrow(receivedItemDTO.getLocationId());

            Optional<InventoryItem> existingInventoryItem = inventoryItemRepository
                    .findByProductTemplateIdAndStoreIdAndBatchNumberAndExpirationDate(
                            orderItem.getProductTemplate().getId(), TenantContext.getCurrentStoreId(), receivedItemDTO.getBatchNumber(), receivedItemDTO.getExpirationDate());

            if (existingInventoryItem.isPresent()) {
                InventoryItem item = existingInventoryItem.get();
                item.setQuantity(item.getQuantity() + receivedItemDTO.getQuantity());
                item.setRetailPrice(receivedItemDTO.getRetailPrice());
                item.setCostPrice(receivedItemDTO.getCostPrice());
                item.setLastStockUpdate(LocalDateTime.now());
                inventoryItemRepository.save(item);
                logAuditEntry("UPDATE_INVENTORY_ITEM", item.getId(), "Updated inventory item with received quantity: " + receivedItemDTO.getQuantity());
                log.debug("Updated existing inventory item ID {} with new received quantity {}. New total: {}", item.getId(), receivedItemDTO.getQuantity(), item.getQuantity());
            } else {
                InventoryItem newInventoryItem = inventoryItemMapper.toEntity(new CreateInventoryItemDTO(
                        orderItem.getProductTemplate().getId(),
                        receiptLocation.getId(),
                        receivedItemDTO.getQuantity(),
                        receivedItemDTO.getCostPrice(),
                        receivedItemDTO.getRetailPrice(),
                        receivedItemDTO.getExpirationDate(),
                        receivedItemDTO.getBatchNumber(),
                        receivedItemDTO.getLotNumber()
                ));
                newInventoryItem.setProductTemplate(orderItem.getProductTemplate());
                newInventoryItem.setStore(TenantContext.getCurrentStore());
                newInventoryItem.setLocation(receiptLocation);
                newInventoryItem.setRetailPrice(receivedItemDTO.getRetailPrice());
                newInventoryItem.setCostPrice(receivedItemDTO.getCostPrice());
                newInventoryItem.setLastStockUpdate(LocalDateTime.now());
                inventoryItemRepository.save(newInventoryItem);
                logAuditEntry("CREATE_INVENTORY_ITEM", newInventoryItem.getId(), "Created inventory item for received product: " + orderItem.getProductTemplate().getName());
                log.debug("Created new inventory item ID {} for received product {}.", newInventoryItem.getId(), orderItem.getProductTemplate().getName());
            }

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
        Location location = findLocationOrThrow(createDTO.getLocationId());
        User user = findUserOrThrow(createDTO.getUserId());

        if (!location.getStore().getId().equals(TenantContext.getCurrentStoreId())) {
            throw new SecurityException("Location does not belong to current store.");
        }

        if (createDTO.getQuantity() <= 0) {
            throw new InvalidOperationException("Quantity for damage/loss must be positive.");
        }

        Optional<InventoryItem> itemToDecrementOpt = inventoryItemRepository
                .findByProductTemplateIdAndStoreIdAndLocationId(product.getId(), TenantContext.getCurrentStoreId(), location.getId())
                .stream()
                .filter(item -> item.getQuantity() >= createDTO.getQuantity() && item.getQuantity() > 0)
                .findFirst();

        if (itemToDecrementOpt.isEmpty()) {
            throw new InsufficientStockException("Insufficient specific stock at location " + location.getName() + " for product " + product.getName() + " to record loss.");
        }

        InventoryItem itemToDecrement = itemToDecrementOpt.get();
        updateInventoryItemQuantity(itemToDecrement.getId(), -createDTO.getQuantity());

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

    // --- Stock Check & Information ---

    @Override
    @Transactional(readOnly = true)
    public int getTotalStockQuantity(UUID productId) {
        log.debug("Getting total stock quantity for product ID: {} in store ID: {}", productId, TenantContext.getCurrentStoreId());
        findProductTemplateOrThrow(productId);
        return inventoryItemRepository.getTotalQuantityByProductTemplateIdAndStoreId(productId, TenantContext.getCurrentStoreId()).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public int getStockQuantityAtLocation(UUID productId, UUID locationId) {
        log.debug("Getting stock quantity for product ID {} at location ID {} in store ID: {}", productId, locationId, TenantContext.getCurrentStoreId());
        findProductTemplateOrThrow(productId);
        findLocationOrThrow(locationId);
        return inventoryItemRepository.getTotalQuantityByProductTemplateIdAndStoreIdAndLocationId(productId, TenantContext.getCurrentStoreId(), locationId).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getInventoryItemRetailPrice(UUID inventoryItemId) {
        log.debug("Getting retail price for inventory item ID: {} in store ID: {}", inventoryItemId, TenantContext.getCurrentStoreId());
        InventoryItem item = findInventoryItemOrThrow(inventoryItemId);
        return item.getRetailPrice();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(UUID productId, int quantityNeeded) {
        log.debug("Checking stock availability for product ID {}: needed {} in store ID: {}", productId, quantityNeeded, TenantContext.getCurrentStoreId());
        if (quantityNeeded <= 0) {
            throw new IllegalArgumentException("Quantity needed must be positive.");
        }
        int available = getTotalStockQuantity(productId);
        return available >= quantityNeeded;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailabilityAtLocation(UUID productId, UUID locationId, int quantityNeeded) {
        log.debug("Checking stock availability for product ID {} at location ID {}: needed {} in store ID: {}", productId, locationId, quantityNeeded, TenantContext.getCurrentStoreId());
        if (quantityNeeded <= 0) {
            throw new IllegalArgumentException("Quantity needed must be positive.");
        }
        int available = getStockQuantityAtLocation(productId, locationId);
        return available >= quantityNeeded;
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
    public LocationDTO createLocation(CreateLocationDTO createDTO) {
        log.info("Creating new location: {} for store ID: {}", createDTO.getName(), TenantContext.getCurrentStoreId());
        if (locationRepository.findByNameAndStoreId(createDTO.getName(), TenantContext.getCurrentStoreId()).isPresent()) {
            throw new DuplicateResourceException("Location with name '" + createDTO.getName() + "' already exists in store.");
        }
        Location newLocation = locationMapper.toEntity(createDTO);
        newLocation.setStore(TenantContext.getCurrentStore());
        newLocation.setType(LocationType.valueOf(String.valueOf(createDTO.getType())));
        Location savedLocation = locationRepository.save(newLocation);
        logAuditEntry("CREATE_LOCATION", savedLocation.getId(), "Created location: " + savedLocation.getName());
        log.info("Location created with ID: {}", savedLocation.getId());
        return locationMapper.toDto(savedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDTO> getAllLocations() {
        log.debug("Fetching all locations for store ID: {}", TenantContext.getCurrentStoreId());
        List<Location> locations = locationRepository.findByStoreId(TenantContext.getCurrentStoreId());
        return locationMapper.toDtoList(locations);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDTO getLocationById(UUID locationId) {
        log.debug("Fetching location with ID: {} for store ID: {}", locationId, TenantContext.getCurrentStoreId());
        Location location = findLocationOrThrow(locationId);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public LocationDTO updateLocation(UUID locationId, UpdateLocationDTO updateDTO) {
        log.info("Updating location with ID: {} for store ID: {}", locationId, TenantContext.getCurrentStoreId());
        Location existingLocation = findLocationOrThrow(locationId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingLocation.getName())) {
            if (locationRepository.findByNameAndStoreId(updateDTO.getName(), TenantContext.getCurrentStoreId()).isPresent()) {
                throw new DuplicateResourceException("Location name '" + updateDTO.getName() + "' is already in use in store.");
            }
        }

        locationMapper.updateLocationFromDto(updateDTO, existingLocation);
        if (updateDTO.getType() != null) existingLocation.setType(LocationType.valueOf(String.valueOf(updateDTO.getType())));

        Location updatedLocation = locationRepository.save(existingLocation);
        logAuditEntry("UPDATE_LOCATION", updatedLocation.getId(), "Updated location: " + updatedLocation.getName());
        log.info("Location updated with ID: {}", updatedLocation.getId());
        return locationMapper.toDto(updatedLocation);
    }

    @Override
    @Transactional
    public void deleteLocation(UUID locationId) {
        log.warn("Attempting to delete location with ID: {} for store ID: {}", locationId, TenantContext.getCurrentStoreId());
        Location location = findLocationOrThrow(locationId);

        if (!inventoryItemRepository.findByLocationIdAndStoreId(locationId, TenantContext.getCurrentStoreId()).isEmpty()) {
            throw new InvalidOperationException("Cannot delete location ID " + locationId + " as inventory items are associated with it.");
        }
        if (!damageLossRepository.findByLocationIdAndStoreId(locationId, TenantContext.getCurrentStoreId()).isEmpty()) {
            throw new InvalidOperationException("Cannot delete location ID " + locationId + " as damage/loss records are associated with it.");
        }

        locationRepository.delete(location);
        logAuditEntry("DELETE_LOCATION", locationId, "Deleted location: " + location.getName());
        log.info("Location with ID {} deleted successfully.", locationId);
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