package com.store.mgmt.inventory.repository;

// PurchaseOrderRepository.java

import com.store.mgmt.inventory.model.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findBySupplierId(UUID supplierId);
    List<PurchaseOrder> findByStatus(PurchaseOrder.PurchaseOrderStatus status);
}