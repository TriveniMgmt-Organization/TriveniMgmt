package com.store.mgmt.inventory.repository;

// PurchaseOrderRepository.java

import com.store.mgmt.inventory.model.entity.PurchaseOrder;
import com.store.mgmt.inventory.model.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findBySupplierId(UUID supplierId);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);

    Optional<PurchaseOrder> findByIdAndOrganizationId(UUID id, UUID organizationId);
    Optional<PurchaseOrder> findBySupplierIdAndOrganizationId( UUID supplierId, UUID organizationId);
    List<PurchaseOrder> findByStatusAndOrganizationId(PurchaseOrderStatus status, UUID organizationId);
    List<PurchaseOrder> findByOrganizationId(UUID organizationId);
}