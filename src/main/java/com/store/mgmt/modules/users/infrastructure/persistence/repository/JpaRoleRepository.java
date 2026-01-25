package com.store.mgmt.modules.users.infrastructure.persistence.repository;

import com.store.mgmt.modules.users.domain.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, UUID> {

    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL")
    List<Role> findAllActive();

    @Query("SELECT r FROM Role r WHERE r.deletedAt IS NULL")
    Page<Role> findAllActive(Pageable pageable);

    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Role> findByIdWithPermissions(@Param("id") UUID id);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id IN :ids AND r.deletedAt IS NULL")
    List<Role> findByIdsWithPermissions(@Param("ids") List<UUID> ids);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.deletedAt IS NULL")
    List<Role> findAllWithPermissions();

    boolean existsByName(String name);
}
