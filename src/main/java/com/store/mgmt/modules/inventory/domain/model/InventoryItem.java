package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.modules.inventory.domain.event.*;
import com.store.mgmt.modules.inventory.domain.exception.InsufficientStockException;
import com.store.mgmt.modules.inventory.domain.exception.InvalidQuantityException;
import com.store.mgmt.shared.domain.model.AggregateRoot;
import com.store.mgmt.shared.infrastructure.security.TenantContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root for inventory items.
 * An inventory item represents a specific product variant at a specific location.
 */
public class InventoryItem extends AggregateRoot<InventoryItemId> {

    private final InventoryItemId id;
    private final ProductVariantId variantId;
    private final LocationId locationId;
    private final StoreId storeId;

    private StockLevel stockLevel;
    private String batchNumber;
    private LocalDate expiryDate;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // Private constructor - use factory methods
    private InventoryItem(
            InventoryItemId id,
            ProductVariantId variantId,
            LocationId locationId,
            StoreId storeId,
            StockLevel stockLevel,
            UserId createdBy
    ) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.variantId = Objects.requireNonNull(variantId, "Variant ID cannot be null");
        this.locationId = Objects.requireNonNull(locationId, "Location ID cannot be null");
        this.storeId = Objects.requireNonNull(storeId, "Store ID cannot be null");
        this.stockLevel = Objects.requireNonNull(stockLevel, "Stock level cannot be null");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        registerEvent(new InventoryItemCreatedEvent(
                id, variantId, locationId, storeId, stockLevel.onHand(), createdBy
        ));
    }

    /**
     * Create a new inventory item.
     */
    public static InventoryItem create(
            ProductVariantId variantId,
            LocationId locationId,
            StoreId storeId,
            int initialQuantity,
            int lowStockThreshold,
            UserId createdBy
    ) {
        // Validate tenant context
        TenantContext tenant = TenantContext.current();
        tenant.requireStore(storeId.getValue());

        // Validate quantities
        if (initialQuantity < 0) {
            throw new InvalidQuantityException("Initial quantity cannot be negative");
        }
        if (lowStockThreshold < 0) {
            throw new InvalidQuantityException("Low stock threshold cannot be negative");
        }

        StockLevel stockLevel = StockLevel.initial(initialQuantity, lowStockThreshold);

        return new InventoryItem(
                InventoryItemId.generate(),
                variantId,
                locationId,
                storeId,
                stockLevel,
                createdBy
        );
    }

    /**
     * Reconstitute an inventory item from persistence.
     * Does not raise events.
     */
    public static InventoryItem reconstitute(
            InventoryItemId id,
            ProductVariantId variantId,
            LocationId locationId,
            StoreId storeId,
            StockLevel stockLevel,
            String batchNumber,
            LocalDate expiryDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        InventoryItem item = new InventoryItem(id, variantId, locationId, storeId, stockLevel, null);
        item.clearDomainEvents(); // Remove creation event for reconstituted entities
        item.batchNumber = batchNumber;
        item.expiryDate = expiryDate;
        return item;
    }

    // ==================== Domain Behaviors ====================

    /**
     * Receive stock into inventory.
     */
    public void receiveStock(int quantity, String reason, UserId receivedBy) {
        validateNotDeleted();
        validateQuantityPositive(quantity, "receive");

        int previousOnHand = stockLevel.onHand();
        stockLevel = stockLevel.receive(quantity);
        updatedAt = LocalDateTime.now();

        registerEvent(new StockReceivedEvent(
                id, quantity, previousOnHand, stockLevel.onHand(), reason, receivedBy
        ));
    }

    /**
     * Issue stock from inventory (sales, transfers out, etc.).
     */
    public void issueStock(int quantity, String reason, UserId issuedBy) {
        validateNotDeleted();
        validateQuantityPositive(quantity, "issue");

        if (!stockLevel.hasAvailable(quantity)) {
            throw new InsufficientStockException(id, quantity, stockLevel.available());
        }

        int previousOnHand = stockLevel.onHand();
        boolean wasAboveReorderPoint = !stockLevel.isLow();

        stockLevel = stockLevel.issue(quantity);
        updatedAt = LocalDateTime.now();

        registerEvent(new StockIssuedEvent(
                id, quantity, previousOnHand, stockLevel.onHand(), reason, issuedBy
        ));

        // Check if we crossed the reorder point
        if (wasAboveReorderPoint && stockLevel.isLow()) {
            registerEvent(new LowStockAlertEvent(
                    id, storeId, stockLevel.onHand(), stockLevel.reorderPoint()
            ));
        }
    }

    /**
     * Adjust stock level (corrections, cycle counts, etc.).
     */
    public void adjustStock(int adjustment, String reason, UserId adjustedBy) {
        validateNotDeleted();

        if (adjustment == 0) {
            throw new InvalidQuantityException("Adjustment cannot be zero");
        }

        int newOnHand = stockLevel.onHand() + adjustment;
        if (newOnHand < 0) {
            throw new InvalidQuantityException("Adjustment would result in negative stock");
        }

        int previousOnHand = stockLevel.onHand();
        boolean wasAboveReorderPoint = !stockLevel.isLow();

        stockLevel = stockLevel.adjust(adjustment);
        updatedAt = LocalDateTime.now();

        registerEvent(new StockAdjustedEvent(
                id, adjustment, previousOnHand, stockLevel.onHand(), reason, adjustedBy
        ));

        // Check if we crossed the reorder point
        if (wasAboveReorderPoint && stockLevel.isLow()) {
            registerEvent(new LowStockAlertEvent(
                    id, storeId, stockLevel.onHand(), stockLevel.reorderPoint()
            ));
        }
    }

    /**
     * Reserve stock for a pending order.
     */
    public void reserveStock(int quantity) {
        validateNotDeleted();
        validateQuantityPositive(quantity, "reserve");

        if (!stockLevel.hasAvailable(quantity)) {
            throw new InsufficientStockException(id, quantity, stockLevel.available());
        }

        stockLevel = stockLevel.reserve(quantity);
        updatedAt = LocalDateTime.now();
    }

    /**
     * Release a reservation.
     */
    public void releaseReservation(int quantity) {
        validateNotDeleted();
        validateQuantityPositive(quantity, "release");

        stockLevel = stockLevel.releaseReservation(quantity);
        updatedAt = LocalDateTime.now();
    }

    /**
     * Update batch information.
     */
    public void updateBatchInfo(String batchNumber, LocalDate expiryDate) {
        validateNotDeleted();
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update the reorder point.
     */
    public void updateReorderPoint(int newReorderPoint) {
        validateNotDeleted();
        if (newReorderPoint < 0) {
            throw new InvalidQuantityException("Reorder point cannot be negative");
        }
        stockLevel = stockLevel.withReorderPoint(newReorderPoint);
        updatedAt = LocalDateTime.now();
    }

    /**
     * Soft delete this item.
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = this.deletedAt;
    }

    // ==================== Queries ====================

    public boolean isLowStock() {
        return stockLevel.isLow();
    }

    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now().plusDays(daysThreshold));
    }

    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // ==================== Validation Helpers ====================

    private void validateNotDeleted() {
        if (isDeleted()) {
            throw new IllegalStateException("Cannot modify a deleted inventory item");
        }
    }

    private void validateQuantityPositive(int quantity, String operation) {
        if (quantity <= 0) {
            throw new InvalidQuantityException(
                    String.format("%s quantity must be positive, got %d", operation, quantity)
            );
        }
    }

    // ==================== Getters ====================

    @Override
    public InventoryItemId getId() {
        return id;
    }

    public ProductVariantId getVariantId() {
        return variantId;
    }

    public LocationId getLocationId() {
        return locationId;
    }

    public StoreId getStoreId() {
        return storeId;
    }

    public StockLevel getStockLevel() {
        return stockLevel;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
