package com.store.mgmt.inventory.service;

import com.store.mgmt.inventory.model.dto.*;
import com.store.mgmt.inventory.model.entity.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InventoryService {

    List<CategoryDTO> getAllProductCategories(boolean includeInactive);
    // --- Product Management ---
    ProductDTO createProduct(CreateProductDTO createDTO);
    List<ProductDTO> getAllProducts(boolean includeInactive); // Added param for filtering
    ProductDTO getProductById(UUID productId);
    ProductDTO updateProduct(UUID productId, UpdateProductDTO updateDTO);
    void deleteProduct(UUID productId); // Logical delete (isActive = false)

    // --- Inventory Item Management (Stock specific) ---
    InventoryItemDTO createInventoryItem(CreateInventoryItemDTO createDTO); // Adding new stock
    InventoryItemDTO getInventoryItemById(UUID inventoryItemId);
    List<InventoryItemDTO> getInventoryItemsForProduct(UUID productId);
    List<InventoryItemDTO> getInventoryItemsAtLocation(UUID locationId);
    InventoryItemDTO updateInventoryItemQuantity(UUID inventoryItemId, Integer quantityChange); // Adjust specific item stock
    void deleteInventoryItem(UUID inventoryItemId); // Remove a specific inventory item record

    // --- Stock Operations (Higher-level) ---
    void processSale(CreateSaleDTO saleDTO); // Main method for sales, which reduces stock
    void processPurchaseOrderReceipt(UUID purchaseOrderId, List<PurchaseOrderItemDTO> receivedItems); // For receiving goods

    // --- Stock Check & Information ---
    int getTotalStockQuantity(UUID productId);
    int getStockQuantityAtLocation(UUID productId, UUID locationId);
    BigDecimal getProductRetailPrice(UUID productId);
    boolean checkStockAvailability(UUID roductId, int quantityNeeded);
    boolean checkStockAvailabilityAtLocation(UUID productId, UUID locationId, int quantityNeeded);


    // --- Category Management ---
    CategoryDTO createCategory(CreateCategoryDTO createDTO);
    List<CategoryDTO> getAllCategories();
    CategoryDTO getCategoryById(UUID categoryId);
    CategoryDTO updateCategory(UUID categoryId, UpdateCategoryDTO updateDTO);
    void deleteCategory(UUID categoryId);

    // --- Supplier Management ---
    SupplierDTO createSupplier(CreateSupplierDTO createDTO);
    List<SupplierDTO> getAllSuppliers();
    SupplierDTO getSupplierById(UUID supplierId);
    SupplierDTO updateSupplier(UUID supplierId, UpdateSupplierDTO updateDTO);
    void deleteSupplier(UUID supplierId);

    // --- Location Management ---
    LocationDTO createLocation(CreateLocationDTO createDTO);
    List<LocationDTO> getAllLocations();
    LocationDTO getLocationById(UUID locationId);
    LocationDTO updateLocation(UUID locationId, UpdateLocationDTO updateDTO);
    void deleteLocation(UUID locationId);

    // --- Purchase Order Management ---
    PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderDTO createDTO);
    List<PurchaseOrderDTO> getAllPurchaseOrders(PurchaseOrder.PurchaseOrderStatus statusFilter);
    PurchaseOrderDTO getPurchaseOrderById(UUID purchaseOrderId);
    PurchaseOrderDTO updatePurchaseOrder(UUID purchaseOrderId, UpdatePurchaseOrderDTO updateDTO);
    void cancelPurchaseOrder(UUID purchaseOrderId); // Changes status to cancelled

    // --- Sales History (read-only for Inventory) ---
    SaleDTO getSaleById(UUID saleId);
    List<SaleDTO> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<SaleDTO> getSalesForProduct(UUID productId);

    // --- Discount Management ---
    DiscountDTO createDiscount(CreateDiscountDTO createDTO);
    List<DiscountDTO> getAllDiscounts(boolean includeInactive);
    DiscountDTO getDiscountById(UUID discountId);
    DiscountDTO updateDiscount(UUID discountId, UpdateDiscountDTO updateDTO);
    void deactivateDiscount(UUID discountId);
    List<DiscountDTO> getActiveDiscountsForProduct(UUID productId);
    List<DiscountDTO> getActiveDiscountsForCategory(UUID categoryId);

    // --- Damage/Loss Management ---
    DamageLossDTO recordDamageLoss(CreateDamageLossDTO createDTO);
    List<DamageLossDTO> getAllDamageLossRecords(LocalDateTime startDate, LocalDateTime endDate);
    DamageLossDTO getDamageLossRecordById(UUID damageLossId);

    // --- Unit of Measure Management ---
    UnitOfMeasureDTO createUnitOfMeasure(CreateUnitOfMeasureDTO createDTO);
    List<UnitOfMeasureDTO> getAllUnitOfMeasures();
    UnitOfMeasureDTO getUnitOfMeasureById(UUID uomId);
    UnitOfMeasureDTO updateUnitOfMeasure(UUID uomId, UpdateUnitOfMeasureDTO updateDTO);
    void deleteUnitOfMeasure(UUID uomId);
}