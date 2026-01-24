package com.store.mgmt.modules.organization.domain.repository;

import com.store.mgmt.modules.organization.domain.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByToken(String token);

    List<Invitation> findByOrganizationId(UUID organizationId);

    List<Invitation> findByEmail(String email);

    Optional<Invitation> findByEmailAndOrganizationIdAndUsedFalse(String email, UUID organizationId);

    Optional<Invitation> findByTokenAndUsedFalse(String token);
}
