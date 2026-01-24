package com.store.mgmt.modules.inventory.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Filter;

import java.util.UUID;

/**
 * UnitOfMeasure entity - Units of measure within an organization.
 * Uses UUID reference for Organization to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "units_of_measure")
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UnitOfMeasure extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name; // e.g., "Kilogram", "Piece"

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code; // e.g., "kg", "pc"
}