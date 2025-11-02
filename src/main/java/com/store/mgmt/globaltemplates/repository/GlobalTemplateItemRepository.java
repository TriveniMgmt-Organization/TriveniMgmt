package com.store.mgmt.globaltemplates.repository;

import com.store.mgmt.globaltemplates.model.entity.GlobalTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GlobalTemplateItemRepository extends JpaRepository<GlobalTemplateItem, UUID> {
    
    @Query("SELECT i FROM GlobalTemplateItem i WHERE i.template.id = :templateId AND i.deletedAt IS NULL ORDER BY i.sortOrder ASC")
    List<GlobalTemplateItem> findByTemplateId(UUID templateId);
    
    @Query("SELECT i FROM GlobalTemplateItem i WHERE i.template.id = :templateId AND i.entityType = :entityType AND i.deletedAt IS NULL ORDER BY i.sortOrder ASC")
    List<GlobalTemplateItem> findByTemplateIdAndEntityType(UUID templateId, String entityType);
}

