package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    @Query("SELECT u FROM Location u WHERE u.name = :name AND u.deletedAt IS NULL")
    Optional<Location> findByName(String name);
    Optional<Location> findByIdAndStoreId(UUID id, UUID storeId);
    Optional<Location> findByNameAndStoreId( String name, UUID storeId);
    List<Location> findByStoreId(UUID storeId);
}