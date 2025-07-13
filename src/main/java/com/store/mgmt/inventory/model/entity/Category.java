package com.store.mgmt.inventory.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
}