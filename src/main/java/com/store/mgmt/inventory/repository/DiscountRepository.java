package com.store.mgmt.inventory.repository;
import com.store.mgmt.inventory.model.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {
    Optional<Discount> findByName(String name);
    List<Discount> findByOrganizationId(UUID organizationId);
    List<Discount> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate today, LocalDate today2);
    List<Discount> findByProductTemplateIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID productId, LocalDate today, LocalDate today2);
    List<Discount> findByCategoryIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(UUID categoryId, LocalDate today, LocalDate today2);

    Optional<Discount> findByIdAndStoreId(UUID id, UUID organizationId);
    Optional<Discount> findByNameAndOrganizationId(String name, UUID organizationId);
}