package com.store.mgmt.modules.users.domain.repository;

import com.store.mgmt.modules.users.domain.model.Role;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(UUID id);

    Optional<Role> findByName(String name);

    List<Role> findAll();

    Page<Role> findAll(int page, int size);

    void deleteById(UUID id);

    boolean existsByName(String name);

    Optional<Role> findByIdWithPermissions(UUID id);
}
