package com.store.mgmt.inventory.model.enums;

/**
 * Enum representing the type of stock transaction.
 * All quantity changes must go through StockTransaction records.
 */
public enum TransactionType {
    RECEIPT,           // PO receive
    SALE,              // POS/e-commerce
    TRANSFER_IN,       // Stock transfer in
    TRANSFER_OUT,      // Stock transfer out
    ADJUSTMENT,        // Manual count
    DAMAGE_LOSS,       // Broken/spoiled
    CYCLE_COUNT,       // Physical count
    RETURN_TO_SUPPLIER,
    CUSTOMER_RETURN
}