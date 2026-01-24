package com.store.mgmt.modules.auth.domain.repository;

import com.store.mgmt.modules.auth.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    /**
     * @deprecated RefreshToken now uses UUID userId. Use findByToken()
     * and fetch User separately via UserRepository.
     */
    @Deprecated
    default Optional<RefreshToken> findByTokenWithUser(String token) {
        return findByToken(token);
    }
}
