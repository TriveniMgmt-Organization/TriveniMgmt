package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CreateSupplier", description = "Data Transfer Object for a product category")
public class CreateSupplierDTO {
    @Schema(description = "Name of the supplier", required = true)
    private String name;

    @Schema(description = "Description of the supplier")
    private String description;

    @Schema(description = "Contact number of the supplier")
    private String contactNumber;

    @Schema(description = "Email address of the supplier")
    private String email;

    @Schema(description = "Address of the supplier")
    private String address;
}