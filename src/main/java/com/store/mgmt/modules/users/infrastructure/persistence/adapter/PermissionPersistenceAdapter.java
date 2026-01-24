package com.store.mgmt.modules.users.infrastructure.persistence.adapter;

import com.store.mgmt.modules.users.domain.model.Permission;
import com.store.mgmt.modules.users.domain.repository.PermissionRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaPermissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for Permission entity.
 * Since domain Permission is now a JPA entity, this adapter simply delegates to JpaPermissionRepository.
 */
@Component
public class PermissionPersistenceAdapter implements PermissionRepository {

    private final JpaPermissionRepository jpaRepository;

    public PermissionPersistenceAdapter(JpaPermissionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Page<Permission> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findAllActive(pageable);
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findByName(name);
    }
}
