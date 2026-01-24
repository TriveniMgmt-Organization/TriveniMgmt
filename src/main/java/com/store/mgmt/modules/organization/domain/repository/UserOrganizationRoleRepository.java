package com.store.mgmt.modules.organization.domain.repository;

import com.store.mgmt.modules.organization.domain.model.UserOrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOrganizationRoleRepository extends JpaRepository<UserOrganizationRole, UUID> {

    List<UserOrganizationRole> findByUserId(UUID userId);

    Optional<UserOrganizationRole> findByOrganizationId(UUID organizationId);

    Optional<UserOrganizationRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    List<UserOrganizationRole> findAllByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    Optional<UserOrganizationRole> findByUserIdAndOrganizationIdAndStoreId(UUID userId, UUID organizationId, UUID storeId);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    boolean existsByUserIdAndStoreId(UUID userId, UUID storeId);

    @Query("SELECT uor FROM UserOrganizationRole uor " +
           "LEFT JOIN FETCH uor.organization " +
           "LEFT JOIN FETCH uor.store " +
           "WHERE uor.userId = :userId")
    List<UserOrganizationRole> findByUserIdWithOrganizationAndStore(@Param("userId") UUID userId);
}
