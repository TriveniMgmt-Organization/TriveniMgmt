package com.store.mgmt.pos.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(name = "Transaction", description = "Data Transfer Object for a Point-of-Sale transaction")
public class TransactionDTO {

    @Schema(
            description = "Unique identifier of the transaction",
            example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    )
    private UUID id;

    @Schema(
            description = "Unique identifier of the product involved in the transaction",
            example = "09876543-21ab-cdef-1234-567890fedcba"
    )
    private UUID productId;

    @Schema(
            description = "Quantity of the product sold in this transaction",
            example = "2",
            minimum = "1"
    )
    private Integer quantity;

    @Schema(
            description = "Total amount of the transaction before tax",
            example = "150.00",
            minimum = "0"
    )
    private BigDecimal total;

    @Schema(
            description = "Amount of tax applied to the transaction",
            example = "15.00",
            minimum = "0"
    )
    private BigDecimal tax;

    @Schema(
            description = "Method of payment used for the transaction (e.g., 'Credit Card', 'Cash', 'UPI')",
            example = "Credit Card"
    )
    private String paymentMethod;

    @Schema(
            description = "Timestamp when the transaction occurred (ISO 8601 format)",
            example = "2025-07-13T22:09:18.123456"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "Unique identifier of the user who performed the transaction",
            example = "fedcba98-7654-3210-fedc-ba9876543210"
    )
    private UUID userId;
}