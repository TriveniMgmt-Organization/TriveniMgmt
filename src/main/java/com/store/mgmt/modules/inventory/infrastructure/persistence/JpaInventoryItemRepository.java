package com.store.mgmt.modules.inventory.infrastructure.persistence;

import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.InventoryItemRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of InventoryItemRepository.
 * Maps between domain model and JPA entities.
 */
@Repository("moduleInventoryItemRepository")
public class JpaInventoryItemRepository implements InventoryItemRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<InventoryItem> findById(InventoryItemId id) {
        com.store.mgmt.inventory.model.entity.InventoryItem entity =
                em.find(com.store.mgmt.inventory.model.entity.InventoryItem.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    @Override
    public Optional<InventoryItem> findByIdAndStoreId(InventoryItemId id, StoreId storeId) {
        try {
            TypedQuery<com.store.mgmt.inventory.model.entity.InventoryItem> query = em.createQuery("""
                SELECT i FROM InventoryItem i
                LEFT JOIN FETCH i.variant v
                LEFT JOIN FETCH v.template
                LEFT JOIN FETCH i.location l
                LEFT JOIN FETCH i.batchLot
                LEFT JOIN FETCH i.stockLevel
                WHERE i.id = :id
                AND i.location.store.id = :storeId
                AND i.deletedAt IS NULL
                """, com.store.mgmt.inventory.model.entity.InventoryItem.class);
            query.setParameter("id", id.getValue());
            query.setParameter("storeId", storeId.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<InventoryItem> findByStoreId(StoreId storeId) {
        TypedQuery<com.store.mgmt.inventory.model.entity.InventoryItem> query = em.createQuery("""
            SELECT DISTINCT i FROM InventoryItem i
            LEFT JOIN FETCH i.variant v
            LEFT JOIN FETCH v.template
            LEFT JOIN FETCH i.location l
            LEFT JOIN FETCH i.batchLot
            LEFT JOIN FETCH i.stockLevel
            WHERE i.location.store.id = :storeId
            AND i.deletedAt IS NULL
            ORDER BY i.createdAt DESC
            """, com.store.mgmt.inventory.model.entity.InventoryItem.class);
        query.setParameter("storeId", storeId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByLocationId(LocationId locationId) {
        TypedQuery<com.store.mgmt.inventory.model.entity.InventoryItem> query = em.createQuery("""
            SELECT DISTINCT i FROM InventoryItem i
            LEFT JOIN FETCH i.variant v
            LEFT JOIN FETCH v.template
            LEFT JOIN FETCH i.location l
            LEFT JOIN FETCH i.batchLot
            LEFT JOIN FETCH i.stockLevel
            WHERE i.location.id = :locationId
            AND i.deletedAt IS NULL
            ORDER BY i.createdAt DESC
            """, com.store.mgmt.inventory.model.entity.InventoryItem.class);
        query.setParameter("locationId", locationId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InventoryItem> findByVariantIdAndLocationId(ProductVariantId variantId, LocationId locationId) {
        try {
            TypedQuery<com.store.mgmt.inventory.model.entity.InventoryItem> query = em.createQuery("""
                SELECT i FROM InventoryItem i
                LEFT JOIN FETCH i.stockLevel
                WHERE i.variant.id = :variantId
                AND i.location.id = :locationId
                AND i.deletedAt IS NULL
                """, com.store.mgmt.inventory.model.entity.InventoryItem.class);
            query.setParameter("variantId", variantId.getValue());
            query.setParameter("locationId", locationId.getValue());

            return Optional.of(toDomain(query.getSingleResult()));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<InventoryItem> findLowStockByStoreId(StoreId storeId) {
        TypedQuery<com.store.mgmt.inventory.model.entity.InventoryItem> query = em.createQuery("""
            SELECT DISTINCT i FROM InventoryItem i
            LEFT JOIN FETCH i.variant v
            LEFT JOIN FETCH v.template
            LEFT JOIN FETCH i.location l
            LEFT JOIN FETCH i.batchLot
            JOIN FETCH i.stockLevel sl
            WHERE i.location.store.id = :storeId
            AND i.deletedAt IS NULL
            AND sl.onHand <= sl.reorderPoint
            ORDER BY sl.onHand ASC
            """, com.store.mgmt.inventory.model.entity.InventoryItem.class);
        query.setParameter("storeId", storeId.getValue());

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findExpiringSoon(StoreId storeId, int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);

        TypedQuery<com.store.mgmt.inventory.model.entity.InventoryItem> query = em.createQuery("""
            SELECT DISTINCT i FROM InventoryItem i
            LEFT JOIN FETCH i.variant v
            LEFT JOIN FETCH v.template
            LEFT JOIN FETCH i.location l
            LEFT JOIN FETCH i.batchLot
            LEFT JOIN FETCH i.stockLevel
            WHERE i.location.store.id = :storeId
            AND i.deletedAt IS NULL
            AND i.expiryDate IS NOT NULL
            AND i.expiryDate <= :thresholdDate
            ORDER BY i.expiryDate ASC
            """, com.store.mgmt.inventory.model.entity.InventoryItem.class);
        query.setParameter("storeId", storeId.getValue());
        query.setParameter("thresholdDate", thresholdDate);

        return query.getResultList().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByVariantIdAndLocationId(ProductVariantId variantId, LocationId locationId) {
        TypedQuery<Long> query = em.createQuery("""
            SELECT COUNT(i) FROM InventoryItem i
            WHERE i.variant.id = :variantId
            AND i.location.id = :locationId
            AND i.deletedAt IS NULL
            """, Long.class);
        query.setParameter("variantId", variantId.getValue());
        query.setParameter("locationId", locationId.getValue());

        return query.getSingleResult() > 0;
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        com.store.mgmt.inventory.model.entity.InventoryItem entity = toEntity(item);

        if (em.find(com.store.mgmt.inventory.model.entity.InventoryItem.class, item.getId().getValue()) == null) {
            em.persist(entity);
            // Also persist the stock level
            if (entity.getStockLevel() != null) {
                em.persist(entity.getStockLevel());
            }
        } else {
            entity = em.merge(entity);
        }

        em.flush();
        return toDomain(entity);
    }

    @Override
    public void delete(InventoryItem item) {
        com.store.mgmt.inventory.model.entity.InventoryItem entity =
                em.find(com.store.mgmt.inventory.model.entity.InventoryItem.class, item.getId().getValue());
        if (entity != null) {
            entity.setDeletedAt(item.getDeletedAt());
            em.merge(entity);
        }
    }

    @Override
    public void deleteById(InventoryItemId id) {
        com.store.mgmt.inventory.model.entity.InventoryItem entity =
                em.find(com.store.mgmt.inventory.model.entity.InventoryItem.class, id.getValue());
        if (entity != null) {
            entity.setDeletedAt(java.time.LocalDateTime.now());
            em.merge(entity);
        }
    }

    // ==================== Mapping Methods ====================

    private InventoryItem toDomain(com.store.mgmt.inventory.model.entity.InventoryItem entity) {
        com.store.mgmt.inventory.model.entity.StockLevel sl = entity.getStockLevel();
        // Map from entity fields: committed -> reserved, lowStockThreshold -> reorderPoint
        StockLevel stockLevel = sl != null
                ? new StockLevel(sl.getOnHand(), sl.getCommitted(), sl.getLowStockThreshold())
                : StockLevel.zero(10);

        return InventoryItem.reconstitute(
                InventoryItemId.of(entity.getId()),
                ProductVariantId.of(entity.getVariant().getId()),
                LocationId.of(entity.getLocation().getId()),
                StoreId.of(entity.getLocation().getStore().getId()),
                stockLevel,
                entity.getBatchLot() != null ? entity.getBatchLot().getBatchNumber() : null,
                entity.getExpiryDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private com.store.mgmt.inventory.model.entity.InventoryItem toEntity(InventoryItem domain) {
        com.store.mgmt.inventory.model.entity.InventoryItem entity =
                em.find(com.store.mgmt.inventory.model.entity.InventoryItem.class, domain.getId().getValue());

        if (entity == null) {
            entity = new com.store.mgmt.inventory.model.entity.InventoryItem();
            entity.setId(domain.getId().getValue());

            // Load references
            com.store.mgmt.inventory.model.entity.ProductVariant variant =
                    em.find(com.store.mgmt.inventory.model.entity.ProductVariant.class, domain.getVariantId().getValue());
            com.store.mgmt.inventory.model.entity.InventoryLocation location =
                    em.find(com.store.mgmt.inventory.model.entity.InventoryLocation.class, domain.getLocationId().getValue());

            entity.setVariant(variant);
            entity.setLocation(location);

            // Create stock level - map: reserved -> committed, reorderPoint -> lowStockThreshold
            com.store.mgmt.inventory.model.entity.StockLevel stockLevel =
                    new com.store.mgmt.inventory.model.entity.StockLevel();
            stockLevel.setInventoryItem(entity);
            stockLevel.setOnHand(domain.getStockLevel().onHand());
            stockLevel.setCommitted(domain.getStockLevel().reserved());
            stockLevel.setAvailable(domain.getStockLevel().available());
            stockLevel.setLowStockThreshold(domain.getStockLevel().reorderPoint());
            entity.setStockLevel(stockLevel);
        } else {
            // Update stock level - map: reserved -> committed, reorderPoint -> lowStockThreshold
            com.store.mgmt.inventory.model.entity.StockLevel stockLevel = entity.getStockLevel();
            if (stockLevel != null) {
                stockLevel.setOnHand(domain.getStockLevel().onHand());
                stockLevel.setCommitted(domain.getStockLevel().reserved());
                stockLevel.setAvailable(domain.getStockLevel().available());
                stockLevel.setLowStockThreshold(domain.getStockLevel().reorderPoint());
            }
        }

        entity.setExpiryDate(domain.getExpiryDate());

        if (domain.getDeletedAt() != null) {
            entity.setDeletedAt(domain.getDeletedAt());
        }

        return entity;
    }
}
