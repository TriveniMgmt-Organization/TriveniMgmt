
package com.store.mgmt.inventory.model.dto;

import com.store.mgmt.inventory.model.entity.Sale;
import com.store.mgmt.inventory.model.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(name = "CreateSale", description = "Data Transfer Object for a product category")
public class CreateSaleDTO {
    @Schema( description = "Unique identifier for the sale", required = true)
    private String saleId;

    @Schema( description = "Identifier for the product being sold", required = true)
    private String productTemplateId;

    @Schema(description = "Quantity of the product sold", required = true, minimum = "1", maximum = "1000000000")
    private Integer quantity;

    @Schema(description = "Price at which the product was sold", required = true, minimum = "0", maximum = "1000000000", example = "19.99")
    private double price;

    @Schema( description = "Date and time of the sale", required = true)
    private String saleDateTime;

    @Schema(
            description = "Identifier for the customer making the purchase",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true
    )
    private UUID customerId;

    @Schema(
            description = "Unique identifier of the Product of id this product belongs to",
            example = "fedcba98-7654-3210-fedc-ba9876543210",
            required = true
    )
    private UUID userId;

    @Schema( description = "Date and time of the sale", required = true)
    private PaymentMethod paymentMethod;

    @Schema(description = "List of sale items associated with this sale", required = true)
    private List<CreateSaleItemDTO> items;
}