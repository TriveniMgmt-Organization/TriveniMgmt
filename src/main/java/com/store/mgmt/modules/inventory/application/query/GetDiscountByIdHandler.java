package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Discount;
import com.store.mgmt.modules.inventory.domain.repository.DiscountRepository;
import com.store.mgmt.modules.inventory.application.dto.DiscountResponseDTO;
import com.store.mgmt.modules.inventory.application.service.DiscountMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetDiscountByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetDiscountByIdHandler implements QueryHandler<GetDiscountByIdQuery, DiscountResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetDiscountByIdHandler.class);

    private final DiscountRepository discountRepository;
    private final DiscountMapper mapper;

    public GetDiscountByIdHandler(DiscountRepository discountRepository, DiscountMapper mapper) {
        this.discountRepository = discountRepository;
        this.mapper = mapper;
    }

    @Override
    public DiscountResponseDTO handle(GetDiscountByIdQuery query) {
        log.debug("Getting discount by ID: {}", query.id());

        Discount discount = discountRepository.findByIdAndStoreId(query.id(), query.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with ID: " + query.id()));

        return mapper.toResponseDTO(discount);
    }
}
