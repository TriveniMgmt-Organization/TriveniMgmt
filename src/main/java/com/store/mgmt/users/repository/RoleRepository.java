package com.store.mgmt.users.repository;

import com.store.mgmt.users.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();
}