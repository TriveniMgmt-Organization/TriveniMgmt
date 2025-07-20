package com.store.mgmt.organization.repository;

import com.store.mgmt.organization.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    @Query("SELECT u FROM Store u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<Store> findById(@NonNull UUID id);
    @Query("SELECT u FROM Store u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<Store> findAll();

    Optional<Store> findByNameAndOrganizationId(String name, UUID organizationId);
    List<Store> findByOrganizationId(UUID organizationId);
}
