package com.store.mgmt.users.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.organization.model.entity.Organization;
import com.store.mgmt.organization.model.entity.Store;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import java.util.Set;
import java.util.UUID;
//-- Audit log for permission/role changes
@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store; // Nullable for organization-level actions

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)

    private User user;
    @Column(name="action", nullable = false )
    private String action; //-- e.g., 'ASSIGNED_ROLE', 'UPDATED_PERMISSION'

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Type(JsonType.class)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private String details;

}