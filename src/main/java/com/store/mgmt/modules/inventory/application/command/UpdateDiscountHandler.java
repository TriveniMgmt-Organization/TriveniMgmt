package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.Category;
import com.store.mgmt.modules.inventory.domain.model.Discount;
import com.store.mgmt.modules.inventory.domain.model.ProductTemplate;
import com.store.mgmt.modules.inventory.domain.model.DiscountType;
import com.store.mgmt.modules.inventory.domain.repository.CategoryRepository;
import com.store.mgmt.modules.inventory.domain.repository.DiscountRepository;
import com.store.mgmt.modules.inventory.domain.repository.ProductTemplateRepository;
import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.modules.inventory.application.service.DiscountMapper;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for UpdateDiscountCommand.
 */
@Component
@Transactional
public class UpdateDiscountHandler implements CommandHandler<UpdateDiscountCommand, DiscountResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(UpdateDiscountHandler.class);

    private final DiscountRepository discountRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private final CategoryRepository categoryRepository;
    private final DiscountMapper mapper;

    public UpdateDiscountHandler(
            DiscountRepository discountRepository,
            ProductTemplateRepository productTemplateRepository,
            CategoryRepository categoryRepository,
            DiscountMapper mapper
    ) {
        this.discountRepository = discountRepository;
        this.productTemplateRepository = productTemplateRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    public DiscountResponseDTO handle(UpdateDiscountCommand cmd) {
        log.debug("Updating discount: {}", cmd.id());

        Discount discount = discountRepository.findByIdAndStoreId(cmd.id(), cmd.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with ID: " + cmd.id()));

        // Update name if provided
        if (cmd.name() != null && !cmd.name().equals(discount.getName())) {
            discountRepository.findByNameAndOrganizationId(cmd.name(), discount.getOrganizationId())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(cmd.id())) {
                            throw new IllegalArgumentException("Discount with name '" + cmd.name() + "' already exists");
                        }
                    });
            discount.setName(cmd.name());
        }

        // Update type if provided
        if (cmd.type() != null) {
            try {
                discount.setType(DiscountType.valueOf(cmd.type().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid discount type: " + cmd.type());
            }
        }

        if (cmd.value() != null) {
            discount.setValue(cmd.value());
        }

        if (cmd.startDate() != null) {
            discount.setStartDate(cmd.startDate());
        }

        if (cmd.endDate() != null) {
            discount.setEndDate(cmd.endDate());
        }

        // Validate dates
        if (discount.getEndDate().isBefore(discount.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (cmd.description() != null) {
            discount.setDescription(cmd.description());
        }

        if (cmd.minimumPurchaseAmount() != null) {
            discount.setMinimumPurchaseAmount(cmd.minimumPurchaseAmount());
        }

        if (cmd.minimumItemQuantity() != null) {
            discount.setMinimumItemQuantity(cmd.minimumItemQuantity());
        }

        if (cmd.isActive() != null) {
            discount.setActive(cmd.isActive());
        }

        // Update product template if provided
        if (cmd.productTemplateId() != null) {
            ProductTemplate template = productTemplateRepository.findById(cmd.productTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("Product template not found with ID: " + cmd.productTemplateId()));
            discount.setProductTemplate(template);
        }

        // Update category if provided
        if (cmd.categoryId() != null) {
            Category category = categoryRepository.findById(cmd.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + cmd.categoryId()));
            discount.setCategory(category);
        }

        Discount saved = discountRepository.save(discount);
        log.info("Updated discount with ID: {}", saved.getId());

        return mapper.toResponseDTO(saved);
    }
}
