package com.store.mgmt.users.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
}