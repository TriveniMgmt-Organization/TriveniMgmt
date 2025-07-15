package com.store.mgmt.pos.repository;

import com.store.mgmt.pos.model.entity.PosSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<PosSale, UUID> {
    List<PosSale> findByUserId(UUID userId);
    List<PosSale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
}
