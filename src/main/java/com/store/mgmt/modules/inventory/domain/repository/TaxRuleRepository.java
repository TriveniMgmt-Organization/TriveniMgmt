package com.store.mgmt.modules.inventory.domain.repository;

import com.store.mgmt.modules.inventory.domain.model.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, UUID> {
    
    List<TaxRule> findByOrganizationId(UUID organizationId);
    
    Optional<TaxRule> findByOrganizationIdAndCountryCode(UUID organizationId, String countryCode);
    
    @Query("SELECT t FROM TaxRule t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<TaxRule> findById(UUID id);
}

