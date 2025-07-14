package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set; // For OneToMany relationship

@Entity
@Table(name = "units_of_measure")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasure extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name; // e.g., "Kilogram", "Piece"

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code; // e.g., "kg", "pc"

    // Optional: One-to-Many relationship with Product (if you want to navigate from UoM to Products)
    // Be careful with bidirectional relationships and performance.
    // For simplicity in Inventory Management, often one-way (Product -> UoM) is enough.
    // @OneToMany(mappedBy = "unitOfMeasure", fetch = FetchType.LAZY)
    // @ToString.Exclude // Avoid stack overflow with Lombok's @Data
    // @EqualsAndHashCode.Exclude // Avoid infinite loop with Lombok's @Data
    // private Set<Product> products;
}