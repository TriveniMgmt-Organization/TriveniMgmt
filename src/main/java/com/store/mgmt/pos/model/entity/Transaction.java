package com.store.mgmt.pos.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private BigDecimal tax;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}