package com.store.mgmt.inventory.model.enums;

/**
 * Enum representing the type of stock transaction.
 * All quantity changes must go through StockTransaction records.
 */
public enum TransactionType {
    RECEIPT,        // Goods received from supplier
    SALE,           // Goods sold to customer
    ADJUSTMENT,     // Manual stock adjustment
    DAMAGE,         // Damaged goods
    TRANSFER_IN,    // Stock transferred into location
    TRANSFER_OUT,   // Stock transferred out of location
    COUNT           // Stock count correction
}

