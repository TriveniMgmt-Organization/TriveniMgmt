package com.store.mgmt.modules.organization.domain.repository;

import com.store.mgmt.modules.organization.domain.model.UserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAssignmentRepository extends JpaRepository<UserAssignment, UUID> {

    List<UserAssignment> findByUserId(UUID userId);

    List<UserAssignment> findByOrganizationId(UUID organizationId);

    List<UserAssignment> findByStoreId(UUID storeId);

    Optional<UserAssignment> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    Optional<UserAssignment> findByUserIdAndStoreId(UUID userId, UUID storeId);
}
