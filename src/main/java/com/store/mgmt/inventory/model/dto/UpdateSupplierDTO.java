package com.store.mgmt.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UpdateSupplier", description = "Data Transfer Object for a product category")
public class UpdateSupplierDTO {

    private String name;
    private String address;
    private String contactNumber;
    private String email;
    private String website;
    private String notes;
}