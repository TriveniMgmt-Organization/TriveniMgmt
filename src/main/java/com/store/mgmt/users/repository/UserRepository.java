package com.store.mgmt.users.repository;

import com.store.mgmt.users.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<User> findById(@org.springframework.lang.NonNull UUID id);
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<User> findAll();

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.organizationRoles ur " +     // Eagerly fetch User's roles
            "LEFT JOIN FETCH ur.organization o " +         // Eagerly fetch Organization for each role
            "LEFT JOIN FETCH ur.store s " +                // Eagerly fetch Store for each role (if role has one)
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithAllRelatedData(@Param("username") String username);
}
