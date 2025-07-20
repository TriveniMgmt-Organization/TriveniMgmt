package com.store.mgmt.organization.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.users.model.entity.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
@Data
public class Invitation extends BaseEntity {
    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column
    private boolean used = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}