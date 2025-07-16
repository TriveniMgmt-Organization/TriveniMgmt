package com.store.mgmt.users.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.UUID;
//-- Audit log for permission/role changes
@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {
    @Column(name="action", nullable = true )
    private String action; //-- e.g., 'ASSIGNED_ROLE', 'UPDATED_PERMISSION'

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}