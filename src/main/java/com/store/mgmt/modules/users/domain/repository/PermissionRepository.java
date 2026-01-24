package com.store.mgmt.modules.users.domain.repository;

import com.store.mgmt.modules.users.domain.model.Permission;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    Page<Permission> findAll(int page, int size);

    Optional<Permission> findById(UUID id);

    Optional<Permission> findByName(String name);
}
