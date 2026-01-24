package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Discount;
import com.store.mgmt.modules.inventory.domain.repository.DiscountRepository;
import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.modules.inventory.application.service.DiscountMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Handler for GetActiveDiscountsForProductQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetActiveDiscountsForProductHandler implements QueryHandler<GetActiveDiscountsForProductQuery, List<DiscountResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetActiveDiscountsForProductHandler.class);

    private final DiscountRepository discountRepository;
    private final DiscountMapper mapper;

    public GetActiveDiscountsForProductHandler(DiscountRepository discountRepository, DiscountMapper mapper) {
        this.discountRepository = discountRepository;
        this.mapper = mapper;
    }

    @Override
    public List<DiscountResponseDTO> handle(GetActiveDiscountsForProductQuery query) {
        log.debug("Getting active discounts for product: {}", query.productTemplateId());

        LocalDate today = LocalDate.now();
        List<Discount> discounts = discountRepository.findByProductTemplateIdAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                query.productTemplateId(), today, today
        );

        return mapper.toResponseDTOList(discounts);
    }
}
