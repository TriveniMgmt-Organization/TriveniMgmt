package com.store.mgmt.organization.repository;

import com.store.mgmt.organization.model.entity.Invitation;
import com.store.mgmt.organization.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    Optional<Invitation> findByTokenAndUsedFalse(String token);

}
