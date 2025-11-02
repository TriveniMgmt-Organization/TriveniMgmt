package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("SELECT u FROM Category u WHERE u.name = :name AND u.deletedAt IS NULL")
    @NonNull
    Optional<Category> findByName(@NonNull String name);
    @Query("SELECT u FROM Category u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<Category> findById(@NonNull UUID id);
    @Query("SELECT u FROM Category u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<Category> findAll();

    List<Category> findByOrganizationId(UUID organizationId);
    Optional<Category> findByIdAndOrganizationId(UUID id, UUID organizationId);
    Optional<Category> findByNameAndOrganizationId(String name, UUID organizationId);
    Optional<Category> findByCodeAndOrganizationId(String code, UUID organizationId);
}