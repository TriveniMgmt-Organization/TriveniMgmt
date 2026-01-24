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
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for CreateDiscountCommand.
 */
@Component
@Transactional
public class CreateDiscountHandler implements CommandHandler<CreateDiscountCommand, DiscountResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreateDiscountHandler.class);

    private final DiscountRepository discountRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private final CategoryRepository categoryRepository;
    private final DiscountMapper mapper;

    public CreateDiscountHandler(
            DiscountRepository discountRepository,
            OrganizationRepository organizationRepository,
            StoreRepository storeRepository,
            ProductTemplateRepository productTemplateRepository,
            CategoryRepository categoryRepository,
            DiscountMapper mapper
    ) {
        this.discountRepository = discountRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.productTemplateRepository = productTemplateRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    public DiscountResponseDTO handle(CreateDiscountCommand cmd) {
        log.debug("Creating discount: {}", cmd.name());

        // Validate organization exists
        if (!organizationRepository.existsById(cmd.organizationId())) {
            throw new EntityNotFoundException("Organization not found with ID: " + cmd.organizationId());
        }

        // Validate store exists
        if (!storeRepository.existsById(cmd.storeId())) {
            throw new EntityNotFoundException("Store not found with ID: " + cmd.storeId());
        }

        // Check for duplicate name
        discountRepository.findByNameAndOrganizationId(cmd.name(), cmd.organizationId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Discount with name '" + cmd.name() + "' already exists");
        });

        // Validate dates
        if (cmd.endDate().isBefore(cmd.startDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Parse discount type
        DiscountType discountType;
        try {
            discountType = DiscountType.valueOf(cmd.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid discount type: " + cmd.type());
        }

        Discount discount = new Discount();
        discount.setOrganizationId(cmd.organizationId());
        discount.setStoreId(cmd.storeId());
        discount.setName(cmd.name());
        discount.setType(discountType);
        discount.setValue(cmd.value());
        discount.setStartDate(cmd.startDate());
        discount.setEndDate(cmd.endDate());
        discount.setDescription(cmd.description());
        discount.setMinimumPurchaseAmount(cmd.minimumPurchaseAmount());
        discount.setMinimumItemQuantity(cmd.minimumItemQuantity());
        discount.setActive(cmd.isActive() != null ? cmd.isActive() : true);

        // Set product template if provided
        if (cmd.productTemplateId() != null) {
            ProductTemplate template = productTemplateRepository.findById(cmd.productTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("Product template not found with ID: " + cmd.productTemplateId()));
            discount.setProductTemplate(template);
        }

        // Set category if provided
        if (cmd.categoryId() != null) {
            Category category = categoryRepository.findById(cmd.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + cmd.categoryId()));
            discount.setCategory(category);
        }

        Discount saved = discountRepository.save(discount);
        log.info("Created discount with ID: {}, name: {}", saved.getId(), saved.getName());

        return mapper.toResponseDTO(saved);
    }
}
