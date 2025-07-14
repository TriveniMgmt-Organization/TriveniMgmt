package com.store.mgmt.pos.model.entity;

import com.store.mgmt.common.model.BaseEntity;
import com.store.mgmt.customer.model.entity.Customer;
import com.store.mgmt.users.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "sales")
@Data
public class Sale extends BaseEntity {

    @Column(nullable = false)
    private BigDecimal tax;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime saleDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> saleItems;

    public enum PaymentMethod {
        CASH, CARD, UPI
    }
}