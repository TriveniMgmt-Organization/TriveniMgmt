package com.store.mgmt.inventory.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface InventoryService {
    void checkStock(UUID productId, Integer quantity);
    void updateStock(UUID productId, Integer quantityChange);
    BigDecimal getProductPrice(UUID productId);
}
