package com.store.mgmt.modules.inventory.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for supplier responses.
 */
public record SupplierResponseDTO(
        UUID id,
        UUID organizationId,
        String name,
        String contactPerson,
        String email,
        String phone,
        String address,
        String accountNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID organizationId;
        private String name;
        private String contactPerson;
        private String email;
        private String phone;
        private String address;
        private String accountNumber;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder organizationId(UUID organizationId) { this.organizationId = organizationId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder contactPerson(String contactPerson) { this.contactPerson = contactPerson; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public SupplierResponseDTO build() {
            return new SupplierResponseDTO(id, organizationId, name, contactPerson, email, phone, address, accountNumber, createdAt, updatedAt);
        }
    }
}
