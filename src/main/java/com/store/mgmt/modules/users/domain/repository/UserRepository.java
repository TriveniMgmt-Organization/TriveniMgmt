package com.store.mgmt.modules.users.domain.repository;

import com.store.mgmt.modules.users.domain.model.Email;
import com.store.mgmt.modules.users.domain.model.User;
import com.store.mgmt.modules.users.domain.model.UserId;
import com.store.mgmt.modules.users.domain.model.Username;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User aggregate.
 */
public interface UserRepository {

    /**
     * Find a user by ID.
     */
    Optional<User> findById(UserId id);

    /**
     * Find a user by email.
     */
    Optional<User> findByEmail(Email email);

    /**
     * Find a user by username.
     */
    Optional<User> findByUsername(Username username);

    /**
     * Find all users.
     */
    List<User> findAll();

    /**
     * Check if a user with the given email exists.
     */
    boolean existsByEmail(Email email);

    /**
     * Check if a user with the given username exists.
     */
    boolean existsByUsername(Username username);

    /**
     * Save a user.
     */
    User save(User user);

    /**
     * Delete a user (soft delete).
     */
    void delete(User user);
}
