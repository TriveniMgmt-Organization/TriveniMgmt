package com.store.mgmt.modules.inventory.application.dto;

import java.util.UUID;

/**
 * DTO for stock availability check responses.
 */
public record StockAvailabilityResponseDTO(
        UUID variantId,
        String variantSku,
        String variantName,
        int requestedQuantity,
        int availableQuantity,
        boolean isAvailable,
        int shortfall
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID variantId;
        private String variantSku;
        private String variantName;
        private int requestedQuantity;
        private int availableQuantity;
        private boolean isAvailable;
        private int shortfall;

        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder requestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; return this; }
        public Builder availableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public Builder isAvailable(boolean isAvailable) { this.isAvailable = isAvailable; return this; }
        public Builder shortfall(int shortfall) { this.shortfall = shortfall; return this; }

        public StockAvailabilityResponseDTO build() {
            return new StockAvailabilityResponseDTO(
                    variantId, variantSku, variantName,
                    requestedQuantity, availableQuantity, isAvailable, shortfall
            );
        }
    }
}
