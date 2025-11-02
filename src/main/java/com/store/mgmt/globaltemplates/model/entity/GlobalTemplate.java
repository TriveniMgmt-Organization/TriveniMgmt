package com.store.mgmt.globaltemplates.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "global_templates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"code"})
})
@Data
@EqualsAndHashCode(callSuper = false)
public class GlobalTemplate extends BaseEntity {
    
    @Column(name = "name", nullable = false, length = 100)
    private String name; // e.g., "Retail Starter", "Grocery Pro"
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // e.g., "RETAIL_BASIC", "GROCERY_PRO"
    
    @Column(name = "type", nullable = false, length = 50)
    private String type; // e.g., "RETAIL", "GROCERY", "PHARMA"
    
    @Column(name = "version", nullable = false)
    private Integer version = 1;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @OrderBy("sortOrder ASC")
    private Set<GlobalTemplateItem> items;
}

