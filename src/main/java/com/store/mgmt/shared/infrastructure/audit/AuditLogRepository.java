package com.store.mgmt.shared.infrastructure.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for audit log persistence.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByOrganizationId(UUID organizationId);

    List<AuditLog> findByOrganizationIdAndEntityType(UUID organizationId, String entityType);

    List<AuditLog> findByEntityId(UUID entityId);
}
