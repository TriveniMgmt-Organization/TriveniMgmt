package com.store.mgmt.pos.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionDTO {
    private UUID id;
    private UUID productId;
    private Integer quantity;
    private BigDecimal total;
    private BigDecimal tax;
    private String paymentMethod;
    private LocalDateTime timestamp;
    private UUID userId;
}
