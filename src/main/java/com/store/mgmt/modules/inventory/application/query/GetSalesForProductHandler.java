package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Sale;
import com.store.mgmt.modules.inventory.domain.model.SaleItem;
import com.store.mgmt.modules.inventory.domain.repository.SaleItemRepository;
import com.store.mgmt.modules.inventory.application.dto.SaleResponseDTO;
import com.store.mgmt.modules.inventory.application.service.SaleMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for GetSalesForProductQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetSalesForProductHandler implements QueryHandler<GetSalesForProductQuery, List<SaleResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetSalesForProductHandler.class);

    private final SaleItemRepository saleItemRepository;
    private final SaleMapper mapper;

    public GetSalesForProductHandler(SaleItemRepository saleItemRepository, SaleMapper mapper) {
        this.saleItemRepository = saleItemRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SaleResponseDTO> handle(GetSalesForProductQuery query) {
        log.debug("Getting sales for product template {} in store {}",
                query.productTemplateId(), query.storeId());

        // Find sale items for the product template in the store
        List<SaleItem> saleItems = saleItemRepository.findByProductTemplateIdAndStoreId(
                query.productTemplateId(),
                query.storeId()
        );

        // Get unique sales from the sale items
        List<Sale> uniqueSales = saleItems.stream()
                .map(SaleItem::getSale)
                .distinct()
                .sorted((s1, s2) -> s2.getSaleTimestamp().compareTo(s1.getSaleTimestamp()))
                .collect(Collectors.toList());

        return mapper.toResponseDTOList(uniqueSales);
    }
}
