package com.store.mgmt.users.repository;

import com.store.mgmt.users.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
    @Query("SELECT DISTINCT p FROM User u JOIN u.organizationRoles a JOIN a.role r JOIN r.permissions p WHERE u.id = :userId")
    List<Permission> findPermissionsByUserId(@Param("userId") UUID userId);
}