package com.store.mgmt.modules.auth.domain.model;

import com.store.mgmt.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * RefreshToken entity - Stores refresh tokens for user authentication.
 * Uses UUID reference to User to avoid cross-module entity dependency.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@EqualsAndHashCode(callSuper = false)
public class RefreshToken extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 2048)
    private String token;

    @Column(nullable = false)
    private Date expiryDate;
}
