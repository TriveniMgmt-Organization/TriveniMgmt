package com.store.mgmt.inventory.repository;
import com.store.mgmt.inventory.model.entity.DamageLoss;
import com.store.mgmt.inventory.model.enums.DamageLossReason;
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
    List<DamageLoss> findByProductTemplateId(UUID productTemplateId);
    List<DamageLoss> findByLocationId(UUID locationId);
    List<DamageLoss> findByReason(DamageLossReason reason);

    Optional<DamageLoss> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<DamageLoss> findByLocationIdAndStoreId(UUID locationId, UUID storeId);
    List<DamageLoss> findByDateRecordedBetweenAndStoreId(LocalDateTime startDate, LocalDateTime endDate, UUID storeId);
}