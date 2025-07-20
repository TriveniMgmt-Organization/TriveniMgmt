package com.store.mgmt.users.repository;

import com.store.mgmt.users.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    @Query("SELECT u FROM AuditLog u WHERE u.id = :id AND u.deletedAt IS NULL")
    @NonNull
    Optional<AuditLog> findById(@NonNull UUID id);
    @Query("SELECT u FROM AuditLog u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    @NonNull
    List<AuditLog> findAll();
}
