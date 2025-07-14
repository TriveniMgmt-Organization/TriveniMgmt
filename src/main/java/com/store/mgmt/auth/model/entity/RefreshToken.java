package com.store.mgmt.auth.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
@Data
public class RefreshToken extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Date expiryDate;
}