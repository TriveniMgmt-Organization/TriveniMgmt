package com.store.mgmt.globaltemplates.repository;

import com.store.mgmt.globaltemplates.model.entity.GlobalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GlobalTemplateRepository extends JpaRepository<GlobalTemplate, UUID> {
    
    @Query("SELECT DISTINCT t FROM GlobalTemplate t LEFT JOIN FETCH t.items WHERE t.code = :code AND t.deletedAt IS NULL")
    Optional<GlobalTemplate> findByCode(String code);
    
    @Query("SELECT DISTINCT t FROM GlobalTemplate t LEFT JOIN FETCH t.items WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<GlobalTemplate> findByIdWithItems(UUID id);
    
    @Query("SELECT DISTINCT t FROM GlobalTemplate t LEFT JOIN FETCH t.items WHERE t.isActive = true AND t.deletedAt IS NULL ORDER BY t.name ASC")
    List<GlobalTemplate> findAllActive();
    
    @Query("SELECT DISTINCT t FROM GlobalTemplate t LEFT JOIN FETCH t.items WHERE t.type = :type AND t.isActive = true AND t.deletedAt IS NULL ORDER BY t.name ASC")
    List<GlobalTemplate> findByType(String type);
    
    @Query("SELECT DISTINCT t FROM GlobalTemplate t LEFT JOIN FETCH t.items WHERE t.deletedAt IS NULL ORDER BY t.name ASC")
    List<GlobalTemplate> findAll();
}

