package com.store.mgmt.inventory.service;

import com.store.mgmt.common.exception.ResourceNotFoundException;
import com.store.mgmt.inventory.exceptions.DuplicateResourceException;
import com.store.mgmt.inventory.exceptions.InsufficientStockException;
import com.store.mgmt.inventory.exceptions.InvalidOperationException;
import com.store.mgmt.inventory.mapper.*;
import com.store.mgmt.inventory.model.dto.*;
import com.store.mgmt.inventory.model.entity.*;
import com.store.mgmt.inventory.repository.*;
import com.store.mgmt.users.model.entity.User;
import com.store.mgmt.users.repository.UserRepository;
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
@Slf4j // Lombok for logging
public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final LocationRepository locationRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final DiscountRepository discountRepository;
    private final DamageLossRepository damageLossRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final UserRepository userRepository; // Assuming this is in your user module

    // Mappers - Now injected and used
    private final ProductMapper productMapper;
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
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
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
            UserRepository userRepository,
            // Inject all your Mappers here
            ProductMapper productMapper, CategoryMapper categoryMapper,
            InventoryItemMapper inventoryItemMapper, SaleMapper saleMapper,
            SaleItemMapper saleItemMapper, PurchaseOrderMapper purchaseOrderMapper,
            PurchaseOrderItemMapper purchaseOrderItemMapper, DiscountMapper discountMapper,
            DamageLossMapper damageLossMapper, SupplierMapper supplierMapper,
            LocationMapper locationMapper, UnitOfMeasureMapper unitOfMeasureMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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
        this.userRepository = userRepository;

        // Initialize Mappers
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
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

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
    }

    private Location findLocationOrThrow(UUID locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + locationId));
    }

    private Supplier findSupplierOrThrow(UUID supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + supplierId));
    }

    private InventoryItem findInventoryItemOrThrow(UUID inventoryItemId) {
        return inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + inventoryItemId));
    }

    private PurchaseOrder findPurchaseOrderOrThrow(UUID purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + purchaseOrderId));
    }

    private Sale findSaleOrThrow(UUID saleId) {
        return saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + saleId));
    }

    private Discount findDiscountOrThrow(UUID discountId) {
        return discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));
    }

    private UnitOfMeasure findUnitOfMeasureOrThrow(UUID uomId) {
        return unitOfMeasureRepository.findById(uomId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of Measure not found with ID: " + uomId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    // --- Product Management ---

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductDTO createDTO) {
        log.info("Creating new product with SKU: {}", createDTO.getSku());

        if (productRepository.findBySku(createDTO.getSku()).isPresent()) {
            throw new DuplicateResourceException("Product with SKU '" + createDTO.getSku() + "' already exists.");
        }
        if (createDTO.getBarcode() != null && productRepository.findByBarcode(createDTO.getBarcode()).isPresent()) {
            throw new DuplicateResourceException("Product with Barcode '" + createDTO.getBarcode() + "' already exists.");
        }

        Category category = findCategoryOrThrow(createDTO.getCategoryId());
        UnitOfMeasure uom = findUnitOfMeasureOrThrow(createDTO.getUnitOfMeasureId());

        Product newProduct = productMapper.toEntity(createDTO);
        newProduct.setCategory(category); // Set entity reference
        newProduct.setUnitOfMeasure(uom); // Set entity reference

        Product savedProduct = productRepository.save(newProduct);
        log.info("Product created with ID: {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts(boolean includeInactive) {
        log.debug("Fetching all products, includeInactive: {}", includeInactive);
        List<Product> products = includeInactive ? productRepository.findAll() : productRepository.findAll().stream().filter(Product::isActive).collect(Collectors.toList());
        return productMapper.toDtoList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID productId) {
        log.debug("Fetching product with ID: {}", productId);
        Product product = findProductOrThrow(productId);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(UUID productId, UpdateProductDTO updateDTO) {
        log.info("Updating product with ID: {}", productId);
        Product existingProduct = findProductOrThrow(productId);

        if (updateDTO.getSku() != null && !updateDTO.getSku().equals(existingProduct.getSku())) {
            if (productRepository.findBySku(updateDTO.getSku()).isPresent()) {
                throw new DuplicateResourceException("SKU '" + updateDTO.getSku() + "' is already in use by another product.");
            }
        }
        if (updateDTO.getBarcode() != null && !updateDTO.getBarcode().equals(existingProduct.getBarcode())) {
            if (productRepository.findByBarcode(updateDTO.getBarcode()).isPresent()) {
                throw new DuplicateResourceException("Barcode '" + updateDTO.getBarcode() + "' is already in use by another product.");
            }
        }

        // Use MapStruct to update the existing entity
        productMapper.updateProductFromDto(updateDTO, existingProduct);

        // Handle relationships if their IDs are updated
        if (updateDTO.getCategoryId() != null) existingProduct.setCategory(findCategoryOrThrow(updateDTO.getCategoryId()));
        if (updateDTO.getUnitOfMeasureId() != null) existingProduct.setUnitOfMeasure(findUnitOfMeasureOrThrow(updateDTO.getUnitOfMeasureId()));

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated with ID: {}", updatedProduct.getId());
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId) {
        log.warn("Attempting to logically delete product with ID: {}", productId);
        Product product = findProductOrThrow(productId);
        product.setActive(false); // Logical delete
        productRepository.save(product);
        log.info("Product with ID {} logically deleted (set to inactive).", productId);
    }

    // --- Inventory Item Management (Stock specific) ---

    @Override
    @Transactional
    public InventoryItemDTO createInventoryItem(CreateInventoryItemDTO createDTO) {
        log.info("Creating new inventory item for product ID: {}", createDTO.getProductId());
        Product product = findProductOrThrow(createDTO.getProductId());
        Location location = findLocationOrThrow(createDTO.getLocationId());

        // Check for duplicate inventory item based on unique constraint
        Optional<InventoryItem> existingItem = inventoryItemRepository.findByProductIdAndLocationIdAndBatchNumberAndExpirationDate(
                product.getId(), location.getId(), createDTO.getBatchNumber(), createDTO.getExpirationDate());

        if (existingItem.isPresent()) {
            // If exists, update its quantity instead of creating a new one
            InventoryItem itemToUpdate = existingItem.get();
            itemToUpdate.setQuantity(itemToUpdate.getQuantity() + createDTO.getQuantity());
            itemToUpdate.setLastStockUpdate(LocalDateTime.now());
            // Optionally update status based on new quantity
            if (itemToUpdate.getQuantity() > 0) itemToUpdate.setStatus(InventoryItem.InventoryStatus.IN_STOCK);
            inventoryItemRepository.save(itemToUpdate);
            log.info("Updated existing inventory item ID {} with new quantity {}", itemToUpdate.getId(), itemToUpdate.getQuantity());
            return inventoryItemMapper.toDto(itemToUpdate);
        } else {
            InventoryItem newInventoryItem = inventoryItemMapper.toEntity(createDTO);
            newInventoryItem.setProduct(product);
            newInventoryItem.setLocation(location);
            newInventoryItem.setLastStockUpdate(LocalDateTime.now()); // Ensure this is set
            newInventoryItem.setStatus(createDTO.getQuantity() > 0 ? InventoryItem.InventoryStatus.IN_STOCK : InventoryItem.InventoryStatus.OUT_OF_STOCK);

            InventoryItem savedItem = inventoryItemRepository.save(newInventoryItem);
            log.info("New inventory item created with ID: {}", savedItem.getId());
            return inventoryItemMapper.toDto(savedItem);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemDTO getInventoryItemById(UUID inventoryItemId) {
        log.debug("Fetching inventory item with ID: {}", inventoryItemId);
        InventoryItem item = findInventoryItemOrThrow(inventoryItemId);
        return inventoryItemMapper.toDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getInventoryItemsForProduct(UUID productId) {
        log.debug("Fetching all inventory items for product ID: {}", productId);
        findProductOrThrow(productId); // Ensure product exists
        List<InventoryItem> items = inventoryItemRepository.findByProductId(productId);
        return inventoryItemMapper.toDtoList(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getInventoryItemsAtLocation(UUID locationId) {
        log.debug("Fetching all inventory items at location ID: {}", locationId);
        findLocationOrThrow(locationId); // Ensure location exists
        List<InventoryItem> items = inventoryItemRepository.findByLocationId(locationId);
        return inventoryItemMapper.toDtoList(items);
    }


    @Override
    @Transactional
    public InventoryItemDTO updateInventoryItemQuantity(UUID inventoryItemId, Integer quantityChange) {
        log.info("Updating quantity for inventory item ID {}: change {}", inventoryItemId, quantityChange);

        // Optimistic locking: findById with @Lock will ensure version check
        InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found with ID: " + inventoryItemId));

        int newQuantity = item.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new InsufficientStockException("Inventory item ID " + inventoryItemId + " stock cannot be negative. Current: " + item.getQuantity() + ", Change: " + quantityChange);
        }

        item.setQuantity(newQuantity);
        item.setLastStockUpdate(LocalDateTime.now());
        if (newQuantity == 0) {
            item.setStatus(InventoryItem.InventoryStatus.OUT_OF_STOCK);
        } else if (item.getProduct().getReorderPoint() != null && newQuantity < item.getProduct().getReorderPoint()) {
            item.setStatus(InventoryItem.InventoryStatus.LOW_STOCK);
        } else {
            item.setStatus(InventoryItem.InventoryStatus.IN_STOCK);
        }

        InventoryItem updatedItem = inventoryItemRepository.save(item);
        log.info("Inventory item ID {} quantity updated to {}", updatedItem.getId(), updatedItem.getQuantity());
        return inventoryItemMapper.toDto(updatedItem);
    }

    @Override
    @Transactional
    public void deleteInventoryItem(UUID inventoryItemId) {
        log.warn("Deleting inventory item with ID: {}", inventoryItemId);
        InventoryItem item = findInventoryItemOrThrow(inventoryItemId);
        inventoryItemRepository.delete(item);
        log.info("Inventory item with ID {} deleted.", inventoryItemId);
    }

    // --- Stock Operations (Higher-level) ---

    @Override
    @Transactional
    public void processSale(CreateSaleDTO saleDTO) {
        log.info("Processing new sale for user ID: {}", saleDTO.getUserId());

        User user = null;
        if (saleDTO.getUserId() != null) {
            user = findUserOrThrow(saleDTO.getUserId());
        }

        Sale newSale = saleMapper.toEntity(saleDTO);
        newSale.setSaleTimestamp(LocalDateTime.now());
        newSale.setPaymentMethod(Sale.PaymentMethod.valueOf(saleDTO.getPaymentMethod().toUpperCase())); // Assuming enum string
        newSale.setUser(user);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        Set<SaleItem> saleItems = saleDTO.getItems().stream().map(itemDTO -> {
            Product product = findProductOrThrow(itemDTO.getProductId());

            // --- Advanced Inventory Item Selection Logic ---
            // For a production system, this would involve complex logic:
            // 1. Prioritize items by expiration date (FEFO - First Expired, First Out)
            // 2. Consider FIFO (First In, First Out) for non-expiring goods.
            // 3. Consider location (e.g., POS implies current store location).
            // 4. Handle partial fulfillments from multiple inventory items.
            // A dedicated "InventoryAllocationService" or "StockService" might be extracted for this.

            // Simplified logic: find items for the product that are IN_STOCK and have enough quantity
            List<InventoryItem> availableInventoryItems = inventoryItemRepository.findByProductId(product.getId()).stream()
                    .filter(item -> item.getStatus() == InventoryItem.InventoryStatus.IN_STOCK && item.getQuantity() > 0)
                    // Sort by expiration date for FEFO, then by creation date for FIFO (if no exp date)
                    .sorted((item1, item2) -> {
                        if (item1.getExpirationDate() != null && item2.getExpirationDate() != null) {
                            return item1.getExpirationDate().compareTo(item2.getExpirationDate());
                        }
                        if (item1.getExpirationDate() != null) return -1; // Expiring first
                        if (item2.getExpirationDate() != null) return 1;
                        return item1.getCreatedAt().compareTo(item2.getCreatedAt()); // FIFO by creation
                    })
                    .collect(Collectors.toList());

            int quantityToSell = itemDTO.getQuantity();
            if (getTotalStockQuantity(product.getId()) < quantityToSell) {
                throw new InsufficientStockException("Insufficient total stock for product " + product.getName() + ". Requested: " + quantityToSell + ", Available: " + getTotalStockQuantity(product.getId()));
            }

            // This loop attempts to decrement from multiple inventory items if one is not enough
            int remainingToSell = quantityToSell;
            for (InventoryItem inventoryItemToUpdate : availableInventoryItems) {
                if (remainingToSell <= 0) break; // All quantity fulfilled

                int quantityTaken = Math.min(remainingToSell, inventoryItemToUpdate.getQuantity());
                if (quantityTaken > 0) {
                    updateInventoryItemQuantity(inventoryItemToUpdate.getId(), -quantityTaken); // Decrement stock
                    remainingToSell -= quantityTaken;
                    log.debug("Decremented {} units from inventory item ID {} for product {}", quantityTaken, inventoryItemToUpdate.getId(), product.getName());
                }
            }

            if (remainingToSell > 0) {
                // This should ideally not happen if getTotalStockQuantity check passed, but good for robustness
                throw new InsufficientStockException("Failed to fully allocate stock for product " + product.getName() + ". Remaining: " + remainingToSell);
            }

            SaleItem saleItem = saleItemMapper.toEntity(itemDTO);
            saleItem.setSale(newSale); // Link to the new sale entity
            saleItem.setProduct(product);
            saleItem.setUnitPrice(itemDTO.getUnitPrice()); // Price at time of sale
            saleItem.setDiscountAmount(itemDTO.getDiscountAmount());

            totalAmount = totalAmount.add(saleItem.getUnitPrice().multiply(BigDecimal.valueOf(saleItem.getQuantity())));
            totalDiscountAmount = totalDiscountAmount.add(saleItem.getDiscountAmount());
            return saleItem;
        }).collect(Collectors.toSet());

        newSale.setTotalAmount(totalAmount.subtract(totalDiscountAmount));
        newSale.setTotalDiscountAmount(totalDiscountAmount);
        newSale.setSaleItems(saleItems); // Link items to sale

        Sale savedSale = saleRepository.save(newSale);
        saleItems.forEach(saleItem -> saleItem.setSale(savedSale)); // Ensure Sale reference is set for child entities
        saleItemRepository.saveAll(saleItems);

        log.info("Sale with ID {} processed successfully.", savedSale.getId());
    }

    @Override
    @Transactional
    public void processPurchaseOrderReceipt(UUID purchaseOrderId, List<PurchaseOrderItemDTO> receivedItems) {
        log.info("Processing receipt for Purchase Order ID: {}", purchaseOrderId);
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(purchaseOrderId);

        if (purchaseOrder.getStatus() == PurchaseOrder.PurchaseOrderStatus.CANCELLED || purchaseOrder.getStatus() == PurchaseOrder.PurchaseOrderStatus.RECEIVED_COMPLETE) {
            throw new InvalidOperationException("Cannot receive items for a cancelled or already completed purchase order.");
        }

        boolean allItemsReceived = true;

        for (PurchaseOrderItemDTO receivedItemDTO : receivedItems) {
            PurchaseOrderItem orderItem = purchaseOrder.getPurchaseOrderItems().stream()
                    .filter(item -> item.getProduct().getId().equals(receivedItemDTO.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidOperationException("Product ID " + receivedItemDTO.getProductId() + " not found in Purchase Order " + purchaseOrderId));

            if (receivedItemDTO.getQuantity() < 0) {
                throw new InvalidOperationException("Received quantity cannot be negative for product ID: " + receivedItemDTO.getProductId());
            }

            int newReceivedQuantity = orderItem.getReceivedQuantity() + receivedItemDTO.getQuantity();
            if (newReceivedQuantity > orderItem.getOrderedQuantity()) {
                throw new InvalidOperationException("Received quantity for product " + orderItem.getProduct().getName() + " exceeds ordered quantity in PO " + purchaseOrderId);
            }

            orderItem.setReceivedQuantity(newReceivedQuantity);
            purchaseOrderItemRepository.save(orderItem); // Save updated order item

            // Update or create InventoryItem
            Product product = orderItem.getProduct();
            Location receiptLocation = findLocationOrThrow(receivedItemDTO.getLocationId()); // Assuming a location is specified for receipt

            // Try to find an existing inventory item for this product, location, batch, and expiration
            Optional<InventoryItem> existingInventoryItem = inventoryItemRepository
                    .findByProductIdAndLocationIdAndBatchNumberAndExpirationDate(
                            product.getId(), receiptLocation.getId(), receivedItemDTO.getBatchNumber(), receivedItemDTO.getExpirationDate());

            if (existingInventoryItem.isPresent()) {
                InventoryItem item = existingInventoryItem.get();
                item.setQuantity(item.getQuantity() + receivedItemDTO.getQuantity());
                item.setLastStockUpdate(LocalDateTime.now());
                item.setStatus(InventoryItem.InventoryStatus.IN_STOCK); // Ensure status is updated
                inventoryItemRepository.save(item);
                log.debug("Updated existing inventory item ID {} with new received quantity {}. New total: {}", item.getId(), receivedItemDTO.getQuantity(), item.getQuantity());
            } else {
                InventoryItem newInventoryItem = inventoryItemMapper.toEntity(new CreateInventoryItemDTO(
                        product.getId(),
                        receiptLocation.getId(),
                        receivedItemDTO.getQuantity(),
                        receivedItemDTO.getExpirationDate(),
                        receivedItemDTO.getBatchNumber(),
                        receivedItemDTO.getLotNumber() // Assuming you might have a lot number in PurchaseOrderItemDTO
                ));
                newInventoryItem.setProduct(product);
                newInventoryItem.setLocation(receiptLocation);
                newInventoryItem.setLastStockUpdate(LocalDateTime.now());
                newInventoryItem.setStatus(InventoryItem.InventoryStatus.IN_STOCK);
                inventoryItemRepository.save(newInventoryItem);
                log.debug("Created new inventory item ID {} for received product {}.", newInventoryItem.getId(), product.getName());
            }

            if (orderItem.getReceivedQuantity() < orderItem.getOrderedQuantity()) {
                allItemsReceived = false;
            }
        }

        // Update purchase order status
        if (allItemsReceived) {
            purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.RECEIVED_COMPLETE);
            purchaseOrder.setActualDeliveryDate(LocalDate.now());
            log.info("Purchase Order ID {} marked as RECEIVED_COMPLETE.", purchaseOrderId);
        } else {
            purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.RECEIVED_PARTIAL);
            log.info("Purchase Order ID {} marked as RECEIVED_PARTIAL.", purchaseOrderId);
        }
        purchaseOrderRepository.save(purchaseOrder);
    }

    @Override
    @Transactional
    public DamageLossDTO recordDamageLoss(CreateDamageLossDTO createDTO) {
        log.info("Recording damage/loss for product ID: {}", createDTO.getProductId());
        Product product = findProductOrThrow(createDTO.getProductId());
        Location location = findLocationOrThrow(createDTO.getLocationId());
        User user = findUserOrThrow(createDTO.getUserId());

        if (createDTO.getQuantity() <= 0) {
            throw new InvalidOperationException("Quantity for damage/loss must be positive.");
        }

        // --- Advanced Inventory Item Selection for Loss ---
        // Similar to sales, this needs careful selection of *which* specific inventory items are lost/damaged.
        // For simplicity, we find one at the location and attempt to decrement it.
        // In a real system, you might specify the batch/expiration for losses or apply a FEFO strategy.
        Optional<InventoryItem> itemToDecrementOpt = inventoryItemRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .stream()
                .filter(item -> item.getQuantity() >= createDTO.getQuantity() && item.getQuantity() > 0)
                .findFirst(); // Find any suitable item for now

        if (itemToDecrementOpt.isEmpty()) {
            throw new InsufficientStockException("Insufficient specific stock at location " + location.getName() + " for product " + product.getName() + " to record loss.");
        }

        InventoryItem itemToDecrement = itemToDecrementOpt.get();
        updateInventoryItemQuantity(itemToDecrement.getId(), -createDTO.getQuantity()); // Decrement stock

        DamageLoss damageLoss = damageLossMapper.toEntity(createDTO);
        damageLoss.setProduct(product);
        damageLoss.setLocation(location);
        damageLoss.setUser(user);

        DamageLoss savedDamageLoss = damageLossRepository.save(damageLoss);
        log.info("Recorded damage/loss ID {} for product ID {}", savedDamageLoss.getId(), product.getId());
        return damageLossMapper.toDto(savedDamageLoss);
    }

    // --- Stock Check & Information ---

    @Override
    @Transactional(readOnly = true)
    public int getTotalStockQuantity(UUID productId) {
        log.debug("Getting total stock quantity for product ID: {}", productId);
        findProductOrThrow(productId);
        return inventoryItemRepository.getTotalQuantityByProductId(productId).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public int getStockQuantityAtLocation(UUID productId, UUID locationId) {
        log.debug("Getting stock quantity for product ID {} at location ID {}", productId, locationId);
        findProductOrThrow(productId);
        findLocationOrThrow(locationId);
        return inventoryItemRepository.getTotalQuantityByProductIdAndLocationId(productId, locationId).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getProductRetailPrice(UUID productId) {
        log.debug("Getting retail price for product ID: {}", productId);
        Product product = findProductOrThrow(productId);
        return product.getRetailPrice();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailability(UUID productId, int quantityNeeded) {
        log.debug("Checking stock availability for product ID {}: needed {}", productId, quantityNeeded);
        if (quantityNeeded <= 0) {
            throw new IllegalArgumentException("Quantity needed must be positive.");
        }
        int available = getTotalStockQuantity(productId);
        return available >= quantityNeeded;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStockAvailabilityAtLocation(UUID productId, UUID locationId, int quantityNeeded) {
        log.debug("Checking stock availability for product ID {} at location ID {}: needed {}", productId, locationId, quantityNeeded);
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
        log.info("Creating new category: {}", createDTO.getName());
        if (categoryRepository.findByName(createDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Category with name '" + createDTO.getName() + "' already exists.");
        }
        Category newCategory = categoryMapper.toEntity(createDTO);
        Category savedCategory = categoryRepository.save(newCategory);
        log.info("Category created with ID: {}", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories.");
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDtoList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(UUID categoryId) {
        log.debug("Fetching category with ID: {}", categoryId);
        Category category = findCategoryOrThrow(categoryId);
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(UUID categoryId, UpdateCategoryDTO updateDTO) {
        log.info("Updating category with ID: {}", categoryId);
        Category existingCategory = findCategoryOrThrow(categoryId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingCategory.getName())) {
            if (categoryRepository.findByName(updateDTO.getName()).isPresent()) {
                throw new DuplicateResourceException("Category name '" + updateDTO.getName() + "' is already in use by another category.");
            }
        }

        categoryMapper.updateCategoryFromDto(updateDTO, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Category updated with ID: {}", updatedCategory.getId());
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        log.warn("Attempting to delete category with ID: {}", categoryId);
        Category category = findCategoryOrThrow(categoryId);

        // Check if any products are associated with this category
        if (!productRepository.findByCategoryId(categoryId).isEmpty()) { // Requires adding findByCategoryId to ProductRepository
            throw new InvalidOperationException("Cannot delete category ID " + categoryId + " as products are associated with it.");
        }

        categoryRepository.delete(category);
        log.info("Category with ID {} deleted successfully.", categoryId);
    }

    // --- Supplier Management ---

    @Override
    @Transactional
    public SupplierDTO createSupplier(CreateSupplierDTO createDTO) {
        log.info("Creating new supplier: {}", createDTO.getName());
        if (supplierRepository.findByName(createDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Supplier with name '" + createDTO.getName() + "' already exists.");
        }
        Supplier newSupplier = supplierMapper.toEntity(createDTO);
        Supplier savedSupplier = supplierRepository.save(newSupplier);
        log.info("Supplier created with ID: {}", savedSupplier.getId());
        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> getAllSuppliers() {
        log.debug("Fetching all suppliers.");
        List<Supplier> suppliers = supplierRepository.findAll();
        return supplierMapper.toDtoList(suppliers);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(UUID supplierId) {
        log.debug("Fetching supplier with ID: {}", supplierId);
        Supplier supplier = findSupplierOrThrow(supplierId);
        return supplierMapper.toDto(supplier);
    }

    @Override
    @Transactional
    public SupplierDTO updateSupplier(UUID supplierId, UpdateSupplierDTO updateDTO) {
        log.info("Updating supplier with ID: {}", supplierId);
        Supplier existingSupplier = findSupplierOrThrow(supplierId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingSupplier.getName())) {
            if (supplierRepository.findByName(updateDTO.getName()).isPresent()) {
                throw new DuplicateResourceException("Supplier name '" + updateDTO.getName() + "' is already in use by another supplier.");
            }
        }

        supplierMapper.updateSupplierFromDto(updateDTO, existingSupplier);
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        log.info("Supplier updated with ID: {}", updatedSupplier.getId());
        return supplierMapper.toDto(updatedSupplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(UUID supplierId) {
        log.warn("Attempting to delete supplier with ID: {}", supplierId);
        Supplier supplier = findSupplierOrThrow(supplierId);

        // Check if any purchase orders are associated with this supplier
        if (!purchaseOrderRepository.findBySupplierId(supplierId).isEmpty()) {
            throw new InvalidOperationException("Cannot delete supplier ID " + supplierId + " as purchase orders are associated with it.");
        }

        supplierRepository.delete(supplier);
        log.info("Supplier with ID {} deleted successfully.", supplierId);
    }

    // --- Location Management ---

    @Override
    @Transactional
    public LocationDTO createLocation(CreateLocationDTO createDTO) {
        log.info("Creating new location: {}", createDTO.getName());
        if (locationRepository.findByName(createDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Location with name '" + createDTO.getName() + "' already exists.");
        }
        Location newLocation = locationMapper.toEntity(createDTO);
        newLocation.setType(Location.LocationType.valueOf(createDTO.getType().toUpperCase())); // Ensure enum conversion
        Location savedLocation = locationRepository.save(newLocation);
        log.info("Location created with ID: {}", savedLocation.getId());
        return locationMapper.toDto(savedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDTO> getAllLocations() {
        log.debug("Fetching all locations.");
        List<Location> locations = locationRepository.findAll();
        return locationMapper.toDtoList(locations);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDTO getLocationById(UUID locationId) {
        log.debug("Fetching location with ID: {}", locationId);
        Location location = findLocationOrThrow(locationId);
        return locationMapper.toDto(location);
    }

    @Override
    @Transactional
    public LocationDTO updateLocation(UUID locationId, UpdateLocationDTO updateDTO) {
        log.info("Updating location with ID: {}", locationId);
        Location existingLocation = findLocationOrThrow(locationId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingLocation.getName())) {
            if (locationRepository.findByName(updateDTO.getName()).isPresent()) {
                throw new DuplicateResourceException("Location name '" + updateDTO.getName() + "' is already in use by another location.");
            }
        }

        locationMapper.updateLocationFromDto(updateDTO, existingLocation);
        if (updateDTO.getType() != null) existingLocation.setType(Location.LocationType.valueOf(updateDTO.getType().toUpperCase()));

        Location updatedLocation = locationRepository.save(existingLocation);
        log.info("Location updated with ID: {}", updatedLocation.getId());
        return locationMapper.toDto(updatedLocation);
    }

    @Override
    @Transactional
    public void deleteLocation(UUID locationId) {
        log.warn("Attempting to delete location with ID: {}", locationId);
        Location location = findLocationOrThrow(locationId);

        // Check if any inventory items or damage records are associated
        if (!inventoryItemRepository.findByLocationId(locationId).isEmpty()) {
            throw new InvalidOperationException("Cannot delete location ID " + locationId + " as inventory items are associated with it.");
        }
        if (!damageLossRepository.findByLocationId(locationId).isEmpty()) {
            throw new InvalidOperationException("Cannot delete location ID " + locationId + " as damage/loss records are associated with it.");
        }

        locationRepository.delete(location);
        log.info("Location with ID {} deleted successfully.", locationId);
    }

    // --- Purchase Order Management ---

    @Override
    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderDTO createDTO) {
        log.info("Creating new purchase order for supplier ID: {}", createDTO.getSupplierId());
        Supplier supplier = findSupplierOrThrow(createDTO.getSupplierId());
        User user = findUserOrThrow(createDTO.getUserId());

        PurchaseOrder newPO = purchaseOrderMapper.toEntity(createDTO);
        newPO.setSupplier(supplier);
        newPO.setOrderDate(LocalDateTime.now());
        newPO.setStatus(PurchaseOrder.PurchaseOrderStatus.PENDING);
        newPO.setUser(user);

        Set<PurchaseOrderItem> poItems = createDTO.getItems().stream().map(itemDTO -> {
            Product product = findProductOrThrow(itemDTO.getProductId());
            PurchaseOrderItem poItem = purchaseOrderItemMapper.toEntity(itemDTO);
            poItem.setPurchaseOrder(newPO); // Link to the new PO entity
            poItem.setProduct(product);
            return poItem;
        }).collect(Collectors.toSet());

        newPO.setPurchaseOrderItems(poItems);
        newPO.setTotalEstimatedAmount(poItems.stream()
                .map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getOrderedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        PurchaseOrder savedPO = purchaseOrderRepository.save(newPO);
        poItems.forEach(item -> item.setPurchaseOrder(savedPO)); // Ensure parent reference for child saves
        purchaseOrderItemRepository.saveAll(poItems); // Save child items after parent is saved

        log.info("Purchase Order created with ID: {}", savedPO.getId());
        return purchaseOrderMapper.toDto(savedPO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getAllPurchaseOrders(PurchaseOrder.PurchaseOrderStatus statusFilter) {
        log.debug("Fetching all purchase orders with status filter: {}", statusFilter);
        List<PurchaseOrder> purchaseOrders;
        if (statusFilter != null) {
            purchaseOrders = purchaseOrderRepository.findByStatus(statusFilter);
        } else {
            purchaseOrders = purchaseOrderRepository.findAll();
        }
        return purchaseOrderMapper.toDtoList(purchaseOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getPurchaseOrderById(UUID purchaseOrderId) {
        log.debug("Fetching purchase order with ID: {}", purchaseOrderId);
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(purchaseOrderId);
        return purchaseOrderMapper.toDto(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO updatePurchaseOrder(UUID purchaseOrderId, UpdatePurchaseOrderDTO updateDTO) {
        log.info("Updating purchase order with ID: {}", purchaseOrderId);
        PurchaseOrder existingPO = findPurchaseOrderOrThrow(purchaseOrderId);

        if (existingPO.getStatus() == PurchaseOrder.PurchaseOrderStatus.RECEIVED_COMPLETE || existingPO.getStatus() == PurchaseOrder.PurchaseOrderStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot update a completed or cancelled purchase order.");
        }

        purchaseOrderMapper.updatePurchaseOrderFromDto(updateDTO, existingPO);

        if (updateDTO.getSupplierId() != null) existingPO.setSupplier(findSupplierOrThrow(updateDTO.getSupplierId()));
        if (updateDTO.getUserId() != null) existingPO.setUser(findUserOrThrow(updateDTO.getUserId()));
        if (updateDTO.getStatus() != null) existingPO.setStatus(PurchaseOrder.PurchaseOrderStatus.valueOf(updateDTO.getStatus().toUpperCase()));

        // Logic for updating purchase order items: this would typically involve
        // comparing the DTO items with existing items, adding new ones, updating
        // existing ones, and removing missing ones. For brevity, it's not fully
        // implemented here, assuming simpler scenarios or separate endpoints for item updates.
        // If updateDTO.getItems() is provided, you'd handle that here.

        PurchaseOrder updatedPO = purchaseOrderRepository.save(existingPO);
        log.info("Purchase order updated with ID: {}", updatedPO.getId());
        return purchaseOrderMapper.toDto(updatedPO);
    }

    @Override
    @Transactional
    public void cancelPurchaseOrder(UUID purchaseOrderId) {
        log.warn("Attempting to cancel purchase order with ID: {}", purchaseOrderId);
        PurchaseOrder purchaseOrder = findPurchaseOrderOrThrow(purchaseOrderId);

        if (purchaseOrder.getStatus() == PurchaseOrder.PurchaseOrderStatus.RECEIVED_COMPLETE) {
            throw new InvalidOperationException("Cannot cancel a completed purchase order.");
        }
        if (purchaseOrder.getStatus() == PurchaseOrder.PurchaseOrderStatus.CANCELLED) {
            throw new InvalidOperationException("Purchase order is already cancelled.");
        }

        purchaseOrder.setStatus(PurchaseOrder.PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase Order with ID {} cancelled successfully.", purchaseOrderId);
    }

    // --- Sales History (read-only for Inventory) ---

    @Override
    @Transactional(readOnly = true)
    public SaleDTO getSaleById(UUID saleId) {
        log.debug("Fetching sale with ID: {}", saleId);
        Sale sale = findSaleOrThrow(saleId);
        return saleMapper.toDto(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching sales between {} and {}.", startDate, endDate);
        // Note: Sale.saleTimestamp is LocalDateTime, so adjust query for date range if needed
        // For production, you might need to convert LocalDate to LocalDateTime range (start of day to end of day)
        List<Sale> sales = saleRepository.findBySaleTimestampBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59, 999999999));
        return saleMapper.toDtoList(sales);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesForProduct(UUID productId) {
        log.debug("Fetching sales for product ID: {}", productId);
        findProductOrThrow(productId);
        List<SaleItem> saleItems = saleItemRepository.findByProductId(productId);
        // Map SaleItems to their parent Sales and then to DTOs, avoiding duplicates
        List<Sale> sales = saleItems.stream()
                .map(SaleItem::getSale)
                .distinct() // Get unique sales
                .collect(Collectors.toList());
        return saleMapper.toDtoList(sales);
    }

    // --- Discount Management ---

    @Override
    @Transactional
    public DiscountDTO createDiscount(CreateDiscountDTO createDTO) {
        log.info("Creating new discount: {}", createDTO.getName());
        if (discountRepository.findByName(createDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Discount with name '" + createDTO.getName() + "' already exists.");
        }
        Product product = null;
        if (createDTO.getProductId() != null) {
            product = findProductOrThrow(createDTO.getProductId());
        }
        Category category = null;
        if (createDTO.getCategoryId() != null) {
            category = findCategoryOrThrow(createDTO.getCategoryId());
        }

        Discount newDiscount = discountMapper.toEntity(createDTO);
        newDiscount.setProduct(product);
        newDiscount.setCategory(category);
        newDiscount.setType(Discount.DiscountType.valueOf(createDTO.getType().toUpperCase()));

        Discount savedDiscount = discountRepository.save(newDiscount);
        log.info("Discount created with ID: {}", savedDiscount.getId());
        return discountMapper.toDto(savedDiscount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountDTO> getAllDiscounts(boolean includeInactive) {
        log.debug("Fetching all discounts, includeInactive: {}", includeInactive);
        List<Discount> discounts = includeInactive ? discountRepository.findAll() :
                discountRepository.findAll().stream().filter(Discount::isActive).collect(Collectors.toList());
        return discountMapper.toDtoList(discounts);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountDTO getDiscountById(UUID discountId) {
        log.debug("Fetching discount with ID: {}", discountId);
        Discount discount = findDiscountOrThrow(discountId);
        return discountMapper.toDto(discount);
    }

    @Override
    @Transactional
    public DiscountDTO updateDiscount(UUID discountId, UpdateDiscountDTO updateDTO) {
        log.info("Updating discount with ID: {}", discountId);
        Discount existingDiscount = findDiscountOrThrow(discountId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingDiscount.getName())) {
            if (discountRepository.findByName(updateDTO.getName()).isPresent()) {
                throw new DuplicateResourceException("Discount name '" + updateDTO.getName() + "' is already in use by another discount.");
            }
        }

        discountMapper.updateDiscountFromDto(updateDTO, existingDiscount);
        if (updateDTO.getProductId() != null) existingDiscount.setProduct(findProductOrThrow(updateDTO.getProductId()));
        if (updateDTO.getCategoryId() != null) existingDiscount.setCategory(findCategoryOrThrow(updateDTO.getCategoryId()));
        if (updateDTO.getType() != null) existingDiscount.setType(Discount.DiscountType.valueOf(updateDTO.getType().toUpperCase()));


        Discount updatedDiscount = discountRepository.save(existingDiscount);
        log.info("Discount updated with ID: {}", updatedDiscount.getId());
        return discountMapper.toDto(updatedDiscount);
    }

    @Override
    @Transactional
    public void deactivateDiscount(UUID discountId) {
        log.warn("Deactivating discount with ID: {}", discountId);
        Discount discount = findDiscountOrThrow(discountId);
        if (!discount.isActive()) {
            log.info("Discount ID {} is already inactive.", discountId);
            return;
        }
        discount.setActive(false);
        discountRepository.save(discount);
        log.info("Discount ID {} deactivated.", discountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountDTO> getActiveDiscountsForProduct(UUID productId) {
        log.debug("Fetching active discounts for product ID: {}", productId);
        findProductOrThrow(productId);
        LocalDate today = LocalDate.now();
        List<Discount> discounts = discountRepository.findByProductIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(productId, today, today);
        return discountMapper.toDtoList(discounts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountDTO> getActiveDiscountsForCategory(UUID categoryId) {
        log.debug("Fetching active discounts for category ID: {}", categoryId);
        findCategoryOrThrow(categoryId);
        LocalDate today = LocalDate.now();
        List<Discount> discounts = discountRepository.findByCategoryIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(categoryId, today, today);
        return discountMapper.toDtoList(discounts);
    }

    // --- Damage/Loss Management ---

    @Override
    @Transactional
    public DamageLossDTO recordDamageLoss(CreateDamageLossDTO createDTO) {
        log.info("Recording damage/loss for product ID: {}", createDTO.getProductId());
        Product product = findProductOrThrow(createDTO.getProductId());
        Location location = findLocationOrThrow(createDTO.getLocationId());
        User user = findUserOrThrow(createDTO.getUserId());

        if (createDTO.getQuantity() <= 0) {
            throw new InvalidOperationException("Quantity for damage/loss must be positive.");
        }

        // Logic to select specific InventoryItem(s) to decrement for the loss.
        // Similar to sales, this needs careful selection of *which* specific inventory items are lost/damaged.
        // For simplicity, we find one at the location and attempt to decrement it.
        // In a real system, you might specify the batch/expiration for losses or apply a FEFO strategy.
        Optional<InventoryItem> itemToDecrementOpt = inventoryItemRepository.findByProductIdAndLocationId(product.getId(), location.getId())
                .stream()
                .filter(item -> item.getQuantity() >= createDTO.getQuantity() && item.getQuantity() > 0)
                .findFirst(); // Find any suitable item for now

        if (itemToDecrementOpt.isEmpty()) {
            throw new InsufficientStockException("Insufficient specific stock at location " + location.getName() + " for product " + product.getName() + " to record loss.");
        }

        InventoryItem itemToDecrement = itemToDecrementOpt.get();
        updateInventoryItemQuantity(itemToDecrement.getId(), -createDTO.getQuantity()); // Decrement stock

        DamageLoss damageLoss = damageLossMapper.toEntity(createDTO);
        damageLoss.setProduct(product);
        damageLoss.setLocation(location);
        damageLoss.setUser(user);
        damageLoss.setReason(DamageLoss.DamageLossReason.valueOf(createDTO.getReason().toUpperCase()));

        DamageLoss savedDamageLoss = damageLossRepository.save(damageLoss);
        log.info("Recorded damage/loss ID {} for product ID {}", savedDamageLoss.getId(), product.getId());
        return damageLossMapper.toDto(savedDamageLoss);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DamageLossDTO> getAllDamageLossRecords(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching all damage/loss records between {} and {}.", startDate, endDate);
        List<DamageLoss> records = damageLossRepository.findByDateRecordedBetween(startDate, endDate);
        return damageLossMapper.toDtoList(records);
    }

    @Override
    @Transactional(readOnly = true)
    public DamageLossDTO getDamageLossRecordById(UUID damageLossId) {
        log.debug("Fetching damage/loss record with ID: {}", damageLossId);
        DamageLoss record = findDamageLossOrThrow(damageLossId);
        return damageLossMapper.toDto(record);
    }

    // --- Unit of Measure Management ---

    @Override
    @Transactional
    public UnitOfMeasureDTO createUnitOfMeasure(CreateUnitOfMeasureDTO createDTO) {
        log.info("Creating new Unit of Measure: {}", createDTO.getName());
        if (unitOfMeasureRepository.findByName(createDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Unit of Measure with name '" + createDTO.getName() + "' already exists.");
        }
        if (unitOfMeasureRepository.findByCode(createDTO.getCode()).isPresent()) {
            throw new DuplicateResourceException("Unit of Measure with abbreviation '" + createDTO.getCode() + "' already exists.");
        }
        UnitOfMeasure newUoM = unitOfMeasureMapper.toEntity(createDTO);
        UnitOfMeasure savedUoM = unitOfMeasureRepository.save(newUoM);
        log.info("Unit of Measure created with ID: {}", savedUoM.getId());
        return unitOfMeasureMapper.toDto(savedUoM);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureDTO> getAllUnitOfMeasures() {
        log.debug("Fetching all Units of Measure.");
        List<UnitOfMeasure> uoms = unitOfMeasureRepository.findAll();
        return unitOfMeasureMapper.toDtoList(uoms);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureDTO getUnitOfMeasureById(UUID uomId) {
        log.debug("Fetching Unit of Measure with ID: {}", uomId);
        UnitOfMeasure uom = findUnitOfMeasureOrThrow(uomId);
        return unitOfMeasureMapper.toDto(uom);
    }

    @Override
    @Transactional
    public UnitOfMeasureDTO updateUnitOfMeasure(UUID uomId, UpdateUnitOfMeasureDTO updateDTO) {
        log.info("Updating Unit of Measure with ID: {}", uomId);
        UnitOfMeasure existingUoM = findUnitOfMeasureOrThrow(uomId);

        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingUoM.getName())) {
            if (unitOfMeasureRepository.findByName(updateDTO.getName()).isPresent()) {
                throw new DuplicateResourceException("Unit of Measure name '" + updateDTO.getName() + "' is already in use.");
            }
        }
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(existingUoM.getCode())) {
            if (unitOfMeasureRepository.findByCode(updateDTO.getCode()).isPresent()) {
                throw new DuplicateResourceException("Unit of Measure abbreviation '" + updateDTO.getCode() + "' is already in use.");
            }
        }

        unitOfMeasureMapper.updateUnitOfMeasureFromDto(updateDTO, existingUoM);
        UnitOfMeasure updatedUoM = unitOfMeasureRepository.save(existingUoM);
        log.info("Unit of Measure updated with ID: {}", updatedUoM.getId());
        return unitOfMeasureMapper.toDto(updatedUoM);
    }

    @Override
    @Transactional
    public void deleteUnitOfMeasure(UUID uomId) {
        log.warn("Attempting to delete Unit of Measure with ID: {}", uomId);
        UnitOfMeasure uom = findUnitOfMeasureOrThrow(uomId);

        // Check if any products are associated with this UoM
        if (!productRepository.findByUnitOfMeasureId(uomId).isEmpty()) { // Requires adding findByUnitOfMeasureId to ProductRepository
            throw new InvalidOperationException("Cannot delete Unit of Measure ID " + uomId + " as products are associated with it.");
        }

        unitOfMeasureRepository.delete(uom);
        log.info("Unit of Measure with ID {} deleted successfully.", uomId);
    }
}
