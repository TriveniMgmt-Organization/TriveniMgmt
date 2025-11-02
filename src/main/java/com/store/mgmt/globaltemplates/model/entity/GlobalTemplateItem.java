package com.store.mgmt.globaltemplates.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "global_template_items")
@Data
@EqualsAndHashCode(callSuper = false)
public class GlobalTemplateItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private GlobalTemplate template;
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // e.g., "BRAND", "CATEGORY", "UOM", "TAX_RULE"
    
    @Type(JsonType.class)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private String data; // Flexible JSON payload
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}

