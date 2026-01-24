package com.store.mgmt.modules.organization.domain.repository;

import com.store.mgmt.modules.organization.domain.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByName(@NonNull String name);

    @Query("SELECT u FROM Organization u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<Organization> findById(@NonNull UUID id);

    @Query("SELECT DISTINCT o FROM Organization o " +
           "LEFT JOIN FETCH o.stores s " +
           "WHERE o.id = :id AND o.deletedAt IS NULL " +
           "AND (s.deletedAt IS NULL OR s IS NULL)")
    Optional<Organization> findByIdWithStores(@Param("id") UUID id);

    @Query("SELECT u FROM Organization u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<Organization> findAll();

    @Query("SELECT o, s FROM Organization o LEFT JOIN Store s ON s.id = :storeId WHERE o.id = :orgId")
    Optional<Object[]> findOrganizationAndStore(@Param("orgId") UUID orgId, @Param("storeId") UUID storeId);

    @Query("SELECT o FROM Organization o JOIN o.userRoles ur WHERE ur.userId = :userId AND o.deletedAt IS NULL")
    List<Organization> findAllByUserId(@Param("userId") UUID userId);

    boolean existsByName(String name);
}
