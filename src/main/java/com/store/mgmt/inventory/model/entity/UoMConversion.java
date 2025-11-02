package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "uom_conversions")
@Data
@EqualsAndHashCode(callSuper = false)
public class UoMConversion extends BaseEntity {
    @ManyToOne
    private UnitOfMeasure fromUom;
    @ManyToOne
    private UnitOfMeasure toUom;
    private BigDecimal ratio; // 1 Box = 12 Each
}