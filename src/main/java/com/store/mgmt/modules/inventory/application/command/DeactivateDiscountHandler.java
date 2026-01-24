package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Discount;
import com.store.mgmt.modules.inventory.domain.repository.DiscountRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for DeactivateDiscountCommand.
 */
@Component
@Transactional
public class DeactivateDiscountHandler implements CommandHandler<DeactivateDiscountCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeactivateDiscountHandler.class);

    private final DiscountRepository discountRepository;

    public DeactivateDiscountHandler(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public Void handle(DeactivateDiscountCommand cmd) {
        log.debug("Deactivating discount: {}", cmd.id());

        Discount discount = discountRepository.findByIdAndStoreId(cmd.id(), cmd.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with ID: " + cmd.id()));

        discount.setActive(false);
        discountRepository.save(discount);

        log.info("Deactivated discount with ID: {}", cmd.id());
        return null;
    }
}
