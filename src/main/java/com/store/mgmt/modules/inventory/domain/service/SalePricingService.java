package com.store.mgmt.modules.inventory.domain.service;

import com.store.mgmt.modules.inventory.domain.model.Discount;
import com.store.mgmt.modules.inventory.domain.model.ProductTemplate;
import com.store.mgmt.modules.inventory.domain.model.ProductVariant;
import com.store.mgmt.modules.inventory.domain.model.DiscountType;
import com.store.mgmt.modules.inventory.domain.repository.DiscountRepository;
import com.store.mgmt.modules.inventory.domain.repository.ProductTemplateRepository;
import com.store.mgmt.modules.inventory.domain.repository.ProductVariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for calculating sale prices and discounts.
 */
@Service
public class SalePricingService {

    private static final Logger log = LoggerFactory.getLogger(SalePricingService.class);

    private final DiscountRepository discountRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private final ProductVariantRepository productVariantRepository;

    public SalePricingService(
            DiscountRepository discountRepository,
            ProductTemplateRepository productTemplateRepository,
            ProductVariantRepository productVariantRepository
    ) {
        this.discountRepository = discountRepository;
        this.productTemplateRepository = productTemplateRepository;
        this.productVariantRepository = productVariantRepository;
    }

    /**
     * Calculate the discount amount for a line item.
     *
     * @param variantId The variant being sold
     * @param unitPrice The unit price
     * @param quantity  The quantity being sold
     * @param storeId   The store context
     * @return The total discount amount for this line
     */
    public BigDecimal calculateLineDiscount(
            UUID variantId,
            BigDecimal unitPrice,
            int quantity,
            UUID storeId
    ) {
        List<Discount> applicableDiscounts = getApplicableDiscounts(variantId, storeId);

        if (applicableDiscounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Use the best single discount (highest value)
        Discount bestDiscount = applicableDiscounts.stream()
                .max(Comparator.comparing(d -> calculateDiscountValue(d, unitPrice, quantity)))
                .orElse(null);

        if (bestDiscount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount = calculateDiscountValue(bestDiscount, unitPrice, quantity);
        log.debug("Applied discount {} to variant {}: {}", bestDiscount.getName(), variantId, discountAmount);

        return discountAmount;
    }

    /**
     * Get all applicable active discounts for a variant.
     */
    public List<Discount> getApplicableDiscounts(UUID variantId, UUID storeId) {
        List<Discount> applicableDiscounts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return applicableDiscounts;
        }

        // Get product-level discounts
        UUID productTemplateId = variant.getTemplate().getId();
        List<Discount> productDiscounts = discountRepository
                .findByProductTemplateIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        productTemplateId, today, today
                );
        applicableDiscounts.addAll(productDiscounts);

        // Get category-level discounts
        ProductTemplate template = productTemplateRepository.findById(productTemplateId).orElse(null);
        if (template != null && template.getCategory() != null) {
            List<Discount> categoryDiscounts = discountRepository
                    .findByCategoryIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            template.getCategory().getId(), today, today
                    );
            applicableDiscounts.addAll(categoryDiscounts);
        }

        return applicableDiscounts.stream()
                .filter(d -> d.getDeletedAt() == null)
                .filter(d -> d.isActive())
                .toList();
    }

    /**
     * Calculate the discount value for a specific discount on a line.
     */
    private BigDecimal calculateDiscountValue(Discount discount, BigDecimal unitPrice, int quantity) {
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (discount.getType() == DiscountType.PERCENTAGE) {
            // Percentage discount
            return lineTotal
                    .multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (discount.getType() == DiscountType.FIXED_AMOUNT) {
            // Fixed amount discount - applies per unit
            BigDecimal totalFixedDiscount = discount.getValue().multiply(BigDecimal.valueOf(quantity));
            // Don't exceed line total
            return totalFixedDiscount.min(lineTotal);
        }

        return BigDecimal.ZERO;
    }

    /**
     * Calculate total line amount after discount.
     */
    public BigDecimal calculateLineTotal(BigDecimal unitPrice, int quantity, BigDecimal discountAmount) {
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return lineTotal.subtract(discountAmount).max(BigDecimal.ZERO);
    }
}
