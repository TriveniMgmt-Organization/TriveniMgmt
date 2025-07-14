package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("SELECT u FROM Product u WHERE u.sku = :sku AND u.deletedAt IS NULL")
    Optional<Product> findBySku(String sku);
    @Query("SELECT u FROM Product u WHERE u.barcode = :barcode AND u.deletedAt IS NULL")
    Optional<Product> findByBarcode(String barcode);
    @Query("SELECT u FROM Product u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<Product> findById(@org.springframework.lang.NonNull UUID id);
    @Query("SELECT u FROM Product u WHERE u.categoryId = :categoryId AND u.deletedAt IS NULL")
    @NonNull
    Optional<Product> findByCategoryId(@org.springframework.lang.NonNull UUID categoryId);
    @Query("SELECT u FROM Product u WHERE u.unitOfMeasureId = :unitOfMeasureId AND u.deletedAt IS NULL")
    List<Product> findByUnitOfMeasureId(@org.springframework.lang.NonNull UUID unitOfMeasureId);
    @Query("SELECT u FROM Product u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<Product> findAll();
}