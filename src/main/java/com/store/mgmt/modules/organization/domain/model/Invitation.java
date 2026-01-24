package com.store.mgmt.modules.organization.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invitation entity - Stores organization invitations.
 * Uses UUID reference for Role to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "invitations")
@Data
@EqualsAndHashCode(callSuper = false)
public class Invitation extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column
    private boolean used = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
