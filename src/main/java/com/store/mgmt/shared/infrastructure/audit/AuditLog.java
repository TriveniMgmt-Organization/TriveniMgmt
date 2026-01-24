package com.store.mgmt.shared.infrastructure.audit;

import com.store.mgmt.shared.domain.model.BaseEntity;
import com.store.mgmt.modules.organization.domain.model.Organization;
import com.store.mgmt.modules.organization.domain.model.Store;
import com.store.mgmt.modules.users.domain.model.User;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.util.UUID;

/**
 * Audit log entity for tracking actions across the system.
 */
@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = false)
public class AuditLog extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    @ToString.Exclude
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @ToString.Exclude
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

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
