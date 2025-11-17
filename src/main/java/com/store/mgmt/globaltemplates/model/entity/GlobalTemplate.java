package com.store.mgmt.globaltemplates.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
    name = "global_templates",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})},
    indexes = {@Index(name = "idx_template_code", columnList = "code")}
)
@Data
@EqualsAndHashCode(callSuper = false, exclude = "items")
public class GlobalTemplate extends BaseEntity {

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull
    @Column(name = "type", nullable = false, length = 50)
    private String type;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @OrderBy("sortOrder ASC")
    private Set<GlobalTemplateItem> items = new LinkedHashSet<>();
}