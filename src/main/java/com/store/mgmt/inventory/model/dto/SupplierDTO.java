package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "Supplier", description = "Data Transfer Object for a product category")
public class SupplierDTO {
    @Schema(description = "Unique identifier for the supplier")
    private UUID id;

    @Schema(description = "Name of the supplier")
    private String name;

    @Schema( description = "Contact information for the supplier")
    private String contactInfo;

    @Schema(description = "Address of the supplier")
    private String address;

    @Schema(description = "Email of the supplier")
    private String email;

    @Schema( description = "Phone number of the supplier")
    private String phoneNumber;
}