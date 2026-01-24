package com.store.mgmt.modules.inventory.application.command;

import com.store.mgmt.modules.inventory.domain.model.*;
import com.store.mgmt.modules.inventory.domain.repository.*;
import com.store.mgmt.modules.inventory.application.dto.CreateSaleItemRequestDTO;
import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SaleMapper;
import com.store.mgmt.modules.inventory.domain.service.SalePricingService;
import com.store.mgmt.modules.inventory.domain.service.StockAllocationService;
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
import java.util.Set;

/**
 * Handler for ProcessSaleCommand.
 * Processes a sale with FIFO stock allocation and discount calculation.
 */
@Component
@Transactional
public class ProcessSaleHandler implements CommandHandler<ProcessSaleCommand, SaleResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(ProcessSaleHandler.class);

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final StockAllocationService stockAllocationService;
    private final SalePricingService salePricingService;
    private final SaleMapper mapper;

    public ProcessSaleHandler(
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            ProductTemplateRepository productTemplateRepository,
            ProductVariantRepository productVariantRepository,
            StoreRepository storeRepository,
            StockAllocationService stockAllocationService,
            SalePricingService salePricingService,
            SaleMapper mapper
    ) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productTemplateRepository = productTemplateRepository;
        this.productVariantRepository = productVariantRepository;
        this.storeRepository = storeRepository;
        this.stockAllocationService = stockAllocationService;
        this.salePricingService = salePricingService;
        this.mapper = mapper;
    }

    @Override
    public SaleResponseDTO handle(ProcessSaleCommand cmd) {
        log.info("Processing sale for store: {}", cmd.storeId());

        if (cmd.items() == null || cmd.items().isEmpty()) {
            throw new IllegalArgumentException("Sale must have at least one item");
        }

        // Validate store exists
        if (!storeRepository.existsById(cmd.storeId())) {
            throw new EntityNotFoundException("Store not found: " + cmd.storeId());
        }

        // Validate payment method
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(cmd.paymentMethod());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + cmd.paymentMethod());
        }

        // Create sale entity
        Sale sale = new Sale();
        sale.setStoreId(cmd.storeId());
        sale.setSaleTimestamp(LocalDateTime.now());
        sale.setPaymentMethod(paymentMethod);
        sale.setTransactionId(cmd.transactionId());
        sale.setNotes(cmd.notes());
        sale.setUserId(cmd.userId());
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setTotalDiscountAmount(BigDecimal.ZERO);

        // Save sale first to get ID for reference
        sale = saleRepository.save(sale);
        String saleReference = "SALE-" + sale.getId().toString().substring(0, 8);

        // Process sale items
        Set<SaleItem> saleItems = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        for (CreateSaleItemRequestDTO itemDTO : cmd.items()) {
            // Validate product template
            ProductTemplate template = productTemplateRepository.findById(itemDTO.productTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product template not found: " + itemDTO.productTemplateId()));

            // Validate variant
            ProductVariant variant = productVariantRepository.findById(itemDTO.variantId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product variant not found: " + itemDTO.variantId()));

            // Verify variant belongs to template
            if (!variant.getTemplate().getId().equals(template.getId())) {
                throw new IllegalArgumentException(
                        "Variant " + itemDTO.variantId() + " does not belong to product " + itemDTO.productTemplateId());
            }

            // Check stock availability and allocate using FIFO
            stockAllocationService.allocateStock(
                    itemDTO.variantId(),
                    cmd.storeId(),
                    itemDTO.quantity(),
                    saleReference
            );

            // Calculate discount if not provided
            BigDecimal discountAmount = itemDTO.discountAmount();
            if (discountAmount == null) {
                discountAmount = salePricingService.calculateLineDiscount(
                        itemDTO.variantId(),
                        itemDTO.unitPrice(),
                        itemDTO.quantity(),
                        cmd.storeId()
                );
            }

            // Create sale item
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setStoreId(cmd.storeId());
            saleItem.setProductTemplate(template);
            saleItem.setVariant(variant);
            saleItem.setQuantity(itemDTO.quantity());
            saleItem.setUnitPrice(itemDTO.unitPrice());
            saleItem.setDiscountAmount(discountAmount);

            saleItems.add(saleItem);

            // Accumulate totals
            BigDecimal lineTotal = itemDTO.unitPrice().multiply(BigDecimal.valueOf(itemDTO.quantity()));
            totalAmount = totalAmount.add(lineTotal);
            totalDiscountAmount = totalDiscountAmount.add(discountAmount);
        }

        // Save sale items
        saleItemRepository.saveAll(saleItems);

        // Update sale with totals
        sale.setTotalAmount(totalAmount.subtract(totalDiscountAmount));
        sale.setTotalDiscountAmount(totalDiscountAmount);
        sale.setSaleItems(saleItems);
        sale = saleRepository.save(sale);

        log.info("Sale {} processed successfully with total amount: {}", sale.getId(), sale.getTotalAmount());

        return mapper.toResponseDTO(sale);
    }
}
