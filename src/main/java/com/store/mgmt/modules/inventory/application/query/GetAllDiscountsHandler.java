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

import java.util.List;

/**
 * Handler for GetAllDiscountsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllDiscountsHandler implements QueryHandler<GetAllDiscountsQuery, List<DiscountResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllDiscountsHandler.class);

    private final DiscountRepository discountRepository;
    private final DiscountMapper mapper;

    public GetAllDiscountsHandler(DiscountRepository discountRepository, DiscountMapper mapper) {
        this.discountRepository = discountRepository;
        this.mapper = mapper;
    }

    @Override
    public List<DiscountResponseDTO> handle(GetAllDiscountsQuery query) {
        log.debug("Getting all discounts for organization: {}, includeInactive: {}",
                query.organizationId(), query.includeInactive());

        List<Discount> discounts = discountRepository.findByOrganizationId(query.organizationId());

        if (!query.includeInactive()) {
            discounts = discounts.stream()
                    .filter(Discount::isActive)
                    .toList();
        }

        return mapper.toResponseDTOList(discounts);
    }
}
