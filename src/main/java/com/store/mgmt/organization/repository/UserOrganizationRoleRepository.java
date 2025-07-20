package com.store.mgmt.organization.repository;

import com.store.mgmt.organization.model.entity.UserAssignment;
import com.store.mgmt.organization.model.entity.UserOrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOrganizationRoleRepository extends JpaRepository<UserOrganizationRole, UUID> {
    Optional<UserOrganizationRole> findByOrganizationId(UUID organizationId);
    Optional<UserOrganizationRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}