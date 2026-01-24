package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.PurchaseOrder;
import com.store.mgmt.modules.inventory.domain.model.PurchaseOrderStatus;
import com.store.mgmt.modules.inventory.domain.repository.PurchaseOrderRepository;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.modules.inventory.application.service.PurchaseOrderMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdatePurchaseOrderCommand.
 */
@Component
@Transactional
public class UpdatePurchaseOrderHandler implements CommandHandler<UpdatePurchaseOrderCommand, PurchaseOrderResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdatePurchaseOrderHandler.class);

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderMapper mapper;

    public UpdatePurchaseOrderHandler(PurchaseOrderRepository poRepository, PurchaseOrderMapper mapper) {
        this.poRepository = poRepository;
        this.mapper = mapper;
    }

    @Override
    public PurchaseOrderResponseDTO handle(UpdatePurchaseOrderCommand cmd) {
        log.debug("Updating purchase order: {}", cmd.id());

        PurchaseOrder po = poRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with ID: " + cmd.id()));

        // Validate status changes
        if (po.getStatus() == PurchaseOrderStatus.CANCELLED ||
            po.getStatus() == PurchaseOrderStatus.RECEIVED_COMPLETE) {
            throw new IllegalArgumentException("Cannot update a cancelled or fully received purchase order");
        }

        if (cmd.expectedDeliveryDate() != null) {
            po.setExpectedDeliveryDate(cmd.expectedDeliveryDate());
        }

        if (cmd.trackingNumber() != null) {
            po.setTrackingNumber(cmd.trackingNumber());
        }

        if (cmd.notes() != null) {
            po.setNotes(cmd.notes());
        }

        if (cmd.status() != null) {
            try {
                PurchaseOrderStatus newStatus = PurchaseOrderStatus.valueOf(cmd.status().toUpperCase());
                // Only allow certain transitions
                if (newStatus == PurchaseOrderStatus.ORDERED && po.getStatus() == PurchaseOrderStatus.PENDING) {
                    po.setStatus(newStatus);
                } else if (newStatus == PurchaseOrderStatus.CANCELLED &&
                           (po.getStatus() == PurchaseOrderStatus.PENDING || po.getStatus() == PurchaseOrderStatus.ORDERED)) {
                    po.setStatus(newStatus);
                } else {
                    throw new IllegalArgumentException("Invalid status transition from " + po.getStatus() + " to " + newStatus);
                }
            } catch (IllegalArgumentException e) {
                if (e.getMessage().startsWith("Invalid status")) {
                    throw e;
                }
                throw new IllegalArgumentException("Invalid status: " + cmd.status());
            }
        }

        PurchaseOrder saved = poRepository.save(po);
        log.info("Updated purchase order with ID: {}", saved.getId());

        return mapper.toResponseDTO(saved);
    }
}
