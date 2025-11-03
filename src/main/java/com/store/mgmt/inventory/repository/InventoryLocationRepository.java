package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.InventoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, UUID> {
    @Query("SELECT u FROM InventoryLocation u WHERE u.name = :name AND u.deletedAt IS NULL")
    Optional<InventoryLocation> findByName(String name);
    Optional<InventoryLocation> findByIdAndStoreId(UUID id, UUID storeId);
    Optional<InventoryLocation> findByNameAndStoreId( String name, UUID storeId);
    List<InventoryLocation> findByStoreId(UUID storeId);
}