package com.store.mgmt.inventory.repository;
import com.store.mgmt.inventory.model.entity.DamageLoss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DamageLossRepository extends JpaRepository<DamageLoss, UUID> {
    List<DamageLoss> findByDateRecordedBetween(LocalDate startDate, LocalDate endDate);
    List<DamageLoss> findByProductId(UUID productId);
    List<DamageLoss> findByLocationId(UUID locationId);
    List<DamageLoss> findByReason(DamageLoss.DamageLossReason reason);
}