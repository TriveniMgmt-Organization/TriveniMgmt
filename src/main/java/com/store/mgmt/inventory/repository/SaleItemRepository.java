package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
    List<SaleItem> findBySaleId(UUID saleId);

    List<SaleItem> findByProductTemplateId(UUID productTemplateId);
    List<SaleItem> findByProductTemplateIdAndStoreId(UUID productTemplateId, UUID storeId);
}