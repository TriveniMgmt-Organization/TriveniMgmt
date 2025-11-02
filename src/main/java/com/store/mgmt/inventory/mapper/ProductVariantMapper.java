package com.store.mgmt.inventory.mapper;

import com.store.mgmt.common.mapper.BaseMapperConfig;
import com.store.mgmt.inventory.model.dto.CreateProductVariantDTO;
import com.store.mgmt.inventory.model.dto.ProductVariantDTO;
import com.store.mgmt.inventory.model.dto.UpdateProductVariantDTO;
import com.store.mgmt.inventory.model.entity.ProductVariant;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        config = BaseMapperConfig.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductVariantMapper {

    @Mapping(source = "template.id", target = "templateId")
    @Mapping(target = "template", ignore = true) // Ignore to prevent circular reference
    ProductVariantDTO toDto(ProductVariant variant);
    List<ProductVariantDTO> toDtoList(List<ProductVariant> variants);

    // Base method with all BaseEntity ignores - other methods inherit from this
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "inventoryItems", ignore = true)
    ProductVariant toEntity(CreateProductVariantDTO createDTO);

    @InheritConfiguration(name = "toEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "inventoryItems", ignore = true)
    void updateVariantFromDto(UpdateProductVariantDTO updateDTO, @MappingTarget ProductVariant variant);
}

