package com.store.mgmt.inventory.model.dto;

import com.store.mgmt.inventory.model.entity.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CreateLocation", description = "Data Transfer Object for a product category")
public class CreateLocationDTO {
    @Schema(description = "Name of the location", required = true)
    private String name;

    @Schema(description = "Description of the location")
    private String description;

    @Schema(description = "Address of the location", required = true)
    private String address;

    @Schema(description = "Contact number for the location")
    private String contactNumber;

    @Schema(description = "Type of the location", required = true)
    private Location.LocationType type;
}
