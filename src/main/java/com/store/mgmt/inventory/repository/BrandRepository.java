package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.Brand;
import com.store.mgmt.inventory.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {
    @Query("SELECT u FROM Brand u WHERE u.name = :name AND u.deletedAt IS NULL")
    @NonNull
    Optional<Brand> findByName(@NonNull String name);
    @Query("SELECT u FROM Brand u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<Brand> findById(@NonNull UUID id);
    @Query("SELECT u FROM Brand u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<Brand> findAll();
}