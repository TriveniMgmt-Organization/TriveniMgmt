package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    @Query("SELECT u FROM Supplier u WHERE u.name = :name AND u.deletedAt IS NULL")
    Optional<Supplier> findByName(String name);
}