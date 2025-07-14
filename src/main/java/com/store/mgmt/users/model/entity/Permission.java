package com.store.mgmt.users.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
public class Permission extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
}