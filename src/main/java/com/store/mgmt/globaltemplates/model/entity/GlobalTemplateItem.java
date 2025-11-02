package com.store.mgmt.globaltemplates.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "global_template_items",
    indexes = {
        @Index(name = "idx_template_items_template", columnList = "template_id"),
        @Index(name = "idx_template_items_entity_type", columnList = "entityType"),
        @Index(name = "idx_template_items_sort", columnList = "template_id, sortOrder")
    }
)
@Data
@EqualsAndHashCode(callSuper = false, exclude = "template")
@ToString(exclude = "template")
public class GlobalTemplateItem extends BaseEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private GlobalTemplate template;

    @NotNull
    @Column(nullable = false)
    private String entityType;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode data;

    @Column(nullable = false)
    private int sortOrder = 0;
}