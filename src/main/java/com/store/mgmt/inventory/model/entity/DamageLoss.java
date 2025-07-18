package com.store.mgmt.inventory.model.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "damage_losses")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DamageLoss extends BaseEntity {

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
    private LocalDateTime dateRecorded = LocalDateTime.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Who recorded the loss
    private User user;

    public enum DamageLossReason {
        EXPIRED("expired"),
        DAMAGED_IN_TRANSIT("damaged_in_transit"),
        SPOILAGE("spoilage"),
        THEFT("theft"),
        RETURNED_DAMAGED("returned_damaged"),
        OTHER("other");

        private final String value;

        DamageLossReason(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

}