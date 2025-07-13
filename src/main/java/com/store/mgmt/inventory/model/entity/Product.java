package com.store.mgmt.inventory.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(name = "category_id")
    private UUID categoryId;

    @ManyToOne
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
}