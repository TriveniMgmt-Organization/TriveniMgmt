package com.store.mgmt.modules.inventory.application.query;

import com.store.mgmt.modules.inventory.domain.model.Brand;
import com.store.mgmt.modules.inventory.domain.repository.BrandRepository;
import com.store.mgmt.modules.inventory.application.dto.BrandResponseDTO;
import com.store.mgmt.modules.inventory.application.service.BrandMapper;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handler for GetAllBrandsQuery.
 */
@Component
@Transactional(readOnly = true)
public class GetAllBrandsHandler implements QueryHandler<GetAllBrandsQuery, List<BrandResponseDTO>> {

    private static final Logger log = LoggerFactory.getLogger(GetAllBrandsHandler.class);

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public GetAllBrandsHandler(BrandRepository brandRepository, BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
    }

    @Override
    public List<BrandResponseDTO> handle(GetAllBrandsQuery query) {
        log.debug("Getting all brands, includeInactive: {}", query.includeInactive());

        List<Brand> brands;
        if (query.includeInactive()) {
            brands = brandRepository.findAll();
        } else {
            brands = brandRepository.findAll().stream()
                    .filter(Brand::isActive)
                    .toList();
        }

        return brandMapper.toResponseDTOList(brands);
    }
}
