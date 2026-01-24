package com.store.mgmt.modules.users.infrastructure.persistence.adapter;

import com.store.mgmt.modules.users.domain.model.Role;
import com.store.mgmt.modules.users.domain.repository.RoleRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for Role entity.
 * Since domain Role is now a JPA entity, this adapter simply delegates to JpaRoleRepository.
 */
@Component
public class RolePersistenceAdapter implements RoleRepository {

    private final JpaRoleRepository jpaRepository;

    public RolePersistenceAdapter(JpaRoleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Role save(Role role) {
        return jpaRepository.save(role);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAllActive();
    }

    @Override
    public Page<Role> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jpaRepository.findAllActive(pageable);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public Optional<Role> findByIdWithPermissions(UUID id) {
        return jpaRepository.findByIdWithPermissions(id);
    }
}
