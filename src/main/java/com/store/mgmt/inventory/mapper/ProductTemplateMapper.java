package com.store.mgmt.inventory.mapper;

import com.store.mgmt.inventory.model.dto.CreateProductDTO;
import com.store.mgmt.inventory.model.dto.ProductDTO;
import com.store.mgmt.inventory.model.dto.UpdateProductDTO;
import com.store.mgmt.inventory.model.entity.ProductTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // Ignore nulls for updates
public interface ProductTemplateMapper {

    ProductDTO toDto(ProductTemplate product);
    List<ProductDTO> toDtoList(List<ProductTemplate> products);

    @Mapping(target = "id", ignore = true) // ID is generated by JPA
    ProductTemplate toEntity(CreateProductDTO createDTO);

    // For updating an existing product from DTO
    // nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE in @Mapper annotation
    // handles fields that are null in DTO (i.e., not provided) to be ignored.
    @Mapping(target = "id", ignore = true) // Don't update ID
    void updateProductFromDto(UpdateProductDTO updateDTO, @MappingTarget ProductTemplate product);
}