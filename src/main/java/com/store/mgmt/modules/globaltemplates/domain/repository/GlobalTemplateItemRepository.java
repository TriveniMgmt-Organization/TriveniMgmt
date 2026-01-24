package com.store.mgmt.modules.globaltemplates.domain.repository;

import com.store.mgmt.modules.globaltemplates.domain.model.GlobalTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GlobalTemplateItemRepository extends JpaRepository<GlobalTemplateItem, UUID> {

    List<GlobalTemplateItem> findByTemplateIdOrderBySortOrder(UUID templateId);
}
