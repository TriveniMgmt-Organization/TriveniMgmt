package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Entity
@Table(name = "batch_lots")
@Data
@EqualsAndHashCode(callSuper = false)
public class BatchLot extends BaseEntity {
    @Column(nullable = false)
    private String batchNumber;

    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
}