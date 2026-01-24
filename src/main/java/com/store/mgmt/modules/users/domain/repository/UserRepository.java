package com.store.mgmt.modules.users.domain.repository;

import com.store.mgmt.modules.users.domain.model.User;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

        Optional<User> findByEmail(String email);

        Optional<User> findByUsername(String username);

        @NonNull
        Optional<User> findById(@NonNull UUID id);

        @NonNull
        List<User> findAll();

        List<User> findAllWithRolesAndPermissions();

        Optional<User> findByIdWithRolesAndPermissions(UUID id);

        Optional<User> findByUsernameWithAllRelatedData(String username);

        Optional<User> findByEmailWithRolesAndPermissions(String email);

        boolean existsByEmail(String email);

        boolean existsByUsername(String username);

        User save(User user);

        void deleteById(UUID id);
}
