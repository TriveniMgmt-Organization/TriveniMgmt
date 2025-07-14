package com.store.mgmt.inventory.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "damage_losses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DamageLoss extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 255)
    private DamageLossReason reason;

    @Column(name = "date_recorded", nullable = false)
    private LocalDate dateRecorded = LocalDate.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Who recorded the loss
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DamageLossReason {
        EXPIRED,
        DAMAGED_IN_TRANSIT,
        SPOILAGE,
        THEFT,
        RETURNED_DAMAGED,
        OTHER
    }
}