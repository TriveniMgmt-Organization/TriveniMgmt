package com.store.mgmt.inventory.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateProductDTO;
import com.store.mgmt.inventory.model.dto.ProductDTO;
import com.store.mgmt.inventory.model.dto.UpdateProductDTO;
import com.store.mgmt.inventory.model.entity.ProductTemplate;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        // Removed uses = {ProductVariantMapper.class} to prevent circular mapping
public interface ProductTemplateMapper {

    @Mapping(target = "unitOfMeasure.description", ignore = true)
    @Mapping(target = "unitOfMeasure.conversionFactor", ignore = true)
    @Mapping(target = "variants", ignore = true) // Ignore variants to prevent circular reference: ProductTemplate → variants → template → variants (loop)
    ProductDTO toDto(ProductTemplate product);
    List<ProductDTO> toDtoList(List<ProductTemplate> products);

    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "variants", ignore = true) // Variants are managed separately
    ProductTemplate toEntity(CreateProductDTO createDTO);

    @InheritConfiguration(name = "toEntity")
    void updateProductFromDto(UpdateProductDTO updateDTO, @MappingTarget ProductTemplate product);
}