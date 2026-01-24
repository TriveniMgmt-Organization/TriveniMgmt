package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "batch_lots",
       indexes = {
           @Index(name = "idx_batch_number", columnList = "batch_number"),
           @Index(name = "idx_batch_expiry", columnList = "expiry_date")
       })
@Data
@EqualsAndHashCode(callSuper = false)
public class BatchLot extends BaseEntity {

    @Column(name = "batch_number", nullable = false, length = 100)
    private String batchNumber;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Supplier supplier;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}