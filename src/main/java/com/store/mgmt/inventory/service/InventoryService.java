package com.store.mgmt.inventory.service;

import com.store.mgmt.inventory.model.dto.*;
import com.store.mgmt.inventory.model.entity.PurchaseOrder;
import com.store.mgmt.inventory.model.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InventoryService {

    List<CategoryDTO> getAllProductCategories(boolean includeInactive);
    // --- Product Management ---
    ProductDTO createProduct(CreateProductDTO createDTO);
    List<ProductDTO> getAllProducts(boolean includeInactive); // Added param for filtering
    ProductDTO getProductById(UUID productTemplateId);
    ProductDTO updateProduct(UUID productTemplateId, UpdateProductDTO updateDTO);
    void deleteProduct(UUID productTemplateId); // Logical delete (isActive = false)

    // --- Product Variant Management ---
    ProductVariantDTO createProductVariant(CreateProductVariantDTO createDTO);
    List<ProductVariantDTO> getVariantsByTemplate(UUID templateId);
    List<ProductVariantDTO> getAllVariants(boolean includeInactive);
    ProductVariantDTO getVariantById(UUID variantId);
    ProductVariantDTO updateVariant(UUID variantId, UpdateProductVariantDTO updateDTO);
    void deleteVariant(UUID variantId);

    // --- Inventory Item Management (Stock specific) ---
    InventoryItemDTO createInventoryItem(CreateInventoryItemDTO createDTO); // Creating inventory item (no direct quantity)
    InventoryItemDTO getInventoryItemById(UUID inventoryItemId);
    List<InventoryItemDTO> getAllInventoryItems(); // Get all inventory items for current store
    List<InventoryItemDTO> getInventoryItemsForVariant(UUID variantId);
    List<InventoryItemDTO> getInventoryItemsForTemplate(UUID templateId); // Via variants
    List<InventoryItemDTO> getInventoryItemsAtLocation(UUID locationId);
    void deleteInventoryItem(UUID inventoryItemId); // Remove a specific inventory item record

    // --- Stock Transaction Management (Immutable records) ---
    StockTransactionDTO createStockTransaction(CreateStockTransactionDTO createDTO);
    List<StockTransactionDTO> getTransactionsByInventoryItem(UUID inventoryItemId);
    StockTransactionDTO getTransactionById(UUID transactionId);
    List<StockTransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // --- Stock Level Management (Calculated from transactions) ---
    StockLevelDTO getStockLevel(UUID inventoryItemId);
    List<StockLevelDTO> getStockLevelsByVariant(UUID variantId);
    List<StockLevelDTO> getLowStockItems();

    // --- Batch/Lot Management ---
    BatchLotDTO createBatchLot(CreateBatchLotDTO createDTO);
    List<BatchLotDTO> getAllBatchLots();
    BatchLotDTO getBatchLotById(UUID batchLotId);
    List<BatchLotDTO> getExpiringBatchLots(LocalDate startDate, LocalDate endDate);

    // --- UoM Conversion Management ---
    UoMConversionDTO createUoMConversion(CreateUoMConversionDTO createDTO);
    List<UoMConversionDTO> getAllUoMConversions();
    UoMConversionDTO getUoMConversionById(UUID conversionId);
    List<UoMConversionDTO> getConversionsByUom(UUID uomId);
    UoMConversionDTO getConversion(UUID fromUomId, UUID toUomId);

    // --- Stock Operations (Higher-level) ---
    void processSale(CreateSaleDTO saleDTO); // Main method for sales, which reduces stock
    void processPurchaseOrderReceipt(UUID purchaseOrderId, List<PurchaseOrderItemDTO> receivedItems); // For receiving goods

    // --- Stock Check & Information ---
    int getTotalStockQuantity(UUID variantId); // For variant
    int getTotalStockQuantityForTemplate(UUID templateId); // Sum across all variants
    int getStockQuantityAtLocation(UUID variantId, UUID locationId);
    BigDecimal getVariantRetailPrice(UUID variantId);
    boolean checkStockAvailability(UUID variantId, int quantityNeeded);
    boolean checkStockAvailabilityAtLocation(UUID variantId, UUID locationId, int quantityNeeded);

    // --- Brand Management ---
    BrandDTO createBrand(CreateBrandDTO createDTO);
    List<BrandDTO> getAllBrands(boolean includeInactive);
    BrandDTO getBrandById(UUID brandId);
    BrandDTO updateBrand(UUID brandId, UpdateBrandDTO updateDTO);
    void deleteBrand(UUID brandId);

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
    InventoryLocationDTO createInventoryLocation(CreateInventoryLocationDTO createDTO);
    List<InventoryLocationDTO> getAllInventoryLocations();
    InventoryLocationDTO getInventoryLocationById(UUID inventoryLocationId);
    InventoryLocationDTO updateInventoryLocation(UUID inventoryLocationId, UpdateInventoryLocationDTO updateDTO);
    void deleteInventoryLocation(UUID inventoryLocationId);

    // --- Purchase Order Management ---
    PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderDTO createDTO);
    List<PurchaseOrderDTO> getAllPurchaseOrders(PurchaseOrderStatus statusFilter);
    PurchaseOrderDTO getPurchaseOrderById(UUID purchaseOrderId);
    PurchaseOrderDTO updatePurchaseOrder(UUID purchaseOrderId, UpdatePurchaseOrderDTO updateDTO);
    void cancelPurchaseOrder(UUID purchaseOrderId); // Changes status to cancelled

    // --- Sales History (read-only for Inventory) ---
    SaleDTO getSaleById(UUID saleId);
    List<SaleDTO> getSalesByDateRange(SalesDateRangeDTO dateRange);
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