package com.store.mgmt.organization.repository;

import com.store.mgmt.organization.model.entity.UserAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAssignmentRepository extends JpaRepository<UserAssignment, UUID> {
//    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
//    Optional<User> findByEmail(String email);
//    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
//    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM UserAssignment u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<UserAssignment> findById(@NonNull UUID id);
    @Query("SELECT u FROM UserAssignment u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<UserAssignment> findAll();
}
