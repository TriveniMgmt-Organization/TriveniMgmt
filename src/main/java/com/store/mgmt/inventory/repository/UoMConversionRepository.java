package com.store.mgmt.inventory.repository;

import com.store.mgmt.inventory.model.entity.UoMConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UoMConversionRepository extends JpaRepository<UoMConversion, UUID> {
    
    // Find conversion from one UoM to another
    @Query("SELECT c FROM UoMConversion c WHERE c.fromUom.id = :fromUomId AND c.toUom.id = :toUomId AND c.deletedAt IS NULL")
    Optional<UoMConversion> findByFromUomIdAndToUomId(@Param("fromUomId") UUID fromUomId, @Param("toUomId") UUID toUomId);
    
    // Find all conversions from a specific UoM
    @Query("SELECT c FROM UoMConversion c WHERE c.fromUom.id = :uomId AND c.deletedAt IS NULL")
    List<UoMConversion> findByFromUomId(@Param("uomId") UUID uomId);
    
    // Find all conversions to a specific UoM
    @Query("SELECT c FROM UoMConversion c WHERE c.toUom.id = :uomId AND c.deletedAt IS NULL")
    List<UoMConversion> findByToUomId(@Param("uomId") UUID uomId);
    
    // Find all conversions for a UoM (both from and to)
    @Query("SELECT c FROM UoMConversion c WHERE (c.fromUom.id = :uomId OR c.toUom.id = :uomId) AND c.deletedAt IS NULL")
    List<UoMConversion> findAllByUomId(@Param("uomId") UUID uomId);
}

