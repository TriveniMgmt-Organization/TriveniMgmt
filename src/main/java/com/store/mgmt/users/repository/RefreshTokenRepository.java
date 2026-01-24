package com.store.mgmt.users.repository;

import com.store.mgmt.auth.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);

    /**
     * Find refresh token with eager fetch of user and their organization roles.
     * Avoids N+1 queries when building auth context.
     */
    @Query("SELECT rt FROM RefreshToken rt " +
            "JOIN FETCH rt.user u " +
            "LEFT JOIN FETCH u.organizationRoles uor " +
            "LEFT JOIN FETCH uor.organization " +
            "LEFT JOIN FETCH uor.role r " +
            "LEFT JOIN FETCH r.permissions " +
            "LEFT JOIN FETCH uor.store " +
            "WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);
}