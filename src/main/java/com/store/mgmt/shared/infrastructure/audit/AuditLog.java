package com.store.mgmt.shared.infrastructure.audit;

import com.store.mgmt.shared.domain.model.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import java.util.UUID;

/**
 * Audit log entity for tracking actions across the system.
 * Uses UUIDs to reference other modules (no cross-module entity dependencies).
 */
@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditLog extends BaseEntity {

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "username")
    private String username;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Type(JsonType.class)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    private String details;
}
