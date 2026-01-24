package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * TaxRule entity - Tax rules within an organization.
 * Uses UUID reference for Organization to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "tax_rules")
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@Data
@EqualsAndHashCode(callSuper = false)
public class TaxRule extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String countryCode;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    private String description;
}