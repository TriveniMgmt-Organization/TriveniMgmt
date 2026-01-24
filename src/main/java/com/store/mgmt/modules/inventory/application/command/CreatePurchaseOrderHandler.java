package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.*;
import com.store.mgmt.modules.inventory.application.dto.CreatePurchaseOrderItemRequestDTO;
import com.store.mgmt.modules.inventory.application.dto.PurchaseOrderResponseDTO;
import com.store.mgmt.modules.inventory.application.service.PurchaseOrderMapper;
import com.store.mgmt.modules.organization.domain.repository.OrganizationRepository;
import com.store.mgmt.modules.organization.domain.repository.StoreRepository;
import com.store.mgmt.shared.application.command.CommandHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * Handler for CreatePurchaseOrderCommand.
 */
@Component
@Transactional
public class CreatePurchaseOrderHandler implements CommandHandler<CreatePurchaseOrderCommand, PurchaseOrderResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(CreatePurchaseOrderHandler.class);

    private final PurchaseOrderRepository poRepository;
    private final OrganizationRepository organizationRepository;
    private final SupplierRepository supplierRepository;
    private final ProductTemplateRepository templateRepository;
    private final ProductVariantRepository variantRepository;
    private final StoreRepository storeRepository;
    private final PurchaseOrderMapper mapper;

    public CreatePurchaseOrderHandler(
            PurchaseOrderRepository poRepository,
            OrganizationRepository organizationRepository,
            SupplierRepository supplierRepository,
            ProductTemplateRepository templateRepository,
            ProductVariantRepository variantRepository,
            StoreRepository storeRepository,
            PurchaseOrderMapper mapper
    ) {
        this.poRepository = poRepository;
        this.organizationRepository = organizationRepository;
        this.supplierRepository = supplierRepository;
        this.templateRepository = templateRepository;
        this.variantRepository = variantRepository;
        this.storeRepository = storeRepository;
        this.mapper = mapper;
    }

    @Override
    public PurchaseOrderResponseDTO handle(CreatePurchaseOrderCommand cmd) {
        log.debug("Creating purchase order for supplier: {}", cmd.supplierId());

        // Validate organization exists
        if (!organizationRepository.existsById(cmd.organizationId())) {
            throw new EntityNotFoundException("Organization not found with ID: " + cmd.organizationId());
        }

        // Validate supplier
        Supplier supplier = supplierRepository.findById(cmd.supplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + cmd.supplierId()));

        // Create purchase order
        PurchaseOrder po = new PurchaseOrder();
        po.setOrganizationId(cmd.organizationId());
        po.setSupplier(supplier);
        po.setOrderDate(LocalDateTime.now());
        po.setExpectedDeliveryDate(cmd.expectedDeliveryDate());
        po.setTrackingNumber(cmd.trackingNumber());
        po.setNotes(cmd.notes());
        po.setStatus(PurchaseOrderStatus.PENDING);
        po.setPurchaseOrderItems(new HashSet<>());
        po.setUserId(cmd.userId());

        // Calculate total and create items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreatePurchaseOrderItemRequestDTO itemDto : cmd.items()) {
            // Validate store exists
            if (!storeRepository.existsById(itemDto.storeId())) {
                throw new EntityNotFoundException("Store not found with ID: " + itemDto.storeId());
            }

            ProductTemplate template = templateRepository.findById(itemDto.productTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("Product template not found with ID: " + itemDto.productTemplateId()));

            ProductVariant variant = variantRepository.findById(itemDto.variantId())
                    .orElseThrow(() -> new EntityNotFoundException("Product variant not found with ID: " + itemDto.variantId()));

            PurchaseOrderItem poItem = new PurchaseOrderItem();
            poItem.setPurchaseOrder(po);
            poItem.setStoreId(itemDto.storeId());
            poItem.setProductTemplate(template);
            poItem.setVariant(variant);
            poItem.setOrderedQuantity(itemDto.orderedQuantity());
            poItem.setReceivedQuantity(0);
            poItem.setUnitCost(itemDto.unitCost());

            po.getPurchaseOrderItems().add(poItem);
            totalAmount = totalAmount.add(itemDto.unitCost().multiply(BigDecimal.valueOf(itemDto.orderedQuantity())));
        }

        po.setTotalEstimatedAmount(totalAmount);

        PurchaseOrder saved = poRepository.save(po);
        log.info("Created purchase order with ID: {}", saved.getId());

        return mapper.toResponseDTO(saved);
    }
}
