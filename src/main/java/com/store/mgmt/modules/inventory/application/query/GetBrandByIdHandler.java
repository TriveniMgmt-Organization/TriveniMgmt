package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Brand;
import com.store.mgmt.modules.inventory.domain.repository.BrandRepository;
import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import com.store.mgmt.modules.inventory.application.service.BrandMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for GetBrandByIdQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetBrandByIdHandler implements QueryHandler<GetBrandByIdQuery, BrandResponseDTO> {

    private static final Logger log = LoggerFactory.getLogger(GetBrandByIdHandler.class);

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public GetBrandByIdHandler(BrandRepository brandRepository, BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
    }

    @Override
    public BrandResponseDTO handle(GetBrandByIdQuery query) {
        log.debug("Getting brand by ID: {}", query.id());

        Brand brand = brandRepository.findById(query.id())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with ID: " + query.id()));

        return brandMapper.toResponseDTO(brand);
    }
}
