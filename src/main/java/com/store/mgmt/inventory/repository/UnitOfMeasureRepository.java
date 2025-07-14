package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {
    @Query("SELECT u FROM UnitOfMeasure u WHERE u.name = :name AND u.deletedAt IS NULL")
    @NonNull
    Optional<UnitOfMeasure> findByName(@NonNull String name);

    @Query("SELECT u FROM UnitOfMeasure u WHERE u.code = :code AND u.deletedAt IS NULL")
    Optional<UnitOfMeasure> findByCode(@NonNull String code);
}
