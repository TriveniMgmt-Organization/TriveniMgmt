package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.*;
import com.store.mgmt.modules.inventory.application.dto.DamageLossResponseDTO;
import com.store.mgmt.modules.inventory.application.service.DamageLossMapper;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handler for RecordDamageLossCommand.
 */
@Component
@Transactional
public class RecordDamageLossHandler implements CommandHandler<RecordDamageLossCommand, DamageLossResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(RecordDamageLossHandler.class);

    private final DamageLossRepository damageLossRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryLocationRepository locationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockTransactionRepository transactionRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final DamageLossMapper mapper;

    public RecordDamageLossHandler(
            DamageLossRepository damageLossRepository,
            ProductVariantRepository variantRepository,
            InventoryLocationRepository locationRepository,
            InventoryItemRepository inventoryItemRepository,
            StockLevelRepository stockLevelRepository,
            StockTransactionRepository transactionRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            DamageLossMapper mapper
    ) {
        this.damageLossRepository = damageLossRepository;
        this.variantRepository = variantRepository;
        this.locationRepository = locationRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.transactionRepository = transactionRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.mapper = mapper;
    }

    @Override
    public DamageLossResponseDTO handle(RecordDamageLossCommand cmd) {
        log.debug("Recording damage/loss for variant: {}, location: {}, quantity: {}",
                cmd.variantId(), cmd.locationId(), cmd.quantity());

        // Validate organization exists
        if (!organizationRepository.existsById(cmd.organizationId())) {
            throw new EntityNotFoundException("Organization not found with ID: " + cmd.organizationId());
        }

        // Validate store exists
        if (!storeRepository.existsById(cmd.storeId())) {
            throw new EntityNotFoundException("Store not found with ID: " + cmd.storeId());
        }

        // Validate variant
        ProductVariant variant = variantRepository.findById(cmd.variantId())
                .orElseThrow(() -> new EntityNotFoundException("Variant not found with ID: " + cmd.variantId()));

        // Validate location
        InventoryLocation location = locationRepository.findById(cmd.locationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + cmd.locationId()));

        // Parse reason
        DamageLossReason reason;
        try {
            reason = DamageLossReason.valueOf(cmd.reason().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid damage/loss reason: " + cmd.reason());
        }

        // Find inventory item with sufficient stock
        List<InventoryItem> inventoryItems = inventoryItemRepository.findByVariantIdAndLocationId(
                cmd.variantId(), cmd.locationId()
        );

        InventoryItem inventoryItem = null;
        for (InventoryItem item : inventoryItems) {
            StockLevel sl = stockLevelRepository.findByInventoryItemId(item.getId()).orElse(null);
            if (sl != null && sl.getAvailable() >= cmd.quantity()) {
                inventoryItem = item;
                break;
            }
        }

        if (inventoryItem == null) {
            throw new IllegalArgumentException(
                    "Insufficient stock for variant at location. Required: " + cmd.quantity()
            );
        }

        // Create damage/loss record
        DamageLoss damageLoss = new DamageLoss();
        damageLoss.setOrganizationId(cmd.organizationId());
        damageLoss.setStoreId(cmd.storeId());
        damageLoss.setVariant(variant);
        damageLoss.setLocation(location);
        damageLoss.setQuantity(cmd.quantity());
        damageLoss.setReason(reason);
        damageLoss.setNotes(cmd.notes());
        damageLoss.setDateRecorded(LocalDateTime.now());
        damageLoss.setUserId(cmd.userId());

        DamageLoss saved = damageLossRepository.save(damageLoss);

        // Create stock transaction (negative delta)
        StockTransaction transaction = new StockTransaction();
        transaction.setInventoryItem(inventoryItem);
        transaction.setType(TransactionType.DAMAGE_LOSS);
        transaction.setQuantityDelta(-cmd.quantity());
        transaction.setReferenceType("DAMAGE_LOSS");
        transaction.setReferenceId(saved.getId());
        transaction.setNotes(cmd.notes());
        transaction.setUserId(cmd.userId());
        transactionRepository.save(transaction);

        // Update stock level
        StockLevel stockLevel = stockLevelRepository.findByInventoryItemId(inventoryItem.getId())
                .orElseThrow(() -> new IllegalStateException("Stock level not found"));
        stockLevel.setOnHand(stockLevel.getOnHand() - cmd.quantity());
        stockLevelRepository.save(stockLevel);

        log.info("Recorded damage/loss with ID: {}, quantity: {}, reason: {}",
                saved.getId(), cmd.quantity(), reason);

        return mapper.toResponseDTO(saved);
    }
}
