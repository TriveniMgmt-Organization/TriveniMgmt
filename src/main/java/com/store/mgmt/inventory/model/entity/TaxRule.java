package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_rules")
@Data
public class TaxRule extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String countryCode;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    private String description;
}