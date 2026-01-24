package com.store.mgmt.modules.users.infrastructure.persistence.adapter;

import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.repository.UserRepository;
import com.store.mgmt.modules.users.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for User entity.
 * Since domain User is now a JPA entity, this adapter simply delegates to JpaUserRepository.
 */
@Component
public class UserPersistenceAdapter implements UserRepository {

    private final JpaUserRepository jpaRepository;

    public UserPersistenceAdapter(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public @NonNull Optional<User> findById(@NonNull UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public @NonNull List<User> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<User> findAllWithRolesAndPermissions() {
        return jpaRepository.findAllWithRolesAndPermissions();
    }

    @Override
    public Optional<User> findByIdWithRolesAndPermissions(UUID id) {
        return jpaRepository.findByIdWithRolesAndPermissions(id);
    }

    @Override
    public Optional<User> findByUsernameWithAllRelatedData(String username) {
        return jpaRepository.findByUsernameWithAllRelatedData(username);
    }

    @Override
    public Optional<User> findByEmailWithRolesAndPermissions(String email) {
        return jpaRepository.findByEmailWithRolesAndPermissions(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
