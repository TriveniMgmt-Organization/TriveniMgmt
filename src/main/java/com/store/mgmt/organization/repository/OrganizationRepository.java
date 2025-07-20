package com.store.mgmt.organization.repository;

import com.store.mgmt.organization.model.entity.Organization;
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
//    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
//    Optional<User> findByEmail(String email);
//    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
//    Optional<User> findByUsername(String username);

    Optional<Organization> findByName(@NonNull String name);
    @Query("SELECT u FROM Organization u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<Organization> findById(@NonNull UUID id);
    @Query("SELECT u FROM Organization u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<Organization> findAll();

    @Query("SELECT o, s FROM Organization o LEFT JOIN Store s ON s.id = :storeId WHERE o.id = :orgId")
    Optional<Object[]> findOrganizationAndStore(@Param("orgId") UUID orgId, @Param("storeId") UUID storeId);


    @Query("SELECT o FROM Organization o JOIN o.userRoles ur WHERE ur.user.id = :userId AND o.deletedAt IS NULL")
    List<Organization> findAllByUserId(UUID userId);
}
