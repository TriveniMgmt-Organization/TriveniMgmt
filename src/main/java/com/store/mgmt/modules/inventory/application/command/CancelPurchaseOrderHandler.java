package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.PurchaseOrder;
import com.store.mgmt.modules.inventory.domain.model.PurchaseOrderStatus;
import com.store.mgmt.modules.inventory.domain.repository.PurchaseOrderRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CancelPurchaseOrderCommand.
 */
@Component
@Transactional
public class CancelPurchaseOrderHandler implements CommandHandler<CancelPurchaseOrderCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(CancelPurchaseOrderHandler.class);

    private final PurchaseOrderRepository poRepository;

    public CancelPurchaseOrderHandler(PurchaseOrderRepository poRepository) {
        this.poRepository = poRepository;
    }

    @Override
    public Void handle(CancelPurchaseOrderCommand cmd) {
        log.debug("Cancelling purchase order: {}", cmd.id());

        PurchaseOrder po = poRepository.findByIdAndOrganizationId(cmd.id(), cmd.organizationId())
                .orElseThrow(() -> new EntityNotFoundException("Purchase order not found with ID: " + cmd.id()));

        if (po.getStatus() == PurchaseOrderStatus.RECEIVED_COMPLETE) {
            throw new IllegalArgumentException("Cannot cancel a fully received purchase order");
        }

        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Purchase order is already cancelled");
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        poRepository.save(po);

        log.info("Cancelled purchase order with ID: {}", cmd.id());
        return null;
    }
}
