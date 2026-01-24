package com.store.mgmt.modules.inventory.domain.repository;
import com.store.mgmt.modules.inventory.domain.model.DamageLoss;
import com.store.mgmt.modules.inventory.domain.model.DamageLossReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DamageLossRepository extends JpaRepository<DamageLoss, UUID> {
    List<DamageLoss> findByDateRecordedBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<DamageLoss> findByVariantId(UUID variantId); // Changed from productTemplateId to variantId
    List<DamageLoss> findByLocationId(UUID locationId);
    List<DamageLoss> findByReason(DamageLossReason reason);

    Optional<DamageLoss> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<DamageLoss> findByLocationIdAndStoreId(UUID locationId, UUID storeId);
    List<DamageLoss> findByDateRecordedBetweenAndStoreId(LocalDateTime startDate, LocalDateTime endDate, UUID storeId);
}