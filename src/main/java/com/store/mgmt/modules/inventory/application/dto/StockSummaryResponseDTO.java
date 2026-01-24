package com.store.mgmt.modules.inventory.application.dto;

import java.util.UUID;

/**
 * DTO for stock summary responses.
 */
public record StockSummaryResponseDTO(
        UUID variantId,
        String variantSku,
        String variantName,
        UUID templateId,
        String templateName,
        int totalOnHand,
        int totalCommitted,
        int totalAvailable,
        int locationCount
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID variantId;
        private String variantSku;
        private String variantName;
        private UUID templateId;
        private String templateName;
        private int totalOnHand;
        private int totalCommitted;
        private int totalAvailable;
        private int locationCount;

        public Builder variantId(UUID variantId) { this.variantId = variantId; return this; }
        public Builder variantSku(String variantSku) { this.variantSku = variantSku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder templateId(UUID templateId) { this.templateId = templateId; return this; }
        public Builder templateName(String templateName) { this.templateName = templateName; return this; }
        public Builder totalOnHand(int totalOnHand) { this.totalOnHand = totalOnHand; return this; }
        public Builder totalCommitted(int totalCommitted) { this.totalCommitted = totalCommitted; return this; }
        public Builder totalAvailable(int totalAvailable) { this.totalAvailable = totalAvailable; return this; }
        public Builder locationCount(int locationCount) { this.locationCount = locationCount; return this; }

        public StockSummaryResponseDTO build() {
            return new StockSummaryResponseDTO(
                    variantId, variantSku, variantName,
                    templateId, templateName,
                    totalOnHand, totalCommitted, totalAvailable, locationCount
            );
        }
    }
}
