package com.store.mgmt.reporting.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "reports")
@Data
@EqualsAndHashCode(callSuper = false)
public class Report extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private String data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Report.Type type;

    public enum Type {
        SALES_SUMMARY,
        INVENTORY_STATUS,
        CANCELLED
    }
}
