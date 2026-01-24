package com.store.mgmt.modules.users.infrastructure.persistence.repository;

import com.store.mgmt.modules.users.domain.model.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPermissionRepository extends JpaRepository<Permission, UUID> {

    @Query("SELECT p FROM Permission p WHERE p.deletedAt IS NULL")
    Page<Permission> findAllActive(Pageable pageable);

    Optional<Permission> findByName(String name);
}
